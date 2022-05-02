package com.appsoil.solvle.service.solvers;

import com.appsoil.solvle.data.Word;

import java.util.List;
import java.util.Set;

public interface Solver {
     List<String> solve(Word word, Set<Word> viable, Set<Word> fishing, String firstWord);
}
