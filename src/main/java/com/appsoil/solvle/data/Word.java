package com.appsoil.solvle.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public record Word(String word, Map<Character, Integer> letters) implements Serializable, Comparable<Word> {

    public Word(String word){
        this(word, new HashMap<>());
        for(Character c : word.toCharArray()){
            if(letters.containsKey(c)){
                letters.put(c, letters.get(c) + 1);
            } else {
                letters.put(c,1);
            }
        }
    }

    @Override
    public int compareTo(Word other) {
        if(other.toString().length() == this.toString().length()){
            return this.toString().compareTo(other.toString());
        }
        return other.toString().length() - this.toString().length();
    }

    public int getLength(){
        return word.length();
    }

}
