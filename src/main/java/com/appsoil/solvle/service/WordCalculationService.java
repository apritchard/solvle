package com.appsoil.solvle.service;

import com.appsoil.solvle.data.Word;
import com.appsoil.solvle.data.WordFrequencyScore;
import com.appsoil.solvle.data.WordRestrictions;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WordCalculationService {

    private final double rightLocationMultiplier;
    private final double uniquenessMultiplier;
    private final boolean useHarmonic;
    private final int permutationThreshold;
    private final double viableWordPreference;

    public WordCalculationService(WordCalculationConfig config) {
        this.rightLocationMultiplier = config.rightLocationMultiplier();
        this.uniquenessMultiplier = config.uniquenessMultiplier();
        this.useHarmonic = config.useHarmonic();
        this.permutationThreshold = config.partitionThreshold();
        this.viableWordPreference = config.viableWordPreference();
    }

    /**
     * Finds all words in the provided set that match a given set of word restrictions.
     * @param wordSet
     * @param wordRestrictions
     * @return
     */
    public Set<Word> findMatchingWords(Set<Word> wordSet, WordRestrictions wordRestrictions) {
        return wordSet.parallelStream()
                .filter(w -> isValidWord(w, wordRestrictions))
                .collect(Collectors.toSet());
    }

    /**
     * Returns true if this word could be a valid word for the provided letters
     *
     * @param wordRestrictions The Word describing available and required letters
     * @return
     */
    public boolean isValidWord(Word word, WordRestrictions wordRestrictions) {

        //if required letters are missing
        if (!word.letters().keySet().containsAll(wordRestrictions.requiredLetters())) {
            return false;
        }

        //check if any required positions are missing
        for (Map.Entry<Integer, Character> entry : wordRestrictions.letterPositions().entrySet()) {
            if (word.word().charAt(entry.getKey() - 1) != entry.getValue()) {
                return false;
            }
        }

        //check if any excluded positions are present
        for (var entry : wordRestrictions.positionExclusions().entrySet()) {
            if (entry.getValue().contains(word.word().charAt(entry.getKey() - 1))) {
                return false;
            }
        }

        //then check if all letters in this word are available in the restrictions
        return wordRestrictions.word().letters().keySet().containsAll(word.letters().keySet());
    }

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
            word.letters().forEach((key, value) -> {
                counts.computeIfAbsent(key, k -> new LongAdder()).increment();
            });
        });
        return counts;
    }

    /**
     * Creates a map representing the number of words in the provided set that contain
     * each letter, organized by position. A word that contains the letter multiple times
     * will only be counted once.
     * @param words
     * @return
     */
    public Map<Integer, Map<Character, LongAdder>> calculateCharacterCountsByPosition(Set<Word> words) {
        Map<Integer, Map<Character, LongAdder>> countsByPos = new ConcurrentHashMap<>();
        words.parallelStream().forEach(word -> {
            for(int i = 0; i < word.word().length(); i++) {
                countsByPos.computeIfAbsent((i+1), k -> new ConcurrentHashMap<>(26))
                        .computeIfAbsent(word.word().charAt(i), k -> new LongAdder()).increment();
            }
        });
        return countsByPos;
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
    public Set<WordFrequencyScore> calculateViableWords(Set<Word> words, Map<Character, LongAdder> characterCounts, int viableWordsCount, int requiredCharCount, int sizeLimit) {
        return words.parallelStream()
                .map(word -> new WordFrequencyScore(word.word(), calculateFreqScore(word, characterCounts, viableWordsCount, word.getLength() - requiredCharCount)))
                .sorted()
                .limit(sizeLimit)
                .collect(Collectors.toCollection(() -> new TreeSet<>()));
    }

    public Set<WordFrequencyScore> calculateViableWordsByPosition(Set<Word> words, Map<Integer, Map<Character, LongAdder>> characterCounts, Set<Word> containedWords, int requiredCharCount, int sizeLimit, WordRestrictions wordRestrictions) {
        return words.parallelStream()
                .map(word -> new WordFrequencyScore(word.word(), calculateFreqScoreByPosition(word, characterCounts, containedWords, word.getLength() - requiredCharCount, wordRestrictions)))
                .sorted()
                .limit(sizeLimit)
                .collect(Collectors.toCollection(() -> new TreeSet<>()));
    }

    /**
     * Identical to {@link #calculateViableWords(Set, Map, int, int, int)} but with the addition of a requiredLetters set.
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

        return calculateViableWords(allWords, newMap, viableWordsCount, requiredLetters.size(), sizeLimit);
    }

    public Set<WordFrequencyScore> calculateFishingWordsByPosition(Set<Word> allWords, Map<Integer, Map<Character, LongAdder>> characterCounts, Set<Word> containedWords, int sizeLimit, WordRestrictions wordRestrictions) {

        AtomicInteger removals = new AtomicInteger(0);
        double length = characterCounts.keySet().size();

        characterCounts.forEach((pos, v) -> {
            //remove characters from the positions you know they should or shouldn't be
            if(wordRestrictions.positionExclusions().containsKey(pos)) {
                wordRestrictions.positionExclusions().get(pos).forEach(c -> {
                    if(v.containsKey(c)) {
                        v.get(c).reset();
                        removals.incrementAndGet();
                    }
                });
            }
            Character knownPos = wordRestrictions.letterPositions().get(pos);
            if(knownPos != null && v.containsKey(knownPos)) {
                v.get(knownPos).reset();
                removals.incrementAndGet();
            }
        });

        return calculateViableWordsByPosition(allWords, characterCounts, containedWords, (int)(removals.doubleValue() / length), sizeLimit, wordRestrictions);
    }


    /**
     * Calculates letter frequency. A value of 1.0 represents that all letters in the solution set contain exactly the same
     * letters as the word under consideration, while a value of 0.6 would represent that 3/5 letters in this word are found
     * in every word of the contained set.
     * @param word The Word to score
     * @param wordsWithCharacter A map of how many words contain each character
     * @param totalWords The number of words in the viable words set
     * @param maxScore The number of letters available for scoring. For example, if we already know 2 letters of a 5-letter word, the max score is 3
     * @return
     */
    protected Double calculateFreqScore(Word word, Map<Character, LongAdder> wordsWithCharacter, int totalWords, int maxScore) {
        if(totalWords < 1 || maxScore < 1) {
            return 0.0;
        }
        return word.letters().entrySet().stream()
                .mapToDouble(c ->
                        (wordsWithCharacter.containsKey(c.getKey()) ? wordsWithCharacter.get(c.getKey()).doubleValue() : 0) /
                                ((double)totalWords * maxScore))
                .sum();
    }

    /**
     * Calculates letter frequency by position and adds additional weighting bonuses as defined by this
     * service's configuration. Return value of 1.0 represents that every letter in the word exists in the same
     * positions in every word in the set. Values greater than 1.0 may be returned as a result of bias multipliers.
     * @param word
     * @param wordsWithCharacter
     * @param containedWords
     * @param maxScore
     * @param wordRestrictions
     * @return
     */
    protected Double calculateFreqScoreByPosition(Word word, Map<Integer, Map<Character, LongAdder>> wordsWithCharacter, Set<Word> containedWords, int maxScore, WordRestrictions wordRestrictions) {
        if(containedWords.size() < 1 || maxScore < 1) {
            return 0.0;
        }

        double totalScore = 0.0;

        for(int i = 0; i < word.word().length(); i++) {
            char c = word.word().charAt(i);
            for(int j = 0; j < word.word().length(); j++) {
                double locationBonus = (i == j) ? rightLocationMultiplier : 1;
                double uniqueBonus = (word.letters().get(c) < 2) && !wordRestrictions.requiredLetters().contains(c) ? uniquenessMultiplier : 1;
                double numerator = wordsWithCharacter.get(j+1).containsKey(c) ? harmonic(wordsWithCharacter.get(j+1).get(c).intValue()): 0;
                 totalScore += ((numerator * locationBonus * uniqueBonus)
                         / (containedWords.size() * maxScore * rightLocationMultiplier)); //divide by max score * bonuses to normalize scores closer to 100%
            }
        }

        if(totalScore > 0 && containedWords.contains(word)) {
            totalScore += viableWordPreference; //tiebreaker toward potential solutions
        }

        return totalScore;
    }

    /**
     * Calculates a list of words that will minimize the average remaining words in the viable word set if chosen.
     * Combines the top current viable words and top current fishing words in order to seed the search space, so all
     * suggested words will exist in the viable or fishing word list.
     *
     * Will do nothing if the number of containedWords is greater than the permutationThreshold set for the config on
     * this instance, because this operation is expensive.
     *
     * @param wordRestrictions The current word restrictions that produced the provided containedWords.
     * @param containedWords The list of currently viable words matching the starting restrictions.
     * @param wordFrequencyScores The calculated top words.
     * @param fishingWords The calculated top fishing words.
     * @return
     */
    public Set<WordFrequencyScore> calculateRemainingWords(WordRestrictions wordRestrictions, Set<Word> containedWords, Set<WordFrequencyScore> wordFrequencyScores, Set<WordFrequencyScore> fishingWords) {
        if(containedWords.size() <= permutationThreshold) {
            //get a pool of words taken from the top fishing and valid list
            Set<Word> wordPool = Stream.of(wordFrequencyScores, fishingWords)
                    .flatMap(Set::stream)
                    .map(WordFrequencyScore::word)
                    .distinct()
                    .map(Word::new)
                    .collect(Collectors.toSet());
//            Set<String> viableStrings = wordFrequencyScores.stream().map(WordFrequencyScore::word).collect(Collectors.toSet()); //to give tiny bonus points
            return wordsByRemainingGuesses(wordRestrictions, containedWords, wordPool);
        } else {
            return new HashSet<>();
        }
    }

    /**
     * Scores the words in the wordPool based on how well they partition the set of contained words. Each
     * score represents what percentage of the contained set will be eliminated on average (plus any viableWordPreference bonus)
     * @param startingRestrictions Initial restrictions that produced the set of containedWords
     * @param containedWords Current set of valid words we're trying to reduce
     * @param wordPool List of words to consider
     * @return
     */
    public Set<WordFrequencyScore> wordsByRemainingGuesses(WordRestrictions startingRestrictions, Set<Word> containedWords, Set<Word> wordPool) {
        if(containedWords.size() <= 2) {
            //50/50 shot either way
            return containedWords.stream().map(word -> new WordFrequencyScore(word.word(), 1.0 / containedWords.size())).collect(Collectors.toSet());
        }

        Set<WordFrequencyScore> scores = new TreeSet<>();

        Map<Word, DescriptiveStatistics> statSummary = new HashMap<>();

        //for each word in the pool, create a new wordRequirements as if that word had been picked for each solution
        //  then calculate how many remaining words are left and average the results
        for(Word word : wordPool) {
            DescriptiveStatistics stats = getPartitionStatsForWord(startingRestrictions, containedWords, word);
            if(stats != null ) {
                statSummary.put(word, stats);
            }
        }

        statSummary.forEach((k, v) ->
                scores.add(new WordFrequencyScore(k.word(),
                        ((1.0 - (v.getMean() / containedWords.size()))
                                + (containedWords.contains(k) ? viableWordPreference : 0))))); // add tiny bonus to viable words so they are prioritized
        return scores;
    }

    public DescriptiveStatistics getPartitionStatsForWord(WordRestrictions startingRestrictions, Set<Word> containedWords, Word word) {
        DescriptiveStatistics stats = null;
        for(Word solution : containedWords) {
            WordRestrictions newRestrictions = WordRestrictions.generateRestrictions(solution, word, startingRestrictions);
            Set<Word> newWords = findMatchingWords(containedWords, newRestrictions);
            if(!newWords.isEmpty()) {
                if(stats == null) {
                    stats = new DescriptiveStatistics();
                }
                stats.addValue(newWords.size());
            }
        }
        return stats;
    }

    /**
     * Returns the nth value of the harmonic series. Used to lower
     * the importance of prioritizing letter frequency in favor
     * of letter variety.
     * @param n Number of words with letter
     * @return The nth value of the harmonic series. For example: 1(1), 2(1.5), 3(1.833), 4(2.083)...
     */
    private double harmonic(int n) {
        if(!useHarmonic) {
            return n;
        }
        double sum = 0.0;
        for(int i = 1; i <= n; i++) {
            sum += 1.0 / (double)n;
        }
        return sum;
    }

}
