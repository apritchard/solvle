package com.appsoil.solvle.service;

import com.appsoil.solvle.wordler.Dictionary;
import com.appsoil.solvle.wordler.Word;
import com.appsoil.solvle.wordler.WordleInfo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
@Log4j2
public class SolvleService {

    @Autowired
    Dictionary dictionary;

    public Set<String> getValidWords(String wordleString, int length) {
        WordleInfo wordleInfo = new WordleInfo(wordleString);
        SortedSet<String> containedWords = dictionary.getWords().stream()
                .filter(w -> w.getLength() == length)
                .filter(w -> isValidWord(w, wordleInfo))
                .map(Word::getWord)
                .collect(Collectors.toCollection(() -> new TreeSet<>()));
        log.info("Found " + containedWords.size() + " viable matches.");
        return containedWords;
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
