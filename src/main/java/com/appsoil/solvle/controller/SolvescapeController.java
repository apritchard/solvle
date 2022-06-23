package com.appsoil.solvle.controller;

import com.appsoil.solvle.service.SolvescapeService;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/solvescape")
@Log4j2
public class SolvescapeController {

    private SolvescapeService solvescapeService;

    public SolvescapeController(SolvescapeService solvescapeService) {
        this.solvescapeService = solvescapeService;
    }

    @GetMapping("/{availableLetters}")
    public Map<Integer, List<String>> getWordAnalysis(@PathVariable String availableLetters) {
        log.info("Anagrams requested for " + availableLetters);
        var ret = solvescapeService.getAnagrams(availableLetters.toLowerCase());
        log.info("Returning " + ret + " for " + availableLetters);
        return ret;
    }

}
