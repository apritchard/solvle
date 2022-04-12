package com.appsoil.solvle.controller;

import com.appsoil.solvle.service.SolvleService;
import com.appsoil.solvle.wordler.Word;
import com.appsoil.solvle.wordler.WordleData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/solvle")
public class SolvleController {

    @Autowired
    private SolvleService solvleService;

    @GetMapping("/{wordLength}/{wordleString}")
    public WordleData getValidWords(@PathVariable int wordLength, @PathVariable String wordleString) {
        return solvleService.getValidWords(wordleString.toLowerCase(), wordLength);
    }
}
