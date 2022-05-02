package com.appsoil.solvle.service.solvers;

import com.appsoil.solvle.controller.SolvleDTO;
import com.appsoil.solvle.data.Word;
import com.appsoil.solvle.data.WordRestrictions;
import com.appsoil.solvle.service.SolvleService;
import com.appsoil.solvle.service.WordCalculationConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Attempts to guess the top fishing word until only 2 solutions remain
 * and then picks the top one.
 */
public class FishingSolver implements Solver {

    private SolvleService solvleService;

    public FishingSolver(SolvleService solvleService) {
        this.solvleService = solvleService;
    }

    @Override
    public List<String> solve(Word word, Set<Word> viable, Set<Word> fishing, String firstWord) {
        WordRestrictions wordRestrictions = new WordRestrictions("abcdefghijklmnopqrstuvwxyz");
        WordCalculationConfig config = WordCalculationConfig.getOptimalMeanConfig();
        List<String> solution = new ArrayList<>();

        //get the first guess
        SolvleDTO guess = solvleService.getValidWords(wordRestrictions, viable, fishing, config);

        // if there are more than two solutions, guess a fishing word
        while(guess.totalWords() > 2) {
            //guess the top fishing word
            String currentGuess = guess.fishingWords().stream().findFirst().get().word();

            //if we've already tried this word, we have to start guessing for real
            if(solution.contains(currentGuess)){
                break;
            }
            solution.add(currentGuess);

            //if it's correct, bail
            if(currentGuess.equals(word.word())) {
                return solution;
            }

            //generate new restrictions and try again
            wordRestrictions = WordRestrictions.generateRestrictions(word, new Word(currentGuess), wordRestrictions);
            guess = solvleService.getValidWords(wordRestrictions, viable, fishing, config);
        }


        //if we guessed wrong
        while(guess.totalWords() > 0) {
            String currentGuess = guess.wordList().stream().findFirst().get().word();
            solution.add(currentGuess);

            //if it's correct, bail
            if(currentGuess.equals(word.word())) {
                return solution;
            }

            //generate new restrictions and try again
            wordRestrictions = WordRestrictions.generateRestrictions(word, new Word(currentGuess), wordRestrictions);
            guess = solvleService.getValidWords(wordRestrictions, viable, fishing, config);
        }

        throw new IllegalStateException("Failed to find word " + word.word());
    }
}
