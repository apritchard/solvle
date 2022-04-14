package com.appsoil.solvle.wordler;

import lombok.Data;

@Data
public class WordleResult implements Comparable<WordleResult> {
    private final String word;
    private final Double freqScore;

    public WordleResult(Word word, WordleData wordleData) {
        this.word = word.getWord();
        this.freqScore = calculateFreqScore(word, wordleData);
    }

    private Double calculateFreqScore(Word word, WordleData wordleData) {
        return word.getLetters().entrySet().stream()
                .mapToDouble(c ->
                        (wordleData.getWordsWithCharacter().get(c.getKey()).doubleValue()) /
                                wordleData.getTotalWords().doubleValue())
                .sum();
    }

    @Override
    public int compareTo(WordleResult other) {
        int freqS = other.freqScore.compareTo(freqScore);
        if(freqS == 0) {
            return word.compareTo(other.word);
        } else {
            return freqS;
        }
    }
}
