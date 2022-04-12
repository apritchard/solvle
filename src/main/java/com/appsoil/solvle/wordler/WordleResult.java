package com.appsoil.solvle.wordler;

import lombok.Data;

@Data
public class WordleResult implements Comparable<WordleResult> {
    private final Word word;
    private final Double charScore;
    private final Double freqScore;

    public WordleResult(Word word, WordleData wordleData) {
        this.word = word;
        this.charScore = calculateCharScore(wordleData);
        this.freqScore = calculateFreqScore(wordleData);
    }

    private Double calculateFreqScore(WordleData wordleData) {
        return word.getLetters().entrySet().stream()
                .mapToDouble(c ->
                        (wordleData.getWordsWithCharacter().get(c.getKey()).doubleValue()) /
                                wordleData.getTotalWords().doubleValue())
                .sum();
    }

    private Double calculateCharScore(WordleData wordleData) {
        return word.getLetters().entrySet().stream()
                .mapToDouble(c ->
                        (wordleData.getCharacterCounts().get(c.getKey()).doubleValue()) /
                                wordleData.getTotalWords().doubleValue())
                .sum();
    }

    @Override
    public int compareTo(WordleResult other) {
        int freqS, charS, text;

        freqS = other.freqScore.compareTo(freqScore);
        if(freqS == 0) {
            charS = other.charScore.compareTo(charScore);
            if(charS ==0) {
                return word.compareTo(other.word);
            } else {
                return charS;
            }
        } else {
            return freqS;
        }
    }
}
