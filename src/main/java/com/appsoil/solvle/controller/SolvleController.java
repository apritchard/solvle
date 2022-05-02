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

    public SolvleController(SolvleService solvleService) {
        this.solvleService = solvleService;
    }

    @CrossOrigin
    @GetMapping("/{wordRestrictions}")
    public SolvleDTO getValidWords(@PathVariable String wordRestrictions,
                                   @RequestParam int wordLength,
                                   @RequestParam(defaultValue = "simple") String wordList) {


        logRequestsCount();
        SolvleDTO result = solvleService.getValidWords(wordRestrictions.toLowerCase(), wordLength, wordList, WordCalculationConfig.getOptimalMeanConfig());
        return SolvleDTO.appendRestrictionString(wordRestrictions, result);
    }

    @GetMapping("/solve/{solution}")
    public List<String> solvePuzzle(@PathVariable String solution,
                                    @RequestParam(defaultValue = "200") int permutationThreshold,
                                    @RequestParam(defaultValue = "") String firstWord) {

        Solver solver = new RemainingSolver(solvleService, getConfig(permutationThreshold));
        return solvleService.solveWord(solver, new Word(solution), firstWord);
    }

    private WordCalculationConfig getConfig(int permutationThreshold) {
        return WordCalculationConfig.withPartitionThreshold(Math.min(permutationThreshold, 200), WordCalculationConfig.getOptimalMeanConfig());
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
