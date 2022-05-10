package com.appsoil.solvle.service;

import java.util.Map;

public record  WordCalculationConfig (
        double rightLocationMultiplier,     // multiplies a letter score if this letter is in the correct position. Best range: [3-6]
        double uniquenessMultiplier,        // multiplies a letter score if it is not duplicate in the current word Best range: [4-9]
        boolean useHarmonic,                // scales down impact of more matches to prioritize new letters > important letters. Increases mean, but decreases max.
        int partitionThreshold,             // number of viable words below which word partition calcs are performed. Impacts performance noticeably above 10, very significantly above 100.
        int fishingThreshold,               // used by solvers to determine when to switch to only viable word choices. Best value is usually 2 or 3.
        double viableWordPreference         // flat bonus to words in the viable word set. High values increase StDev. Best range: [0.001 - 0.01].
){

    public static WordCalculationConfig withPartitionThreshold(int partitionThreshold, WordCalculationConfig c) {
        return new WordCalculationConfig(c.rightLocationMultiplier, c.uniquenessMultiplier, c.useHarmonic, partitionThreshold, c.fishingThreshold, c.viableWordPreference);
    }

    /**
     * config that will not utilize letter position bias or remaining word partitioning.
     * {2=51, 3=743, 4=1221, 5=284, 6=16}
     * Mean: 3.7714902807775377
     * StDv: 0.7123185627850815
     * Median: 4.0
     * @return
     */
    public static WordCalculationConfig SIMPLE = new WordCalculationConfig(0, 0, false, 0, 2, 0);

    /**
     * Returns a config that minimizes the mean score without failing
     * {1=1, 2=84, 3=1129, 4=1010, 5=87, 6=4}
     *  mean: 3.479482
     *  std dev: 0.640661721
     *  median: 3.0
     * @return
     */
    public static WordCalculationConfig OPTIMAL_MEAN =  new WordCalculationConfig(4, 9, false, 100, 2, .007);

    /**
     * Returns a config guaranteed to solve any wordle in 5 or fewer guesses, at the cost of higher mean
     *  {2=79, 3=927, 4=1165, 5=144}
     *  mean: 3.593520518
     *  std dev: 0.65886115
     *  median: 4.0
     * @return
     */
    public static WordCalculationConfig LOWEST_MAX = new WordCalculationConfig(1, 5, false, 50, 2, 0.01);

    /**
     * Maximize the number of scores 3 and below
     * {1=1, 2=75, 3=1172, 4=943, 5=113, 6=11}
     *  mean: 3.485961123
     *  std dev: 0.665969227
     *  median: 3.0
     * @return
     */
    public static WordCalculationConfig THREE_OR_LESS = new WordCalculationConfig(4, 8, false, 50, 3, .001);

    /**
     * Maximize the number of scores 4 and below
     * {1=1, 2=75, 3=1126, 4=1022, 5=87, 6=4}
     *  mean: 3.488552916
     *  std dev: 0.634790355
     *  median: 3.0
     * @return
     */
    public static WordCalculationConfig FOUR_OR_LESS = new WordCalculationConfig(3, 10, false, 50, 2, .007);

    /**
     * Maximize the number of scores 2 at all cost
     * {1=1, 2=146, 3=959, 4=966, 5=214, 6=27, 7=1, 8=1}
     *  mean: 3.577105832
     *  std dev: 0.798384986
     *  median: 4.0
     * @return
     */
    public static WordCalculationConfig TWO_OR_LESS =  new WordCalculationConfig(10, 3, false, 10, 2, .25);

    public static Map<String, WordCalculationConfig> DEFAULT_CONFIGS = Map.of(
            "SIMPLE", SIMPLE,
            "OPTIMAL_MEAN", OPTIMAL_MEAN,
            "LOWEST_MAX", LOWEST_MAX,
            "THREE_OR_LESS", THREE_OR_LESS,
            "FOUR_OR_LESS", FOUR_OR_LESS,
            "TWO_OR_LESS", TWO_OR_LESS
    );
}
