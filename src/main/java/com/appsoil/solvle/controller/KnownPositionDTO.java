package com.appsoil.solvle.controller;

import com.appsoil.solvle.data.WordFrequencyScore;

import java.util.Set;

public record KnownPositionDTO(String position, Set<String> words, Set<WordFrequencyScore> recommendations){
}
