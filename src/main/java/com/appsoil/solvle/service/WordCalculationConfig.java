package com.appsoil.solvle.service;

public record  WordCalculationConfig (
        double rightLocationMultiplier,     // multiplies a letter score if this letter is in the correct position. Best range: [3-6]
        double uniquenessMultiplier,        // multiplies a letter score if it is not duplicate in the current word Best range: [4-8]
        boolean useHarmonic,                // scales down impact of more matches to prioritize new letters > important letters. Increases mean, but decreases max.
        int partitionThreshold,             // number of viable words below which word partition calcs are performed. Impacts performance noticeably above 10, very significantly above 100.
        int fishingThreshold,               // used by solvers to determine when to switch to only viable word choices. Best value is usually 2 or 3.
        double viableWordPreference         // flat bonus to words in the viable word set. High values increase StDev. Best range: [0.001 - 0.01].
){

    public static WordCalculationConfig withPartitionThreshold(int partitionThreshold, WordCalculationConfig c) {
        return new WordCalculationConfig(c.rightLocationMultiplier, c.uniquenessMultiplier, c.useHarmonic, partitionThreshold, c.fishingThreshold, c.viableWordPreference);
    }

    /**
     * Returns a config that will not utilize letter position bias or remaining word partitioning.
     * @return
     */
    public static WordCalculationConfig getSimpleConfig() {
        return new WordCalculationConfig(0, 0, false, 0, 2, 0);
    }

    /**
     * Returns a config that minimizes the mean score without failing
     * {2=79, 3=1145, 4=988, 5=98, 6=5}
     *  mean: 3.483801295896329
     *  std dev: 0.644819307834611
     *  median: 3.0
     * @return
     */
    public static WordCalculationConfig getOptimalMeanConfig() {
        return new WordCalculationConfig(4, 8, false, 50, 2, .007);
    }

    /**
     * Returns a config guaranteed to solve any wordle in 5 or fewer guesses, at the cost of higher mean
     *  {2=45, 3=694, 4=1290, 5=286}
     *  mean: 3.7848812095032396
     *  std dev: 0.6745371479880169
     *  median: 4.0
     * @return
     */
    public static WordCalculationConfig getLowestMaxConfig() {
        return new WordCalculationConfig(3, 5, true, 20, 2, 0.0);
    }

    /**
     * Maximize the number of scores 3 and below
     * {2=83, 3=1156, 4=954, 5=111, 6=10, 7=1}
     *  mean: 3.4868250539956804
     *  std dev: 0.669868910970966
     *  median: 3.0
     * @return
     */
    public static WordCalculationConfig getMaximizeThreeConfig() {
        return new WordCalculationConfig(3, 5, false, 50, 3, .007);
    }
}
