package com.appsoil.solvle.wordler;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

@Data
public class WordleResult implements Comparable<WordleResult> {
    private final String word;
    private final Double freqScore;

    public WordleResult(Word word, WordleData wordleData) {
        this.word = word.getWord();
        this.freqScore = calculateFreqScore(word, wordleData.getWordsWithCharacter(), wordleData.getTotalWords().doubleValue());
    }

    public WordleResult(Word word, Map<Character, LongAdder> wordsWithCharacter, Double totalWords) {
        this.word = word.getWord();
        this.freqScore = calculateFreqScore(word, wordsWithCharacter, totalWords);
    }

    private Double calculateFreqScore(Word word, Map<Character, LongAdder> wordsWithCharacter, Double totalWords) {
        return word.getLetters().entrySet().stream()
                .mapToDouble(c ->
                        (wordsWithCharacter.containsKey(c.getKey()) ? wordsWithCharacter.get(c.getKey()).doubleValue() : 0) / totalWords)
                .sum();
    }

    @Override
    public int compareTo(WordleResult other) {
        int freqS = other.freqScore.compareTo(freqScore);
        if (freqS == 0) {
            return word.compareTo(other.word);
        } else {
            return freqS;
        }
    }
}
