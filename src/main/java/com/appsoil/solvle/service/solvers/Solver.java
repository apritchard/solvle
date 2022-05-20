package com.appsoil.solvle.service.solvers;

import com.appsoil.solvle.data.Word;
import com.appsoil.solvle.data.WordRestrictions;

import java.util.List;
import java.util.Set;

public interface Solver {
     List<String> solve(Word word, Set<Word> viable, Set<Word> fishing, String firstWord);
     List<String> solve(Word word, Set<Word> viable, Set<Word> fishing, Word firstWord, WordRestrictions wordRestrictions);
     List<String> solve(Word word, Set<Word> viable, Set<Word> fishing, WordRestrictions wordRestrictions);
}
