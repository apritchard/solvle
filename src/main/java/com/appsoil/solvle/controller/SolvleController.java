package com.appsoil.solvle.controller;

import com.appsoil.solvle.service.SolvleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/solvle")
public class SolvleController {

    @Autowired
    private SolvleService solvleService;

    @CrossOrigin
    @GetMapping("/{wordleString}")
    public WordleDTO getValidWords(@PathVariable String wordleString,
                                            @RequestParam int wordLength,
                                            @RequestParam(defaultValue="default") String wordleDict,
                                            @RequestParam(defaultValue="100") int numSuggestions) {
        return solvleService.getValidWords(wordleString.toLowerCase(), wordLength, wordleDict, numSuggestions);
    }
}
