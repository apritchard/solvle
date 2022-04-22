package com.appsoil.solvle.data;

import java.util.Map;
import java.util.Set;

public record Dictionary(Map<Integer, Set<Word>> wordsBySize) {
}
