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

    private final int MAX_RESULT_LIST_SIZE = 100;

    @Cacheable("validWords")
    public WordleDTO getValidWords(String wordleString, int length, String wordleDict, int size) {
        WordleInfo wordleInfo = new WordleInfo(wordleString);
        log.info("Searching for words of length {}", length);
        Dictionary dictionary = switch(wordleDict) {
            case "wordle" -> length == 5 ? wordleDictionary : defaultDictionary;
            case "big" -> bigDictionary;
            case "huge" -> hugeDictionary;
            default -> defaultDictionary;
        };
        SortedSet<Word> containedWords = dictionary.getWordsBySize().get(length).stream()
                .filter(w -> isValidWord(w, wordleInfo))
                .collect(Collectors.toCollection(() -> new TreeSet<>()));

        WordleData wordleData = new WordleData(containedWords);

        log.info("Found " + wordleData.getTotalWords() + " viable matches.");
        return new WordleDTO(wordleData.getWords().stream().limit(Math.min(size,MAX_RESULT_LIST_SIZE)).toList(), wordleData.getTotalWords().intValue(), wordleData.getWordsWithCharacter());
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
