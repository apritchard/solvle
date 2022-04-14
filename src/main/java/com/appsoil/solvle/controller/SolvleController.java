package com.appsoil.solvle.controller;

import com.appsoil.solvle.service.SolvleService;
import com.appsoil.solvle.wordler.WordleResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/solvle")
public class SolvleController {

    @Autowired
    private SolvleService solvleService;

    @GetMapping("/{wordleString}")
    public List<WordleResult> getValidWords(@PathVariable String wordleString,
                                            @RequestParam int wordLength,
                                            @RequestParam(defaultValue="default") String wordleDict,
                                            @RequestParam(defaultValue="50") int size) {
        List<WordleResult> resultList = solvleService.getValidWords(wordleString.toLowerCase(), wordLength, wordleDict);
        return resultList.subList(0, Math.min(size, resultList.size()));
    }
}
