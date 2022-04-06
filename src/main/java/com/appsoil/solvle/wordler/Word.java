package com.appsoil.solvle.wordler;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class Word implements Serializable, Comparable<Word> {

    private String word;
    private Map<Character, Integer> letters = new HashMap<>();

    public Word(String word){
        this.word = word;
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
