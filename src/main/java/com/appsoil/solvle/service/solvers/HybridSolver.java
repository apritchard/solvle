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
public class HybridSolver implements Solver {

    private SolvleService solvleService;

    private int fishingThreshold;
    private int requiredThreshold;
    private int positionThreshold;
    WordCalculationConfig config = WordCalculationConfig.OPTIMAL_MEAN;

    public HybridSolver(SolvleService solvleService, int fishingThreshold, int requiredThreshold, int positionThreshold) {
        this.solvleService = solvleService;
        this.fishingThreshold = fishingThreshold;
        this.requiredThreshold = requiredThreshold;
        this.positionThreshold = positionThreshold;
    }

    @Override
    public List<String> solve(Word word, Set<Word> viable, Set<Word> fishing, String firstWord) {
        WordRestrictions wordRestrictions = new WordRestrictions("abcdefghijklmnopqrstuvwxyz");
        List<String> solution = new ArrayList<>();

        if(firstWord != null && firstWord.length() == word.word().length()) {
            solution.add(firstWord);
            wordRestrictions = WordRestrictions.generateRestrictions(word, new Word(firstWord), wordRestrictions);
        }

        //get the first guess
        SolvleDTO guess = solvleService.getValidWords(wordRestrictions, viable, fishing, config);
//        SolvleDTO guess = solvleService.getValidWords(wordRestrictions, viable, fishing);

        // keep fishing as long as:
        // * there are at least fishingThreshold words to choose from
        // OR
        // * there are more than requiredThreshold letters required AND
        // * there are fewer than positionThreshold known positions
        while(guess.totalWords() > fishingThreshold && wordRestrictions.positionExclusions().keySet().size() < requiredThreshold && wordRestrictions.letterPositions().keySet().size() < positionThreshold) {
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
//            guess = solvleService.getValidWords(wordRestrictions, viable, fishing);
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
//            guess = solvleService.getValidWords(wordRestrictions, viable, fishing);
        }

        throw new IllegalStateException("Failed to find word " + word.word());
    }
}
