package com.appsoil.solvle.wordler;

import lombok.Data;

import java.util.Set;

@Data
public class Dictionary {
    private final Set<Word> words;
}
