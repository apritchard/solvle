package com.appsoil.solvle.service.solvers;

import com.appsoil.solvle.controller.SolvleDTO;
import com.appsoil.solvle.data.Word;
import com.appsoil.solvle.data.WordFrequencyScore;
import com.appsoil.solvle.data.WordRestrictions;
import com.appsoil.solvle.service.SolvleService;
import com.appsoil.solvle.service.WordCalculationConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Guesses the top fishing word until few enough words remain that we can
 * start using the best partition word. Use this until fishing threshold is
 * hit and then switch to top valid word.
 */
public class RemainingSolver implements Solver {

    private SolvleService solvleService;

    WordCalculationConfig config;

    public RemainingSolver(SolvleService solvleService,WordCalculationConfig config) {
        this.solvleService = solvleService;
        this.config = config;
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

        // start by fishing until we are below the partition threshold
        while(guess.totalWords() > config.partitionThreshold() && guess.totalWords() > config.fishingThreshold()) {
            //guess the top fishing word
            WordFrequencyScore currentGuess = guess.fishingWords().stream().findFirst().get();

            //if we've already tried this word, we're stuck in a loop, switch to viable words
            if(solution.contains(currentGuess.word())){
                break;
            }
            solution.add(currentGuess.word());

            //if it's correct, bail
            if(currentGuess.word().equals(word.word())) {
                return solution;
            }

            //generate new restrictions and try again
            wordRestrictions = WordRestrictions.generateRestrictions(word, new Word(currentGuess.word(), currentGuess.naturalOrdering()), wordRestrictions);
            guess = solvleService.getValidWords(wordRestrictions, viable, fishing, config);
        }

        // partition until we are below the fishing threshold as long as there are words in the partition set
        while(guess.totalWords() > config.fishingThreshold() && guess.bestWords()!= null && !guess.bestWords().isEmpty()) {
            WordFrequencyScore currentGuess = guess.bestWords().stream().findFirst().get();
            if(solution.contains(currentGuess.word())){
                break;
            }
            solution.add(currentGuess.word());

            //if it's correct, bail
            if(currentGuess.word().equals(word.word())) {
                return solution;
            }
            wordRestrictions = WordRestrictions.generateRestrictions(word, new Word(currentGuess.word(), currentGuess.naturalOrdering()), wordRestrictions);
            guess = solvleService.getValidWords(wordRestrictions, viable, fishing, config);
        }

        // pick out of the viable set
        while(guess.totalWords() > 0) {
            WordFrequencyScore currentGuess = guess.wordList().stream().findFirst().get();
            solution.add(currentGuess.word());
            //if it's correct, bail
            if(currentGuess.word().equals(word.word())) {
                return solution;
            }

            //generate new restrictions and try again
            wordRestrictions = WordRestrictions.generateRestrictions(word, new Word(currentGuess.word(), currentGuess.naturalOrdering()), wordRestrictions);
            guess = solvleService.getValidWords(wordRestrictions, viable, fishing, config);
        }

        throw new IllegalStateException("Failed to find word " + word.word());
    }
}
