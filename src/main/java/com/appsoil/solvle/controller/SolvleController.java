package com.appsoil.solvle.controller;

import com.appsoil.solvle.service.SolvleService;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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
                                   @RequestParam(defaultValue = "simple") String wordList,
                                   @RequestParam(defaultValue = "100") int numSuggestions) {


        logRequestsCount();
        return solvleService.getValidWords(wordRestrictions.toLowerCase(), wordLength, wordList, numSuggestions);
    }

    private static void logRequestsCount() {
        LocalDateTime now = LocalDateTime.now();
        if (requestsSinceLoading++ % 1000 == 0 || MINUTES.between(lastRequestLogTime, now) > 29) {
            double requestPerHour = (double) requestsSinceLoading * 60 / (double) MINUTES.between(startTime, now);
            log.info("{} requests made since {} ({} per hour)", requestsSinceLoading, lastRequestLogTime, requestPerHour);
            lastRequestLogTime = now;
        }
    }
}
