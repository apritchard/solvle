package com.appsoil.solvle.service;

import com.appsoil.solvle.data.*;
import com.appsoil.solvle.service.solvers.Solver;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WordCalculationService {

    private static final Logger log = LogManager.getLogger(WordCalculationService.class);

    private final double rightLocationMultiplier;
    private final double uniquenessMultiplier;
    private final boolean useHarmonic;
    private final int permutationThreshold;
    private final double viableWordPreference;
    private final double locationAdjustmentScale;
    private final double uniqueAdjustmentScale;
    private final double viableWordAdjustmentScale;
    private final double vowelAdjustment;
    private final double rutBreakMultiplier;
    private final int rutBreakThreshold;

    private static final Set<Character> vowels = Set.of('a', 'e', 'i', 'o', 'u');

    public WordCalculationService(WordCalculationConfig config) {
        this.rightLocationMultiplier = config.rightLocationMultiplier();
        this.uniquenessMultiplier = config.uniquenessMultiplier();
        this.useHarmonic = config.useHarmonic();
        this.permutationThreshold = config.partitionThreshold();
        this.viableWordPreference = config.viableWordPreference();
        this.locationAdjustmentScale = config.locationAdjustmentScale();
        this.uniqueAdjustmentScale = config.uniqueAdjustmentScale();
        this.viableWordAdjustmentScale = config.viableWordAdjustmentScale();
        this.vowelAdjustment = config.vowelMultiplier();
        this.rutBreakMultiplier = config.rutBreakMultiplier();
        this.rutBreakThreshold = config.rutBreakThreshold();
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
    public Set<WordFrequencyScore> calculateViableWords(Set<Word> words, Map<Character, LongAdder> characterCounts, int viableWordsCount, int requiredCharCount, int sizeLimit, Map<Character, DoubleAdder> positionBonus) {
        return words.parallelStream()
                .map(word -> new WordFrequencyScore(word.getOrder(), word.word(),
                        calculateFreqScore(word, characterCounts, viableWordsCount, word.getLength() - requiredCharCount, positionBonus)))
                .sorted()
                .limit(sizeLimit)
                .collect(Collectors.toCollection(() -> new TreeSet<>()));
    }

    public Set<WordFrequencyScore> calculateViableWordsByPosition(Set<Word> words, Map<Integer, Map<Character, LongAdder>> characterCounts, Set<Word> containedWords,
                                                                  int requiredCharCount, int sizeLimit, WordRestrictions wordRestrictions, Map<Character, DoubleAdder> positionBonus) {
        return words.parallelStream()
                .map(word -> new WordFrequencyScore(word.getOrder(), word.word(),
                        calculateFreqScoreByPosition(word, characterCounts, containedWords, word.getLength() - requiredCharCount, wordRestrictions, positionBonus)))
                .sorted()
                .limit(sizeLimit)
                .collect(Collectors.toCollection(() -> new TreeSet<>()));
    }

    /**
     * Identical to {@link #calculateViableWords(Set, Map, int, int, int, Map)} but with the addition of a requiredLetters set.
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
    public Set<WordFrequencyScore> calculateFishingWords(Set<Word> allWords, Map<Character, LongAdder> characterCounts, int viableWordsCount, int sizeLimit, Set<Character> requiredLetters, Map<Character, DoubleAdder> positionBonus) {
        return calculateViableWords(allWords,
                removeRequiredLettersFromCounts(characterCounts, requiredLetters),
                viableWordsCount, requiredLetters.size(), sizeLimit, positionBonus);
    }

    public Set<WordFrequencyScore> calculateFishingWordsByPosition(Set<Word> allWords, Map<Integer, Map<Character, LongAdder>> characterCounts, Set<Word> containedWords, int sizeLimit, WordRestrictions wordRestrictions, Map<Character, DoubleAdder> positionBonus) {
        return calculateViableWordsByPosition(allWords,
                removeRequiredLettersFromCountsByPosition(characterCounts, wordRestrictions),
                containedWords, wordRestrictions.letterPositions().keySet().size(), sizeLimit, wordRestrictions, positionBonus);
    }

    /**
     * Returns a new map that matches the provided map, but any entries for one of the required letters has been removed.
     * @param characterCounts
     * @param requiredLetters
     * @return
     */
    public Map<Character, LongAdder> removeRequiredLettersFromCounts(Map<Character, LongAdder> characterCounts, Set<Character> requiredLetters) {
        return characterCounts.entrySet().stream()
                .filter(entrySet -> !requiredLetters.contains(entrySet.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, ConcurrentHashMap::new));
    }

    public Map<Integer, Map<Character, LongAdder>> removeRequiredLettersFromCountsByPosition(Map<Integer, Map<Character, LongAdder>> characterCounts, WordRestrictions wordRestrictions) {
        Map<Integer, Map<Character, LongAdder>> newMap = new ConcurrentHashMap<>();
        characterCounts.forEach((pos, v) -> {
            if(!wordRestrictions.letterPositions().containsKey(pos) || !v.containsKey(wordRestrictions.letterPositions().get(pos))) {
                newMap.put(pos, v);
            } else {
                newMap.put(pos, new ConcurrentHashMap<>());
            }
        });
        return newMap;
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
    protected Double calculateFreqScore(Word word, Map<Character, LongAdder> wordsWithCharacter, int totalWords, int maxScore, Map<Character, DoubleAdder> positionBonus) {
        if(totalWords < 1 || maxScore < 1) {
            return 0.0;
        }

        return word.letters().entrySet().stream()
                .mapToDouble(c -> {
                    double rutBreakerBonus = positionBonus.containsKey(c) ? positionBonus.get(c).doubleValue() : 0.0;
                    double numerator =  (wordsWithCharacter.containsKey(c.getKey()) ? wordsWithCharacter.get(c.getKey()).doubleValue() : 0) + rutBreakerBonus;
                    return numerator / ((double)totalWords * maxScore);
                }).sum();
    }

    /**
     * Calculates letter frequency by position and adds additional weighting bonuses as defined by this
     * service's configuration. Return value of 1.0 represents that every letter in the word exists in the same
     * positions in every word in the set. Values greater than 1.0 may be returned as a result of bias multipliers.
     * @return
     */
    protected Double calculateFreqScoreByPosition(Word word, Map<Integer, Map<Character, LongAdder>> wordsWithCharacter,
                                                  Set<Word> containedWords, int maxScore, WordRestrictions wordRestrictions, Map<Character, DoubleAdder> positionBonus) {
        if(containedWords.size() < 1 || maxScore < 1) {
            return 0.0;
        }

        double numKnownLetters = wordRestrictions.letterPositions().keySet().size();
        double wordLength = wordRestrictions.word().getLength();


        //scale location bonus based on number of positions known
        double locationAdjustment = 1 - ((numKnownLetters / word.getLength()) * locationAdjustmentScale);

        //scale unique bonus based on number of letters remaining
        double uniqueAdjustment = 1 - ((1 - (wordLength / WordRestrictions.NO_RESTRICTIONS.word().getLength())) * uniqueAdjustmentScale);

        //scale viable word preference based on number of positions known
        double viableWordAdjustment = (viableWordPreference - ((numKnownLetters * viableWordAdjustmentScale)/wordLength));

        double totalScore = 0.0;

        // for each position in this word (i), check to see how many points it scores based on letters in each position across all words (j)
        for(int i = 0; i < word.word().length(); i++) {
            char c = word.word().charAt(i);
            double vowelPenalty = vowels.contains(c) ? vowelAdjustment : 1.0;
            for(int j = 0; j < word.word().length(); j++) {
                double locationBonus = (i == j) ? 1 + (rightLocationMultiplier-1)*locationAdjustment : 1;
                double uniqueBonus = (word.letters().get(c) < 2) && !wordRestrictions.requiredLetters().contains(c) ? 1 + (uniquenessMultiplier-1)*uniqueAdjustment : 1;
                double rutBreakerBonus = positionBonus.containsKey(c) ? positionBonus.get(c).doubleValue() : 0.0;
                double numerator = wordsWithCharacter.get(j+1).containsKey(c) ? harmonic(wordsWithCharacter.get(j+1).get(c).intValue()) + rutBreakerBonus: 0;

                totalScore += ((numerator * locationBonus * uniqueBonus * vowelPenalty))
                         / (containedWords.size() * maxScore * rightLocationMultiplier); //divide by max score * bonuses to normalize scores closer to 100%
            }
        }

        if(totalScore > 0 && containedWords.contains(word)) {
            totalScore += viableWordAdjustment ; //tiebreaker toward potential solutions
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
            return wordsByRemainingGuesses(wordRestrictions, containedWords, mergeWordPools(wordFrequencyScores, fishingWords));
        } else {
            return new HashSet<>();
        }
    }

    public Set<Word> mergeWordPools(Set<WordFrequencyScore> viable, Set<WordFrequencyScore> fishing) {
        //get a pool of words taken from the top fishing and valid list
        return Stream.of(viable, fishing)
                .flatMap(Set::stream)
                .distinct()
                .map(wfs -> new Word(wfs.word(), wfs.naturalOrdering()))
                .collect(Collectors.toSet());
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
            //50/50 shot either way, so don't bother calculating
            return containedWords.stream().map(word -> new WordFrequencyScore(word.getOrder(), word.word(), 1.0 / containedWords.size())).collect(Collectors.toSet());
        }

        Set<WordFrequencyScore> scores = new TreeSet<>();
        Map<Word, DescriptiveStatistics> statSummary = new ConcurrentHashMap<>();

        //for each word in the pool, create a new wordRequirements as if that word had been picked for each solution
        //  then calculate how many remaining words are left and average the results
        wordPool.parallelStream().forEach(word -> {
            DescriptiveStatistics stats = getPartitionStatsForWord(startingRestrictions, containedWords, word);
            if(stats != null ) {
                statSummary.put(word, stats);
            }
        });

        statSummary.forEach((k, v) ->
                scores.add(new WordFrequencyScore(k.getOrder(), k.word(),
                        ((1.0 - (v.getMean() / containedWords.size()))
                                + (containedWords.contains(k) ? (viableWordPreference / (1 + startingRestrictions.letterPositions().keySet().size() * viableWordAdjustmentScale)) : 0))))); // add tiny bonus to viable words so they are prioritized
        return scores;
    }

    /**
     * Calculate how many words will remain in the word pool on average if a given word is selected
     * @param startingRestrictions The restrictions that were used to generate the current set of contained words
     * @param containedWords The currently available pool of valid solutions
     * @param word The word to be evaluated
     * @return A stats object populated with the counts of all the potential new words list
     */
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

    public Set<PlayOut> getWordsBySolveLength(Set<Word> containedWords, Set<Word> fishing, Set<Word> wordPool, Solver solver, WordRestrictions startingRestrictions, int guessNumber) {
        log.info("Generating {} playouts with {} valid solutions for {} total playouts using restrictions {}", wordPool.size(), containedWords.size(), (wordPool.size() * containedWords.size()), startingRestrictions);
        AtomicInteger i = new AtomicInteger(0);
        return wordPool.stream().map(guess -> {
            DescriptiveStatistics stats = new DescriptiveStatistics();
            List<List<String>> failures = new ArrayList<>();
            containedWords.forEach(solution -> {
                List<String> r = solver.solve(solution, containedWords, fishing, guess, startingRestrictions);
                stats.addValue(r.size());
                if(r.size() > (6 - guessNumber)) {
                    failures.add(r);
                }
            });
            var countMap = Arrays.stream(stats.getSortedValues()).mapToInt(num -> (int) num).boxed().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            if(i.incrementAndGet() % 10 == 0) {
                log.info("Completed " + i.get() + "/" + wordPool.size() + " playouts");
            }
            return new PlayOut(guess.word(), stats.getMean(), countMap.toString(), failures);
        }).collect(Collectors.toCollection(() -> new TreeSet<>()));
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

    /**
     *
     * @param wordList
     * @return
     */
    public SharedPositions findSharedWordRestrictions(Set<Word> wordList) {

        Word[] words = new Word[wordList.size()];
        words = wordList.toArray(words);

        Map<KnownPosition, Set<Word>> knownPositions = new HashMap<>();

        for(int i = 0; i < words.length; i++) {
            for(int j = i; j < words.length; j++) {
                Word w1 = words[i];
                Word w2 = words[j];
                Map<Integer, Character> sharedPositions = findSharedPositions(w1, w2);
                if(sharedPositions.isEmpty() || sharedPositions.keySet().size() < 3 || sharedPositions.keySet().size() == w1.getLength()) {
                    continue; //don't bother saving all the tiny matches or cases where the only match is the full word
                }
                KnownPosition knownPosition = new KnownPosition(sharedPositions);
                knownPositions.computeIfAbsent(knownPosition, k-> new HashSet<>()).addAll(List.of(w1, w2));
            }
        }
        return new SharedPositions(knownPositions);
    }

    /**
     * Returns a 1-based map of letter position to shared character. For example
     * if the words are ROWER and TONER, the result is {2='O', 4='E', 5='R'}
     * @param w1
     * @param w2
     * @return
     */
    private Map<Integer, Character> findSharedPositions(Word w1, Word w2) {

        Map<Integer, Character> knownPositions = new HashMap<>();
        for(int i = 0; i < w1.getLength(); i++) {
            if(w1.word().charAt(i) == w2.word().charAt(i)) {
                knownPositions.put(i+1, w1.word().charAt(i));
            }
        }
        return knownPositions;
    }

    /**
     * Generates a set of weights for characters that are used to differentiate between words in common
     * sets of similar words. For example, EIGHT/LIGHT/SIGHT/TIGHT would apply bonus weight to E, L, and S
     * (but not T, because we already know that is part of the set).
     * @param sharedPositions All the shared positions available for this set of restrictions
     * @param wordRestrictions Word restrictions (used to exclude bonuses for letters we already know)
     * @return
     */
    public Map<Character, DoubleAdder> generateSharedCharacterWeights(SharedPositions sharedPositions, WordRestrictions wordRestrictions) {

        Map<Character, DoubleAdder> result = new ConcurrentHashMap<>();

        sharedPositions.knownPositions().forEach((kp, wordSet) -> {
            //sets with more words are weighted more heavily
            if(wordSet.size() < rutBreakThreshold) {
                return;
            }
            double multFactor = ((double)wordSet.size() / sharedPositions.largestSet()) * rutBreakMultiplier;
            calculateCharacterCountsByPosition(wordSet).forEach((position, charMap) ->{
                // if the knownPosition doesn't have any value for this position, that means we still need to know it
                //   so add the number of characters to the result
                if(!kp.pos().containsKey(position)) {
                    charMap.forEach((c, amt) -> {
                        // only add bonuses to letters we don't know
                        if(!wordRestrictions.requiredLetters().contains(c) && !kp.pos().containsValue(c)) {
                            result.computeIfAbsent(c, k -> new DoubleAdder()).add(amt.longValue() * multFactor);
                        }
                    });
                }
            });
        });
        return result;
    }


}
