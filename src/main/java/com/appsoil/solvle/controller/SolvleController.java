package com.appsoil.solvle.controller;

import com.appsoil.solvle.data.PlayOut;
import com.appsoil.solvle.data.Word;
import com.appsoil.solvle.service.SolvleService;
import com.appsoil.solvle.service.WordCalculationConfig;
import com.appsoil.solvle.service.solvers.RemainingSolver;
import com.appsoil.solvle.service.solvers.Solver;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.time.temporal.ChronoUnit.MINUTES;

@RestController
@RequestMapping("/solvle")
@Log4j2
public class SolvleController {

    private final SolvleService solvleService;
    private final CacheManager cacheManager;

    private static long requestsSinceLoading;
    private static final LocalDateTime startTime = LocalDateTime.now();
    private static LocalDateTime lastRequestLogTime = LocalDateTime.now();
    private static int MAX_PARTITION = 3000;

    public SolvleController(SolvleService solvleService, CacheManager cacheManager) {
        this.solvleService = solvleService;
        this.cacheManager = cacheManager;
    }

    @GetMapping("/{wordRestrictions}")
    public SolvleDTO getWordAnalysis(@PathVariable String wordRestrictions,
                                               @RequestParam(defaultValue= "5") int wordLength,
                                               @RequestParam(defaultValue = "simple") String wordList,
                                               @RequestParam(defaultValue = "1") double rightLocationMultiplier,
                                               @RequestParam(defaultValue = "1") double uniquenessMultiplier,
                                               @RequestParam(defaultValue = "0.0") double viableWordPreference,
                                               @RequestParam(defaultValue = "0") double locationAdjustmentScale,
                                               @RequestParam(defaultValue = "0") double uniqueAdjustmentScale,
                                               @RequestParam(defaultValue = "0") double viableWordAdjustmentScale,
                                               @RequestParam(defaultValue = "1") double vowelMultiplier,
                                               @RequestParam(defaultValue = "0") double rutBreakMultiplier,
                                               @RequestParam(defaultValue = "0") int rutBreakThreshold,
                                               @RequestParam(defaultValue = "50") int partitionThreshold,
                                               @RequestParam(defaultValue = "false") boolean hardMode
                                   ) {

        LocalDateTime start = LocalDateTime.now();
        logRequestsCount(start);
        WordCalculationConfig wordCalculationConfig =
                new WordCalculationConfig(rightLocationMultiplier, uniquenessMultiplier, Math.min(partitionThreshold, MAX_PARTITION), viableWordPreference)
                        .withFineTuning(locationAdjustmentScale, uniqueAdjustmentScale, viableWordAdjustmentScale, vowelMultiplier)
                        .withHardMode(hardMode)
                        .withRutBreak(rutBreakMultiplier, rutBreakThreshold);
        log.info("Valid words requested with configuration {}", wordCalculationConfig);
        checkMemory();
        log.info(cacheManager.getCacheNames());
        SolvleDTO result = solvleService.getWordAnalysis(wordRestrictions.toLowerCase(), wordLength, wordList, wordCalculationConfig);
        log.info("Valid words for {} took {}", wordRestrictions, Duration.between(start, LocalDateTime.now()));
        return SolvleDTO.appendRestrictionString(wordRestrictions, result);
    }

    @GetMapping("/{wordRestrictions}/{wordToScore}")
    public WordScoreDTO getWordScore(@PathVariable String wordRestrictions,
                                     @PathVariable String wordToScore,
                                     @RequestParam(defaultValue = "simple") String wordList,
                                     @RequestParam(defaultValue = "1") double rightLocationMultiplier,
                                     @RequestParam(defaultValue = "1") double uniquenessMultiplier,
                                     @RequestParam(defaultValue = "0.0") double viableWordPreference,
                                     @RequestParam(defaultValue = "0") double locationAdjustmentScale,
                                     @RequestParam(defaultValue = "0") double uniqueAdjustmentScale,
                                     @RequestParam(defaultValue = "0") double viableWordAdjustmentScale,
                                     @RequestParam(defaultValue = "1") double vowelMultiplier,
                                     @RequestParam(defaultValue = "0") double rutBreakMultiplier,
                                     @RequestParam(defaultValue = "0") int rutBreakThreshold,
                                     @RequestParam(defaultValue = "50") int partitionThreshold,
                                     @RequestParam(defaultValue = "false") boolean hardMode
                                     ) {
        LocalDateTime start = LocalDateTime.now();
        logRequestsCount(start);
        WordCalculationConfig wordCalculationConfig =
                new WordCalculationConfig(rightLocationMultiplier, uniquenessMultiplier, Math.min(partitionThreshold, MAX_PARTITION), viableWordPreference)
                        .withFineTuning(locationAdjustmentScale, uniqueAdjustmentScale, viableWordAdjustmentScale, vowelMultiplier)
                        .withHardMode(hardMode)
                        .withRutBreak(rutBreakMultiplier, rutBreakThreshold);
        log.info("Word Score requested for {} with configuration {}", wordToScore, wordCalculationConfig);
        WordScoreDTO result = solvleService.getScore(wordRestrictions.toLowerCase(), wordToScore.toLowerCase(), wordList, wordCalculationConfig);
        log.info("Word Score for {} took {}", wordToScore, Duration.between(start, LocalDateTime.now()));

        return result;
    }

    @GetMapping("/{wordRestrictions}/playout")
    public Set<PlayOut> playOutSolution(@PathVariable String wordRestrictions,
                                        @RequestParam(defaultValue= "5") int wordLength,
                                        @RequestParam(defaultValue = "simple") String wordList,
                                        @RequestParam(defaultValue = "1") double rightLocationMultiplier,
                                        @RequestParam(defaultValue = "1") double uniquenessMultiplier,
                                        @RequestParam(defaultValue = "0.0") double viableWordPreference,
                                        @RequestParam(defaultValue = "0") double locationAdjustmentScale,
                                        @RequestParam(defaultValue = "0") double uniqueAdjustmentScale,
                                        @RequestParam(defaultValue = "0") double viableWordAdjustmentScale,
                                        @RequestParam(defaultValue = "1") double vowelMultiplier,
                                        @RequestParam(defaultValue = "0") double rutBreakMultiplier,
                                        @RequestParam(defaultValue = "0") int rutBreakThreshold,
                                        @RequestParam(defaultValue = "50") int partitionThreshold,
                                        @RequestParam(defaultValue = "false") boolean hardMode,
                                        @RequestParam(defaultValue = "0") int guess
    ) {


        logRequestsCount();
        WordCalculationConfig wordCalculationConfig =
                new WordCalculationConfig(rightLocationMultiplier, uniquenessMultiplier, Math.min(partitionThreshold, MAX_PARTITION), viableWordPreference)
                        .withFineTuning(locationAdjustmentScale, uniqueAdjustmentScale, viableWordAdjustmentScale, vowelMultiplier)
                        .withHardMode(hardMode)
                        .withRutBreak(rutBreakMultiplier, rutBreakThreshold);
        log.info("Playout requested with configuration {}", wordCalculationConfig);
        Set<PlayOut> result = solvleService.playOutSolutions(wordRestrictions.toLowerCase(), wordLength, wordList, wordCalculationConfig, guess);
        return result;
    }

    @GetMapping("/solve/{solution}")
    public List<String> solvePuzzle(@PathVariable String solution,
                                    @RequestParam(defaultValue = "") String firstWord,
                                    @RequestParam(defaultValue = "simple") String wordList,
                                    @RequestParam(defaultValue = "1") double rightLocationMultiplier,
                                    @RequestParam(defaultValue = "1") double uniquenessMultiplier,
                                    @RequestParam(defaultValue = "0.0") double viableWordPreference,
                                    @RequestParam(defaultValue = "0") double locationAdjustmentScale,
                                    @RequestParam(defaultValue = "0") double uniqueAdjustmentScale,
                                    @RequestParam(defaultValue = "0") double viableWordAdjustmentScale,
                                    @RequestParam(defaultValue = "1") double vowelMultiplier,
                                    @RequestParam(defaultValue = "0") double rutBreakMultiplier,
                                    @RequestParam(defaultValue = "0") int rutBreakThreshold,
                                    @RequestParam(defaultValue = "50") int partitionThreshold,
                                    @RequestParam(defaultValue = "false") boolean hardMode
                                    ) {
        logRequestsCount();
        WordCalculationConfig wordCalculationConfig =
                new WordCalculationConfig(rightLocationMultiplier, uniquenessMultiplier, Math.min(partitionThreshold, MAX_PARTITION), viableWordPreference)
                        .withFineTuning(locationAdjustmentScale, uniqueAdjustmentScale, viableWordAdjustmentScale, vowelMultiplier)
                        .withHardMode(hardMode)
                        .withRutBreak(rutBreakMultiplier, rutBreakThreshold);
        log.info("Solution requested for [{}] with first word [{}] and configuration {}", solution, firstWord, wordCalculationConfig);
        Solver solver = new RemainingSolver(solvleService, wordCalculationConfig);
        return solvleService.solveWord(solver, new Word(solution.toLowerCase()), firstWord.toLowerCase(), wordList);
    }

    private void logRequestsCount() {
        logRequestsCount(LocalDateTime.now());
    }

    private static synchronized void logRequestsCount(LocalDateTime now) {
        if (requestsSinceLoading++ % 1000 == 0 || MINUTES.between(lastRequestLogTime, now) > 29) {
            double requestPerHour = (double) requestsSinceLoading * 60 / (double) MINUTES.between(startTime, now);
            log.info("{} requests made since {} ({} per hour)", requestsSinceLoading, startTime, requestPerHour);
            lastRequestLogTime = now;
        }
    }

    private void checkMemory() {
        long maxMem = Runtime.getRuntime().maxMemory();
        long totalMem = Runtime.getRuntime().totalMemory();
        long freeMem = Runtime.getRuntime().freeMemory();

        Cache validWords = cacheManager.getCache("validWords");
        int validWordsSize = ((Map)validWords.getNativeCache()).size();

        log.info("max({}) total({}) free({}) cachedWords({})", maxMem, totalMem, freeMem, validWordsSize);

        if(validWordsSize > 50000) {
            log.warn("clearing solvle cache");
            cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
        }
    }

}
