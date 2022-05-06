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
 * Simply guesses the top valid word until finding the correct one
 */
public class TopWordSolver implements Solver {

    private SolvleService solvleService;

    public TopWordSolver(SolvleService solvleService) {
        this.solvleService = solvleService;
    }

    @Override
    public List<String> solve(Word word, Set<Word> viable, Set<Word> fishing, String firstWord) {
        WordRestrictions wordRestrictions = new WordRestrictions("abcdefghijklmnopqrstuvwxyz");
        WordCalculationConfig config = WordCalculationConfig.OPTIMAL_MEAN;
        List<String> solution = new ArrayList<>();

        //get the first guess
        SolvleDTO guess = solvleService.getValidWords(wordRestrictions, viable, fishing, config);

        //guess the top word until we find it
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
