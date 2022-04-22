package com.appsoil.solvle.service;

import com.appsoil.solvle.controller.SolvleDTO;
import com.appsoil.solvle.data.Dictionary;
import com.appsoil.solvle.data.Word;
import com.appsoil.solvle.data.WordFrequencyScore;
import com.appsoil.solvle.data.WordRestrictions;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

@Service
@Log4j2
public class SolvleService {

    private final Dictionary defaultDictionary;
    private final Dictionary simpleDictionary;
    private final Dictionary bigDictionary;
    private final Dictionary hugeDictionary;

    private final WordCalculationService wordCalculationService;

    private final int MAX_RESULT_LIST_SIZE = 100;
    private final int FISHING_WORD_SIZE = 25;

    public SolvleService(@Qualifier("defaultDictionary") Dictionary defaultDictionary,
                         @Qualifier("simpleDictionary") Dictionary simpleDictionary,
                         @Qualifier("bigDictionary") Dictionary bigDictionary,
                         @Qualifier("hugeDictionary") Dictionary hugeDictionary,
                         WordCalculationService wordCalculationService) {
        this.defaultDictionary = defaultDictionary;
        this.simpleDictionary = simpleDictionary;
        this.bigDictionary = bigDictionary;
        this.hugeDictionary = hugeDictionary;
        this.wordCalculationService = wordCalculationService;
    }

    @Cacheable("validWords")
    public SolvleDTO getValidWords(String restrictionString, int length, String wordList, int numSuggestions) {

        log.debug("Searching for words of length {}", length);

        // parse the string to identify required letters and position exclusions
        WordRestrictions wordRestrictions = new WordRestrictions(restrictionString);

        Dictionary dictionary = switch (wordList) {
            case "simple" -> length == 5 ? simpleDictionary : defaultDictionary;
            case "big" -> bigDictionary;
            case "huge" -> hugeDictionary;
            default -> defaultDictionary;
        };

        // step 1: find all the valid words in our dictionary for this restriction string
        SortedSet<Word> containedWords = dictionary.wordsBySize().get(length).parallelStream()
                .filter(w -> isValidWord(w, wordRestrictions))
                .collect(Collectors.toCollection(() -> new TreeSet<>()));

        // step 2: calculate how many words in the valid word set contain each character
        Map<Character, LongAdder> characterCounts = wordCalculationService.calculateCharacterCounts(containedWords);

        // step 3: calculate the frequency score for each word in the available list.
        Set<WordFrequencyScore> wordFrequencyScores = wordCalculationService
                .calculateViableWords(containedWords, characterCounts, containedWords.size(), 0, Math.min(numSuggestions, MAX_RESULT_LIST_SIZE));

        // step 4: calculate words containing the most letters present, regardless off letter requirements
        Set<WordFrequencyScore> fishingWords = containedWords.size() < 1 ? new HashSet<>() : wordCalculationService
                .calculateFishingWords(dictionary.wordsBySize().get(length), characterCounts, containedWords.size(), FISHING_WORD_SIZE, wordRestrictions.requiredLetters());

        log.info("Found {} length {} matches for {}", containedWords.size(), length, restrictionString);
        return new SolvleDTO(wordFrequencyScores, fishingWords, containedWords.size(), characterCounts);
    }

    /**
     * Returns true if this word could be a valid word for the provided letters
     *
     * @param wordRestrictions The Word describing available and required letters
     * @return
     */
    public boolean isValidWord(Word word, WordRestrictions wordRestrictions) {

        //if required letters are missing
        if (!word.letters().keySet().containsAll(wordRestrictions.requiredLetters())) {
            return false;
        }

        //check if any required positions are missing
        for (Map.Entry<Integer, Character> entry : wordRestrictions.letterPositions().entrySet()) {
            if (word.word().charAt(entry.getKey() - 1) != entry.getValue()) {
                return false;
            }
        }

        //check if any excluded positions are present
        for (var entry : wordRestrictions.positionExclusions().entrySet()) {
            if (entry.getValue().contains(word.word().charAt(entry.getKey() - 1))) {
                return false;
            }
        }

        //then check if all letters in this word are available in the restrictions
        return wordRestrictions.word().letters().keySet().containsAll(word.letters().keySet());

    }
}
