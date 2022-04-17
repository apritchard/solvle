package com.appsoil.solvle.service;

import com.appsoil.solvle.wordler.Word;
import com.appsoil.solvle.wordler.WordFrequencyScore;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

@Service
public class WordCalculationService {

    /**
     * Creates a map representing the number of words in the provided set that contain
     * each letter present in any word in the set. A word that contains the letter
     * multiple times will only be counted once.
     * @param words
     * @return
     */
    public Map<Character, LongAdder> calculateCharacterCounts(Set<Word> words) {
        Map<Character, LongAdder> counts = new ConcurrentHashMap<>(26);
        words.parallelStream().forEach(word -> {
            word.getLetters().forEach((key, value) -> {
                counts.computeIfAbsent(key, k -> new LongAdder()).increment();
            });
        });
        return counts;
    }

    /**
     * Sets the frequency score for every word in the provided words list and returns the top n words
     * based on score, as specified by the sizeLimit.
     *
     * The frequency score will be between 0.0 and n, where n is the length of words in this set.
     * It reflects how many letters in a given word are contained within words in the viable word list, on average.
     * For example, a score of 5.0 means that an average of 5 letters in a given candidate word are present in
     * the words in the viable list (this would occur if your word is a transposition of the only viable word,
     * like ALERT and LATER). 0.0 means that none of the letters in the sample word are found in any viable words.
     *
     * @param words List of words that will have frequency scores associated with them
     * @param characterCounts Total count of how many words in the viable words list contain each letter
     * @param viableWordsCount number of viable words, used as the divisor in the frequency calculation
     * @param sizeLimit Maximum number of results to return
     * @return
     */
    public Set<WordFrequencyScore> calculateWordleResults(Set<Word> words, Map<Character, LongAdder> characterCounts, int viableWordsCount, int sizeLimit) {
        return words.parallelStream()
                .map(word -> new WordFrequencyScore(word.getWord(), calculateFreqScore(word, characterCounts, (double)viableWordsCount)))
                .sorted()
                .limit(sizeLimit)
                .collect(Collectors.toCollection(() -> new TreeSet<>()));
    }

    /**
     * Identical to {@link #calculateWordleResults(Set, Map, int, int)} but with the addition of a requiredLetters set.
     * This set refers to letters that are required in viable words, and they will be excluded from the frequency
     * score calculation for fishing words.
     *
     * This means that fishing words will only be rated based on letters that you don't already know. The maximum
     * score for a fishing word will be n-x, where n is the length of the word and x is the size of the requiredLetters set.
     *
     * For example, if you know 2 of the letters from a 5-letter word, the maximum score for a fishing word is 3.0, which means
     * that all 3 of its remaining letters are present in every single available word. For example, if the only available word
     * is ZEBRA and you already know Z and B, any word that contains E, R, and A will score 3.0.
     * @param allWords
     * @param characterCounts
     * @param viableWordsCount
     * @param sizeLimit
     * @param requiredLetters
     * @return
     */
    public Set<WordFrequencyScore> calculateFishingWords(Set<Word> allWords, Map<Character, LongAdder> characterCounts, int viableWordsCount, int sizeLimit, Set<Character> requiredLetters) {
        Map<Character, LongAdder> newMap = characterCounts.entrySet().stream()
                .filter(entrySet -> !requiredLetters.contains(entrySet.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, ConcurrentHashMap::new));

        return calculateWordleResults(allWords, newMap, viableWordsCount, sizeLimit);
    }


    private Double calculateFreqScore(Word word, Map<Character, LongAdder> wordsWithCharacter, Double totalWords) {
        if(totalWords < 1) {
            return 0.0;
        }
        return word.getLetters().entrySet().stream()
                .mapToDouble(c ->
                        (wordsWithCharacter.containsKey(c.getKey()) ? wordsWithCharacter.get(c.getKey()).doubleValue() : 0) / totalWords)
                .sum();
    }
}
