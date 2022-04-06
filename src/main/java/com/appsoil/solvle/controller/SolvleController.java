package com.appsoil.solvle.controller;

import com.appsoil.solvle.service.SolvleService;
import com.appsoil.solvle.wordler.Word;
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

    @GetMapping("/{wordleString}")
    public Set<String> getValidWords(@PathVariable String wordleString) {
        return solvleService.getValidWords(wordleString, 5); //default to 5 length for now
    }
}
