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
    public WordleDTO getValidWords(@PathVariable String wordleString,
                                            @RequestParam int wordLength,
                                            @RequestParam(defaultValue="default") String wordleDict,
                                            @RequestParam(defaultValue="100") int size) {
        return solvleService.getValidWords(wordleString.toLowerCase(), wordLength, wordleDict, size);
    }
}
