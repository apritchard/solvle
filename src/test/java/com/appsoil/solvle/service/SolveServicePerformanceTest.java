package com.appsoil.solvle.service;

import com.appsoil.solvle.controller.SolvleController;
import com.appsoil.solvle.data.Dictionary;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * Note: This class was created specifically to compare the use of stream vs. parallelStream in
 * the wordCalculationService, but I've removed the redundant methods in that service that were
 * used to run the comparative tests in this class. Leaving them commented out here in case
 * I want to revisit in the future.
 */
@SpringBootTest
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class SolveServicePerformanceTest {

    @Autowired
    private WordCalculationService wordCalculationService;
    @Autowired
    private Dictionary defaultDictionary;
    @Autowired
    private SolvleController solvleController;

    private static SolvleController controller;
    private static WordCalculationService staticWordService;
    private static Dictionary dictionary;
    private static Map<Character, LongAdder> fullDictionarycharacters;

    private static final Random random = new Random();
    private List<String> restrictionStrings = new ArrayList<>();

    @Test
    public void executeJmhRunner() throws RunnerException {
        //jmh benchmarks run outside the scope of this spring context, so set janky static variables they can reference
        controller = solvleController;
        staticWordService = wordCalculationService;
        dictionary = defaultDictionary;
        fullDictionarycharacters = staticWordService.calculateCharacterCounts(dictionary.wordsBySize().get(5));

        Options jmhRunnerOptions = new OptionsBuilder()
                // set the class name regex for benchmarks to search for to the current class
                .include("\\." + this.getClass().getSimpleName() + "\\.")
                .warmupIterations(2)
                .measurementIterations(3)
                .forks(0)
                .threads(1)
                .shouldDoGC(true)
                .shouldFailOnError(true)
                .resultFormat(ResultFormatType.JSON)
                .result("solvle-jmh.txt") // set this to a valid filename if you want reports
                .shouldFailOnError(true)
                .jvmArgs("-server")
                .build();

//        new Runner(jmhRunnerOptions).run();
    }

    @Setup
    public void setupRestrictionStrings() {
        restrictionStrings = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            for(int j = 0; j < 10; j++) {
                restrictionStrings.add(generateRandomRestrictionString(j, 1,1));
            }
        }
    }

    private static String generateRandomRestrictionString(int numUnavailableLetters, int numRequiredLetters, int numUnsureLetters) {
        String allLetters = "abcdefghijklmnopqrstuvwxyz";
        int charLoc = 0;
        String charToModify = "";
        for(int i = 0; i < numUnavailableLetters; i++) {
            charLoc = random.nextInt(allLetters.length());
            charToModify = allLetters.substring(charLoc, charLoc+1);
            allLetters = allLetters.replace(charToModify, "");
        }
        for(int i = 0; i < numUnsureLetters; i++) {
            charLoc = random.nextInt(allLetters.length());
            charToModify = allLetters.substring(charLoc, charLoc+1);
            allLetters = allLetters.replace(charToModify, charToModify + "!" + random.nextInt(1, 6));
        }
        for(int i = 0; i < numRequiredLetters; i++) {
            charLoc = random.nextInt(allLetters.length());
            charToModify = allLetters.substring(charLoc, charLoc+1);
            allLetters = allLetters.replace(charToModify, charToModify + random.nextInt(1, 6));
        }
        return allLetters;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void someBenchmarkMethod() {
        //  note - this test is useless with cacheing on - @todo create test profile or something to disable cacheing for performance test
        restrictionStrings.forEach(s -> controller.getValidWords(s, 5, "default", 100));
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void benchmarkCharacterCounts() {
        staticWordService.calculateCharacterCounts(dictionary.wordsBySize().get(5));
    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    public void benchmarkCharacterCountsSingleThread() {
//        staticWordService.calculateCharacterCountsSingleThread(dictionary.wordsBySize().get(5));
//    }
//
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void benchmarkRestrictionResults() {
        staticWordService.calculateViableWords(dictionary.wordsBySize().get(5), fullDictionarycharacters, dictionary.wordsBySize().get(5).size(), 0, 100 );
    }

//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    public void benchmarkRestrictionResultsSingleThread() {
//        staticWordService.calculateRestrictionResultsSingleThread(dictionary.wordsBySize().get(5), fullDictionarycharacters, dictionary.wordsBySize().get(5).size(), 100 );
//    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void benchmarkFishingWords() {
        staticWordService.calculateFishingWords(dictionary.wordsBySize().get(5), fullDictionarycharacters, dictionary.wordsBySize().get(5).size(), 100, Set.of('i') );
    }

//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    public void benchmarkFishingWordsMultiThread() {
//        staticWordService.calculateFishingWordsMultiThread(dictionary.wordsBySize().get(5), fullDictionarycharacters, dictionary.wordsBySize().get(5).size(), 100 , Set.of('i'));
//    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void benchmarkFreqScore() {
        dictionary.wordsBySize().get(5).forEach(word -> staticWordService.calculateFreqScore(word, fullDictionarycharacters, 0, dictionary.wordsBySize().get(5).size()));
    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    public void benchmarkFreqScoreMultiThread() {
//        dictionary.wordsBySize().get(5).forEach(word -> staticWordService.calculateFreqScoreMultiThread(word, fullDictionarycharacters, (double)dictionary.wordsBySize().get(5).size()));
//    }
}
