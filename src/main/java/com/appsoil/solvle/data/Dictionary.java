package com.appsoil.solvle.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record Dictionary(Map<Integer, Set<Word>> wordsBySize) {

    public Dictionary(Map<Integer, Set<Word>> wordsBySize) {
        //manually pre-alphabetize to save time sorting later
        Map<Integer, Set<Word>> newDict = new HashMap<>();
        wordsBySize.forEach((length, words) -> {
            Set<Word> newWords = new HashSet<>(words.size());
            int i = 1;
            for(Word word : words) {
                word.setOrder(i++);
                newWords.add(word);
            }
            newDict.put(length, newWords);
        });
        this.wordsBySize = newDict;
    }

}
