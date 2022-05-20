package com.appsoil.solvle.data;

import java.util.List;
import java.util.Objects;

public record PlayOut(String word, double average, String counts, List<List<String>> failures) implements Comparable<PlayOut> {
    @Override
    public int compareTo(PlayOut o) {
        if(average == o.average) {
            return word.compareTo(o.word);
        } else {
            return Double.compare(average, o.average);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayOut that = (PlayOut) o;
        return word.equals(that.word);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word);
    }
}
