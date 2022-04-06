package com.appsoil.solvle.wordler;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Data
@RequiredArgsConstructor
public class Dictionary {
    private final Set<Word> words;
}
