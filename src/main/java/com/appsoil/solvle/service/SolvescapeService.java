package com.appsoil.solvle.service;

import com.appsoil.solvle.data.Dictionary;
import com.appsoil.solvle.data.Word;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class SolvescapeService {

    private final Dictionary bigDictionary;

    public SolvescapeService(@Qualifier("bigDictionary") Dictionary bigDictionary) {
        this.bigDictionary = bigDictionary;
    }

    public Map<Integer, List<String>> getAnagrams(String availableLetters) {
        Word available = new Word(availableLetters);

        Map<Integer, List<String>> options = new ConcurrentSkipListMap<>((i, j) -> Integer.compare(j, i));

        IntStream.rangeClosed(3, availableLetters.length()).parallel().forEach(i -> options.put(i,
                bigDictionary.wordsBySize().get(i).stream()
                        .filter(w -> contains(available, w))
                        .map(Word::word)
                        .sorted().collect(Collectors.toList())));

        return options;
    }

    public boolean contains(Word available, Word other){
        Map<Character, Integer> otherLetters = other.letters();
        Map<Character, Integer> availableLetters = available.letters();
        for(Character c : otherLetters.keySet()){
            if(!availableLetters.containsKey(c)) {
                return false;
            } else if (availableLetters.get(c) < otherLetters.get(c)){
                return false;
            }
        }
        return true;
    }
}
