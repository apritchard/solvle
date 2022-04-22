package com.appsoil.solvle.data;

public record WordFrequencyScore(String word, Double freqScore) implements Comparable<WordFrequencyScore> {
    @Override
    public int compareTo(WordFrequencyScore other) {
        int freqS = other.freqScore.compareTo(freqScore);
        if (freqS == 0) {
            return word.compareTo(other.word);
        } else {
            return freqS;
        }
    }
}
