package com.appsoil.solvle.controller;

import com.appsoil.solvle.data.WordFrequencyScore;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;

public record SolvleDTO(Set<WordFrequencyScore> wordList,
                        Set<WordFrequencyScore> fishingWords, int totalWords,
                        Map<Character, LongAdder> wordsWithCharacter) {
}
