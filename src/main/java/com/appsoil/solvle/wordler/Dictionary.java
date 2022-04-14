package com.appsoil.solvle.wordler;

import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class Dictionary {
    private final Map<Integer, Set<Word>> wordsBySize;
}
