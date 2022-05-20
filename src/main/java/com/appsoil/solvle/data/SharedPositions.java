package com.appsoil.solvle.data;

import com.appsoil.solvle.controller.KnownPositionDTO;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record SharedPositions(Map<KnownPosition, Set<Word>> knownPositions, Map<KnownPosition, Set<WordFrequencyScore>> recommendations, int largestSet) {

    public SharedPositions withRecommendations(Map<KnownPosition, Set<WordFrequencyScore>> recommendations) {
        return new SharedPositions(knownPositions, recommendations, largestSet);
    }
    public SharedPositions(Map<KnownPosition, Set<Word>> knownPositions) {
        this(knownPositions, null);
    }

    public SharedPositions(Map<KnownPosition, Set<Word>> knownPositions, Map<KnownPosition, Set<WordFrequencyScore>> recommendations) {
        this(knownPositions, recommendations, knownPositions.values().stream().mapToInt(Set::size).max().orElse(0));
    }

    public List<KnownPositionDTO> toKnownPositionDTOList(int threshold) {
        return sortedPositionStream().map(entry -> {
            if(entry.getValue().isEmpty() || entry.getValue().size() < threshold) {
                return null;
            }
            //build the string description
            int wordLength = entry.getValue().stream().findFirst().get().getLength();
            StringBuilder sb = new StringBuilder();
            Map<Integer, Character> pos = entry.getKey().pos();
            for(int i = 1; i <= wordLength; i++) {
                sb.append(pos.getOrDefault(i, '_'));
            }

            //create a list of words
            Set<String> words = entry.getValue().stream().map(Word::word).collect(Collectors.toSet());

            return new KnownPositionDTO(sb.toString(), words, recommendations == null ? new HashSet<>() : recommendations.get(entry.getKey()));
        }).filter(Objects::nonNull).toList();
    }

    public Stream<Map.Entry<KnownPosition, Set<Word>>> sortedPositionStream() {
        return knownPositions.entrySet().stream().sorted((esA, esB) -> {
            int keyCompare = esA.getKey().compareTo(esB.getKey());
            if(esA.getKey().getShared() == esB.getKey().getShared()) {
                int numWords = esB.getValue().size() - esA.getValue().size();
                if(numWords != 0) {
                    return numWords;
                }
            }
            return keyCompare;
        });
    }
}
