package com.appsoil.solvle.controller;

import com.appsoil.solvle.wordler.WordleResult;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

@Data
public class WordleDTO {
    private final List<WordleResult> wordList;
    private final List<WordleResult> fishingWords;
    private final int totalWords;
    private final Map<Character, LongAdder> wordsWithCharacter;
}
