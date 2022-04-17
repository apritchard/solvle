package com.appsoil.solvle.controller;

import com.appsoil.solvle.wordler.WordFrequencyScore;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;

public record WordleDTO(Set<WordFrequencyScore> wordList,
                        Set<WordFrequencyScore> fishingWords, int totalWords,
                        Map<Character, LongAdder> wordsWithCharacter) {
}
