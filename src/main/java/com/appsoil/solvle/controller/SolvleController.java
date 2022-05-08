package com.appsoil.solvle.controller;

import com.appsoil.solvle.data.Word;
import com.appsoil.solvle.service.SolvleService;
import com.appsoil.solvle.service.WordCalculationConfig;
import com.appsoil.solvle.service.solvers.RemainingSolver;
import com.appsoil.solvle.service.solvers.Solver;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.temporal.ChronoUnit.MINUTES;

@RestController
@RequestMapping("/solvle")
@Log4j2
public class SolvleController {

    private final SolvleService solvleService;

    private static long requestsSinceLoading;
    private static final LocalDateTime startTime = LocalDateTime.now();
    private static LocalDateTime lastRequestLogTime = LocalDateTime.now();
    private static int MAX_PARTITION = 200;

    public SolvleController(SolvleService solvleService) {
        this.solvleService = solvleService;
    }

    @CrossOrigin
    @GetMapping("/{wordRestrictions}")
    public SolvleDTO getValidWords(@PathVariable String wordRestrictions,
                                   @RequestParam(defaultValue= "5") int wordLength,
                                   @RequestParam(defaultValue = "simple") String wordList,
                                   @RequestParam(defaultValue = "4") double rightLocationMultiplier,
                                   @RequestParam(defaultValue = "9") double uniquenessMultiplier,
                                   @RequestParam(defaultValue = ".007") double viableWordPreference,
                                   @RequestParam(defaultValue = "50") int partitionThreshold
                                   ) {


        logRequestsCount();
        WordCalculationConfig wordCalculationConfig = new WordCalculationConfig(rightLocationMultiplier, uniquenessMultiplier,
                false, Math.min(partitionThreshold, MAX_PARTITION), 2, viableWordPreference);
        log.info("Valid words requested with configuration {}", wordCalculationConfig);
        SolvleDTO result = solvleService.getValidWords(wordRestrictions.toLowerCase(), wordLength, wordList, wordCalculationConfig);
        return SolvleDTO.appendRestrictionString(wordRestrictions, result);
    }

    @GetMapping("/{wordRestrictions}/{wordToScore}")
    public WordScoreDTO getWordScore(@PathVariable String wordRestrictions,
                                     @PathVariable String wordToScore,
                                     @RequestParam(defaultValue = "simple") String wordList,
                                     @RequestParam(defaultValue = "4") double rightLocationMultiplier,
                                     @RequestParam(defaultValue = "9") double uniquenessMultiplier,
                                     @RequestParam(defaultValue = ".007") double viableWordPreference,
                                     @RequestParam(defaultValue = "50") int partitionThreshold) {
        logRequestsCount();
        WordCalculationConfig wordCalculationConfig = new WordCalculationConfig(rightLocationMultiplier, uniquenessMultiplier,
                false, Math.min(partitionThreshold, MAX_PARTITION), 2, viableWordPreference);
        log.info("Word Score requested for {} with configuration {}", wordToScore, wordCalculationConfig);
        WordScoreDTO result = solvleService.getScore(wordRestrictions.toLowerCase(), wordToScore.toLowerCase(), wordList, wordCalculationConfig);
        return result;
    }

    @GetMapping("/solve/{solution}")
    public List<String> solvePuzzle(@PathVariable String solution,
                                    @RequestParam(defaultValue = "") String firstWord,
                                    @RequestParam(defaultValue = "4") double rightLocationMultiplier,
                                    @RequestParam(defaultValue = "9") double uniquenessMultiplier,
                                    @RequestParam(defaultValue = ".007") double viableWordPreference,
                                    @RequestParam(defaultValue = "50") int partitionThreshold
                                    ) {
        logRequestsCount();
        WordCalculationConfig wordCalculationConfig = new WordCalculationConfig(rightLocationMultiplier, uniquenessMultiplier,
                false, partitionThreshold, 2, viableWordPreference);
        log.info("Solution requested for [{}] with first word [{}] and configuration {}", solution, firstWord, wordCalculationConfig);
        Solver solver = new RemainingSolver(solvleService, wordCalculationConfig);
        return solvleService.solveWord(solver, new Word(solution.toLowerCase()), firstWord.toLowerCase());
    }

    private static void logRequestsCount() {
        LocalDateTime now = LocalDateTime.now();
        if (requestsSinceLoading++ % 1000 == 0 || MINUTES.between(lastRequestLogTime, now) > 29) {
            double requestPerHour = (double) requestsSinceLoading * 60 / (double) MINUTES.between(startTime, now);
            log.info("{} requests made since {} ({} per hour)", requestsSinceLoading, startTime, requestPerHour);
            lastRequestLogTime = now;
        }
    }
}
