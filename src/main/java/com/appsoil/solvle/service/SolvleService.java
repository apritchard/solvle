package com.appsoil.solvle.service;

import com.appsoil.solvle.controller.SolvleDTO;
import com.appsoil.solvle.data.Dictionary;
import com.appsoil.solvle.data.Word;
import com.appsoil.solvle.data.WordFrequencyScore;
import com.appsoil.solvle.data.WordRestrictions;
import com.appsoil.solvle.service.solvers.FishingSolver;
import com.appsoil.solvle.service.solvers.Solver;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

@Service
@Log4j2
public class SolvleService {

    private final Dictionary simpleDictionary;
    private final Dictionary bigDictionary;
    private final Dictionary hugeDictionary;

    private final int MAX_RESULT_LIST_SIZE = 100;
    private final int FISHING_WORD_SIZE = 25;

    public SolvleService(@Qualifier("simpleDictionary") Dictionary simpleDictionary,
                         @Qualifier("bigDictionary") Dictionary bigDictionary,
                         @Qualifier("hugeDictionary") Dictionary hugeDictionary) {
        this.simpleDictionary = simpleDictionary;
        this.bigDictionary = bigDictionary;
        this.hugeDictionary = hugeDictionary;
    }

    @Cacheable("validWords")
    public SolvleDTO getValidWords(String restrictionString, int length, String wordList, WordCalculationConfig wordCalculationConfig) {

        log.debug("Searching for words of length {}", length);

        // parse the string to identify required letters and position exclusions
        WordRestrictions wordRestrictions = new WordRestrictions(restrictionString);

        Set<Word> wordSet = getPrimarySet(wordList, length);
        Set<Word> fishingSet = getFishingSet(wordList, length);

        SolvleDTO result = getValidWords(wordRestrictions, wordSet, fishingSet, wordCalculationConfig);

        log.info("Found {} length {} matches for {}", result.totalWords(), length, restrictionString);
        return result;
    }

    public SolvleDTO getValidWords(WordRestrictions wordRestrictions, Set<Word> wordSet, Set<Word> fishingSet, WordCalculationConfig wordCalculationConfig) {

        WordCalculationService wordCalculationService = new WordCalculationService(wordCalculationConfig);

        // find all the valid words in our dictionary for this restriction string
        Set<Word> containedWords = wordCalculationService.findMatchingWords(wordSet, wordRestrictions);

        // data needed for the DTO
        Set<WordFrequencyScore> wordFrequencyScores; // scores for possible solution words
        Set<WordFrequencyScore> fishingWordScores;   // scores for non-solution words
        Set<WordFrequencyScore> remainingWords;      // words that reduce the solution set the most
        Map<Character, LongAdder> characterCounts;   // number of words with each character

        // calculate how many words in the valid word set contain each character and
        //   then generate scores for words in the valid list and fishing list
        if(wordCalculationConfig.rightLocationMultiplier() == 0) {
            characterCounts = wordCalculationService.calculateCharacterCounts(containedWords);

            wordFrequencyScores = wordCalculationService
                    .calculateViableWords(containedWords, characterCounts, containedWords.size(), 0, MAX_RESULT_LIST_SIZE);
            fishingWordScores = containedWords.size() < 1 ? new HashSet<>() : wordCalculationService
                    .calculateFishingWords(fishingSet, characterCounts, containedWords.size(), FISHING_WORD_SIZE, wordRestrictions.requiredLetters());
        } else {
            Map<Integer, Map<Character, LongAdder>> positionalCharCounts = wordCalculationService.calculateCharacterCountsByPosition(containedWords);
            wordFrequencyScores = wordCalculationService
                    .calculateViableWordsByPosition(containedWords, positionalCharCounts, containedWords, 0, MAX_RESULT_LIST_SIZE, wordRestrictions);
            fishingWordScores = containedWords.size() < 1 ? new HashSet<>() : wordCalculationService
                    .calculateFishingWordsByPosition(fishingSet, positionalCharCounts, containedWords, FISHING_WORD_SIZE, wordRestrictions);

            //merge positional char counts for use in the DTO
            characterCounts = mergeCharacterCounts(positionalCharCounts);
        }

        // generate words that optimally partition the viable set
        if(wordCalculationConfig.partitionThreshold() <= 0) {
            remainingWords = null;
        } else {
            remainingWords = wordCalculationService.calculateRemainingWords(wordRestrictions, containedWords, wordFrequencyScores, fishingWordScores);
        }

        return new SolvleDTO("", wordFrequencyScores, fishingWordScores, remainingWords, containedWords.size(), characterCounts);
    }

    private Map<Character, LongAdder> mergeCharacterCounts(Map<Integer, Map<Character, LongAdder>> countsByPos) {
        // this returns slightly higher values than the original 'words with characters' map, because words with
        // duplicate chars are represented multiple times in the countsByPos map. The information about words
        // with duplicates is lost at this point and not worth recalculating.
        return countsByPos.values().stream().flatMap(map -> map.entrySet().stream()).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (v1, v2) -> {
            v1.add(v2.longValue());
            return v1;
        }));
    }

    private Set<Word> getPrimarySet(String wordList, int length) {
        Dictionary dictionary = switch (wordList) {
            case "simple" -> length == 5 ? simpleDictionary : bigDictionary;
            case "huge" -> hugeDictionary;
            default -> bigDictionary;
        };
        return dictionary.wordsBySize().get(length);
    }

    private Set<Word> getFishingSet(String wordList, int length) {
        // use the big dictionary for fishing simple words, because answers are not required to be valid
        Dictionary fishingWordDictionary = switch(wordList) {
            case "huge" -> hugeDictionary;
            default -> bigDictionary;
        };
        return fishingWordDictionary.wordsBySize().get(length);
    }

    /**
     * Solves for every word in the simple dictionary and returns a map of the guess-route the provided Solver will
     * choose for each word in the simple dictionary.
     * @param solver Solver implementation class that decide when to use words from fishing/partition/valid lists
     * @param firstWord Optional. Overrides the WordCalculationService's first word guess to see how results change.
     *                  Will otherwise use the first fishing word as calculated for the provided Config
     * @param wordCalculationConfig
     * @return
     */
    public Map<String, List<String>> solveDictionary(Solver solver, String firstWord, WordCalculationConfig wordCalculationConfig) {

        Set<Word> words = getPrimarySet("simple", 5);

        if(firstWord == null || firstWord.isBlank()) {
            SolvleDTO guess = getValidWords(WordRestrictions.noRestrictions(), words, getFishingSet("simple", 5), wordCalculationConfig);
            firstWord = guess.fishingWords().stream().findFirst().get().word();
        }

        final String startingWord = firstWord;

        Map<String, List<String>> outcome = new ConcurrentHashMap<>();
        words.stream().forEach( word -> {
            List<String> guesses = solveWord(solver, word, startingWord);
            outcome.put(word.word(), guesses);
        });

        return outcome;
    }


    public List<String> solveWord(Word word) {
        return solveWord(new FishingSolver(this), word, "");
    }

    /**
     * Attempts to guess the correct solution to a word and returns a list of which words
     * would be used to find the solution.
     * @param word The solution
     * @return An ordered list of guesses, of which the final one is the solution
     */
    public List<String> solveWord(Solver solver, Word word, String firstWord) {

        //get initial restrictions
        Set<Word> wordSet = getPrimarySet("simple", word.getLength());
        Set<Word> fishingSet = getFishingSet("simple", word.getLength());

        if(!wordSet.contains(word)) {
            return List.of("Word Not Found");
        } else if ( !"".equals(firstWord) && !fishingSet.contains(new Word(firstWord))) {
            return List.of("First word not valid");
        }

        return solver.solve(word, wordSet, fishingSet, firstWord);
    }
}
