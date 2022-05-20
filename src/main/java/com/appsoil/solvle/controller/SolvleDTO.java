package com.appsoil.solvle.controller;

import com.appsoil.solvle.data.WordFrequencyScore;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;

/**
 * Data used to display solvle data on the front end
 * @param restrictionString     The string representation of current knowledge about the solution
 * @param wordList              A list of viable solutions, priorized by letter match criteria
 * @param fishingWords          A list of all possible guesses disregarding restrictions, prioritized by letter match criteria
 * @param bestWords             A list of guesses that minimize the average wordList for possible current viable words
 * @param totalWords            Total number of viable words (returned wordList includes only top x words)
 * @param wordsWithCharacter    A map of how many words containing each character (or total character match if bias enabled)
 * @param knownPositions        A list of common word groupings available for this position
 */
public record SolvleDTO(
        String restrictionString,
        Set<WordFrequencyScore> wordList,
        Set<WordFrequencyScore> fishingWords,
        Set<WordFrequencyScore> bestWords,
        int totalWords,
        Map<Character, LongAdder> wordsWithCharacter,
        List<KnownPositionDTO> knownPositions
) {

    public static SolvleDTO appendRestrictionString(String restrictionString, SolvleDTO o) {
        return new SolvleDTO(restrictionString, o.wordList, o.fishingWords, o.bestWords, o.totalWords, o.wordsWithCharacter, o.knownPositions);
    }
}
