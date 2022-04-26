package com.appsoil.solvle.service;

import com.appsoil.solvle.controller.SolvleDTO;
import com.appsoil.solvle.data.Dictionary;
import com.appsoil.solvle.data.Word;
import com.appsoil.solvle.data.WordFrequencyScore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest(classes={SolvleService.class, SolvleServiceTest.SolvleTestConfiguration.class, WordCalculationService.class})
public class SolvleServiceTest {

    @TestConfiguration
    public static class SolvleTestConfiguration {
        @Bean(name="simpleDictionary")
        Dictionary getTestDictionary() {
            Set<Word> words = Stream.of("aaaaa", "aaaab", "aaabc", "aabcd", "abcde", "bcdea").map(Word::new).collect(Collectors.toSet());
            Dictionary dictionary = new Dictionary(Map.of(5, words));
            return dictionary;
        }

        @Bean(name={"bigDictionary", "hugeDictionary"})
        Dictionary getOtherDictionary() {
            return new Dictionary(Map.of(5, Set.of(new Word("foo"))));
        }
    }

    @Autowired SolvleService solvleService;

    @ParameterizedTest
    @CsvSource(value = {
            "a | aaaaa",
            "ab | aaaaa,aaaab",
            "abc | aaaaa,aaaab,aaabc",
            "abcd | aaaaa,aaaab,aaabc,aabcd",
            "abcde | aaaaa,aaaab,aaabc,aabcd,abcde,bcdea"
    }, delimiter = '|')
    void getValidWords_lettersAvailable_matchesWords(String restrictionString, String matches) {
        Set<String> expectedWords = Arrays.stream(matches.split(",")).collect(Collectors.toSet());
        SolvleDTO result = solvleService.getValidWords(restrictionString, 5, "simple", 100);

        Assertions.assertEquals(expectedWords, result.wordList().stream().map(WordFrequencyScore::word).collect(Collectors.toSet()));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "a5bcde | aaaaa,bcdea",
            "a1bcde | aaaaa,aaaab,aaabc,aabcd,abcde",
            "a15bcde | aaaaa",
            "a14bcde | aaaaa,aaaab",
            "ab1cde | bcdea",
            "a1b2c3d4e5 | abcde",
            "a1b3c2de | none "
    }, delimiter = '|')
    void getValidWords_requiredPosition_matchesWords(String restrictionString, String matches) {
        Set<String> expectedWords = Arrays.stream(matches.split(",")).filter(s -> !s.equals("none")).collect(Collectors.toSet());
        SolvleDTO result = solvleService.getValidWords(restrictionString, 5, "simple", 100);

        Assertions.assertEquals(expectedWords, result.wordList().stream().map(WordFrequencyScore::word).collect(Collectors.toSet()));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "a!1bcde | bcdea",
            "a!3bcde | aabcd,abcde,bcdea",
            "a!45bcde | aaabc,aabcd,abcde",
            "a!4b!2cde | aaabc,aabcd,bcdea",
            "a!5b | aaaab",
            "a!15bcde | none"
    }, delimiter = '|')
    void getValidWords_excludedPosition_matchesWords(String restrictionString, String matches) {
        Set<String> expectedWords = Arrays.stream(matches.split(",")).filter(s -> !s.equals("none")).collect(Collectors.toSet());
        SolvleDTO result = solvleService.getValidWords(restrictionString, 5, "simple", 100);

        Assertions.assertEquals(expectedWords, result.wordList().stream().map(WordFrequencyScore::word).collect(Collectors.toSet()));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "a1!3bcde | aabcd,abcde",
            "a4!5bcde | aaaab",
            "a1!3b2cde | abcde",
            "a1!3b!2cde | aabcd",
            "a3!4bc | aaabc",
            "a1!3b3cde | aabcd",
            "a1!2b3cde | none"
    }, delimiter = '|')
    void getValidWords_excludeAndRequired_matchesWords(String restrictionString, String matches) {
        Set<String> expectedWords = Arrays.stream(matches.split(",")).filter(s -> !s.equals("none")).collect(Collectors.toSet());
        SolvleDTO result = solvleService.getValidWords(restrictionString, 5, "simple", 100);

        Assertions.assertEquals(expectedWords, result.wordList().stream().map(WordFrequencyScore::word).collect(Collectors.toSet()));
    }

}
