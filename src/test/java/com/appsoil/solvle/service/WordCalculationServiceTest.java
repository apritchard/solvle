package com.appsoil.solvle.service;

import com.appsoil.solvle.data.Word;
import com.appsoil.solvle.data.WordFrequencyScore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest(classes={WordCalculationService.class})
public class WordCalculationServiceTest {

    @Autowired
    WordCalculationService wordCalculationService;

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

        Set<WordFrequencyScore> scores = wordCalculationService.calculateViableWords(viableWords, counts, 1, 0, 100);

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

        Set<WordFrequencyScore> scores = wordCalculationService.calculateFishingWords(viableWords, counts, 1, 100, Set.of(requiredChar));

        Assertions.assertEquals(score, scores.stream().findFirst().get().freqScore());
    }

}
