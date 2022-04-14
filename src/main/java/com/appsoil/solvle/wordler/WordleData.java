package com.appsoil.solvle.wordler;

import lombok.Data;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

@Data
public class WordleData {
    private Map<Character, LongAdder> wordsWithCharacter = new ConcurrentHashMap<>();
    private LongAdder totalWords = new LongAdder();
    private Set<WordleResult> words;

    public WordleData(Set<Word> words) {
        words.parallelStream().forEach(word -> {
            totalWords.increment();
            word.getLetters().forEach((key, value) -> {
                wordsWithCharacter.computeIfAbsent(key, k -> new LongAdder()).increment();
            });
        });

        this.words = words.parallelStream()
                .map(word -> new WordleResult(word, this))
                .collect(Collectors.toCollection(() -> new TreeSet<>()));
    }

}
