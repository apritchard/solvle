package com.appsoil.solvle.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Word implements Serializable, Comparable<Word> {

    private int order; //used to 'remember' alphabetical order for faster sorting

    private String word;
    private Map<Character, Integer> letters;

    public Word(String word, int order){
        this.word = word;
        this.letters = new HashMap<>();

        for(Character c : word.toCharArray()){
            if(letters.containsKey(c)){
                letters.put(c, letters.get(c) + 1);
            } else {
                letters.put(c,1);
            }
        }
        this.order = order;
    }

    public Word(String word) {
        this(word, 0);
    }

    public String word() {
        return word;
    }

    public Map<Character, Integer> letters() {
        return letters;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }

    @Override
    public int compareTo(Word other) {
        if(order == 0 || other.order == 0) {
            return word.compareTo(other.word);
        } else {
            return order - other.order;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word1 = (Word) o;
        return word.equals(word1.word);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word);
    }

    public int getLength(){
        return word.length();
    }

}
