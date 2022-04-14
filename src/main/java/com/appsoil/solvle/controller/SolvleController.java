package com.appsoil.solvle.controller;

import com.appsoil.solvle.service.SolvleService;
import com.appsoil.solvle.wordler.Word;
import com.appsoil.solvle.wordler.WordleData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/solvle")
public class SolvleController {

    @Autowired
    private SolvleService solvleService;

    @GetMapping("/{wordleString}")
    public WordleData getValidWords(@PathVariable String wordleString, @RequestParam int wordLength, @RequestParam boolean wordleDict) {
        return solvleService.getValidWords(wordleString.toLowerCase(), wordLength, wordleDict);
    }
}
