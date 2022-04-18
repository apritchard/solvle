package com.appsoil.solvle.service;

import com.appsoil.solvle.controller.WordleDTO;
import com.appsoil.solvle.wordler.*;
import com.appsoil.solvle.wordler.Dictionary;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Qualifier("defaultDictionary")
    @Autowired
    Dictionary defaultDictionary;

    @Qualifier("wordleDictionary")
    @Autowired
    Dictionary wordleDictionary;

    @Qualifier("bigDictionary")
    @Autowired
    Dictionary bigDictionary;

    @Qualifier("hugeDictionary")
    @Autowired
    Dictionary hugeDictionary;

    @Autowired
    WordCalculationService wordCalculationService;

    private final int MAX_RESULT_LIST_SIZE = 100;
    private final int FISHING_WORD_SIZE = 10;

    @Cacheable("validWords")
    public WordleDTO getValidWords(String wordleString, int length, String wordleDict, int numSuggestions) {

        log.debug("Searching for words of length {}", length);

        // parse the string to identify required letters and position exclusions
        WordleInfo wordleInfo = new WordleInfo(wordleString);

        Dictionary dictionary = switch(wordleDict) {
            case "wordle" -> length == 5 ? wordleDictionary : defaultDictionary;
            case "big" -> bigDictionary;
            case "huge" -> hugeDictionary;
            default -> defaultDictionary;
        };

        // step 1: find all the valid words in our dictionary for this wordle string
        SortedSet<Word> containedWords = dictionary.getWordsBySize().get(length).parallelStream()
                .filter(w -> isValidWord(w, wordleInfo))
                .collect(Collectors.toCollection(() -> new TreeSet<>()));

        // step 2: calculate how many words in the valid word set contain each character
        Map<Character, LongAdder> characterCounts = wordCalculationService.calculateCharacterCounts(containedWords);

        // step 3: calculate the frequency score for each word in the available list.
        Set<WordFrequencyScore> wordFrequencyScores = wordCalculationService
                .calculateWordleResults(containedWords, characterCounts, containedWords.size(), Math.min(numSuggestions,MAX_RESULT_LIST_SIZE));

        // step 4: calculate words containing the most letters present, regardless off letter requirements
        Set<WordFrequencyScore> fishingWords = containedWords.size() < 1 ? new HashSet<>() : wordCalculationService
                .calculateFishingWords(dictionary.getWordsBySize().get(length), characterCounts, containedWords.size(), FISHING_WORD_SIZE, wordleInfo.getRequiredLetters());

        log.info("Found {} length {} matches for {}", containedWords.size(), length, wordleString);
        return new WordleDTO(wordFrequencyScores, fishingWords, containedWords.size(), characterCounts);
    }

    /**
     * Returns true if this word could be a valid wordle answer for the provided letters
     * @param wordleInfo The Word describing available and required wordle letters
     * @return
     */
    public boolean isValidWord(Word word, WordleInfo wordleInfo) {

        //if required letters are missing
        if(!word.getLetters().keySet().containsAll(wordleInfo.getRequiredLetters())) {
            return false;
        }

        //check if any required positions are missing
        for(Map.Entry<Integer, Character> entry : wordleInfo.getLetterPositions().entrySet()) {
            if(word.getWord().charAt(entry.getKey() - 1) != entry.getValue()) {
                return false;
            }
        }

        //check if any excluded positions are present
        for(var entry : wordleInfo.getPositionExclusions().entrySet()) {
            if(entry.getValue().contains(word.getWord().charAt(entry.getKey() -1))) {
                return false;
            }
        }

        //then check if all letters in this word are available in the wordleInfo
        return wordleInfo.getLetters().keySet().containsAll(word.getLetters().keySet());

    }
}
