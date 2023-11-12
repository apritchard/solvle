package com.appsoil.solvle.service;

import com.appsoil.solvle.data.Dictionary;
import com.appsoil.solvle.data.Word;
import com.appsoil.solvle.data.WordFrequencyScore;
import com.appsoil.solvle.data.WordRestrictions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WordCalculationServiceTest {

    WordCalculationService wordCalculationService = new WordCalculationService(WordCalculationConfig.OPTIMAL_MEAN);

    private final Set<Word> ALL_LETTERS_WORD_SET = Stream.of("how", "quickly", "daft", "jumping", "zebras", "vex").map(Word::new).collect(Collectors.toSet());
    private final Set<Character> CHARACTERS_IN_TWO_WORDS = Set.of('u', 'e', 'a', 'i');
    private final Set<Character> ALPHABET_SET = "abcdefghijklmnopqrstuvwxyz".chars().mapToObj(c -> (char)c).collect(Collectors.toSet());

    @Test
    void calculateCharacterCounts_allLettersPresent_countsCorrectly() {
        Map<Character, LongAdder> counts = wordCalculationService.calculateCharacterCounts(ALL_LETTERS_WORD_SET);

        ALPHABET_SET.forEach(c -> {
            if(CHARACTERS_IN_TWO_WORDS.contains(c)) {
                Assertions.assertEquals(2, counts.get(c).intValue());
            } else {
                Assertions.assertEquals(1, counts.get(c).intValue());
            }
        });
    }

    @ParameterizedTest
    @CsvSource({
            "later, alert, 1.0",
            "abcde, abcdd, 0.8",
            "abcde, aaaaa, 0.2",
            "abcde, fghij, 0.0"})
    void calculateViableResults_singleSourceAndWord_returnsNumberOfSourceCharacters(String sourceWord, String viableWord, double score) {
        Map<Character, LongAdder> counts = wordCalculationService.calculateCharacterCounts(Set.of(new Word(sourceWord)));
        Set<Word> viableWords = Set.of(new Word(viableWord));

        Set<WordFrequencyScore> scores = wordCalculationService.calculateViableWords(viableWords, counts, 1, 0, 100, new HashMap<>());

        Assertions.assertEquals(score, scores.stream().findFirst().get().freqScore());
    }

    @ParameterizedTest
    @CsvSource({
            "later, alert, 1.0, a",
            "abcde, abcdd, 0.75, d",
            "abcde, aaaae, 0.25, e",
            "abcde, aaacd, 0.5, a",
            "abcde, aahij, 0.0, a",
            "abcde, fghij, 0.0, g"})
    void calculateFishingWords_singleSourceAndWord_excludesCharactersFromCount(String sourceWord, String viableWord, double score, Character requiredChar) {
        Map<Character, LongAdder> counts = wordCalculationService.calculateCharacterCounts(Set.of(new Word(sourceWord)));
        Set<Word> viableWords = Set.of(new Word(viableWord));

        Set<WordFrequencyScore> scores = wordCalculationService.calculateFishingWords(viableWords, counts, 1, 100, Set.of(requiredChar), new HashMap<>());

        Assertions.assertEquals(score, scores.stream().findFirst().get().freqScore());
    }

    @Test
    void calculateFishingWordsByPosition_priotizesNewLettersFollowedByPossibleSolutions() {

        Set<Word> words = Arrays.stream("AA, AB, AC, AD, BA, BB, BC, BD, CA, CB, CC, CD, DA, DB, DC, DD".split(", ")).map((String word) -> new Word(word)).collect(Collectors.toSet());
        Set<Word> allWords = getFormattedWords(words);

        Set<Word> viableWords = Stream.of("AA", "AC", "AD").map(Word::new).collect(Collectors.toSet());
        WordRestrictions restrictions = new WordRestrictions("A1B!2CD");
        //
        // solution: AA
        // first guess: AB
        // viable words: AA, AC, AD
        //
        // character counts: 1- a:3, 2- a:1, c:1, d:1
        //
        // ideal fishing words: CD, DC
        //

        var characterCounts = wordCalculationService.calculateCharacterCountsByPosition(viableWords);
        Set<WordFrequencyScore> scores = wordCalculationService.calculateFishingWordsByPosition(allWords, characterCounts, viableWords, 25, restrictions, new HashMap<>());

        Set<String> expected = Set.of("CD", "DC");
        System.out.println(scores.toString());

        Assertions.assertTrue(scores.stream().limit(2).map(WordFrequencyScore::word).collect(Collectors.toSet()).containsAll(expected), "Top solutions did not match expected");
    }

    @Test
    void calculateFishingWordsByCharacter2() {
        Set<Word> allWords = Stream.of("haver", "chivy", "bumph").map(Word::new).collect(Collectors.toSet());
        Set<Word> viableWords = Stream.of("hover", "mover", "homer", "joker", "poker", "rover", "boxer", "foyer", "roger").map(Word::new).collect(Collectors.toSet());
        WordRestrictions restrictions = new WordRestrictions("BE4!5FGHJKMO2PQR5UVXYZ".toLowerCase());

        var characterCounts = wordCalculationService.calculateCharacterCountsByPosition(viableWords);
        Set<WordFrequencyScore> scores = wordCalculationService.calculateFishingWordsByPosition(allWords, characterCounts, viableWords, 25, restrictions, new HashMap<>());

        System.out.println(scores.toString());
    }

    @Test
    void calculateFishingWordsByCharacter3() {
        Set<Word> allWords = Stream.of("haven", "hakim", "bumph").map(Word::new).collect(Collectors.toSet());
        Set<Word> viableWords = Stream.of("hover", "mover", "homer", "joker", "poker", "rover", "boxer", "roger").map(Word::new).collect(Collectors.toSet());
        WordRestrictions restrictions = new WordRestrictions("BE4!5FGHJKMO2PQR5!4UVXZ".toLowerCase());

        var characterCounts = wordCalculationService.calculateCharacterCountsByPosition(viableWords);
        Set<WordFrequencyScore> scores = wordCalculationService.calculateFishingWordsByPosition(allWords, characterCounts, viableWords, 25, restrictions, new HashMap<>());

        System.out.println(scores.toString());
    }

    private static Set<Word> getFormattedWords(Set<Word> words) {
        int size = words.stream().findFirst().get().getLength();
        var wordMap = Map.of(size, words);
        var d = new Dictionary(wordMap);
        return d.wordsBySize().get(size);
    }

}
