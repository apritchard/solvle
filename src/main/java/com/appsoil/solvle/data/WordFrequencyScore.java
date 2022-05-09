package com.appsoil.solvle.data;

public record WordFrequencyScore(int naturalOrdering, String word, double freqScore) implements Comparable<WordFrequencyScore> {
    @Override
    public int compareTo(WordFrequencyScore other) {
        if (freqScore == other.freqScore) {
            return naturalOrdering - other.naturalOrdering;
        } else {
            return Double.compare(other.freqScore, freqScore);
        }
    }

    // these should always have a unique natural ordering when created, so ignore everything else for faster compares
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return naturalOrdering == ((WordFrequencyScore) o).naturalOrdering;
    }

    @Override
    public int hashCode() {
        return naturalOrdering;
    }
}
