package com.appsoil.solvle.service;

import com.appsoil.solvle.config.SolvleConfig;
import com.appsoil.solvle.service.solvers.RemainingSolver;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@Disabled
@SpringBootTest(classes = {SolvleService.class, SolvleConfig.class})
public class FullDictionaryTest {

    @Autowired
    SolvleService solvleService;

    static Map<WordCalculationConfig, TestReport> testReports;

    record TestReport(DescriptiveStatistics stats, String firstWord, List<List<String>> problems) {}

    @BeforeAll
    static private void init() {
         testReports = new HashMap<>();
    }

    @AfterAll
    static private void report() {
        log.warn("Begin report full report:");
        testReports.forEach(FullDictionaryTest::logReport);
    }

    static private void addStats(WordCalculationConfig config, Map<String, List<String>> solution) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        List<List<String>> problems = new ArrayList<>();
        String firstWord = solution.values().stream().findFirst().get().get(0);
        solution.forEach((k, v) -> {
            stats.addValue(v.size());
            if(v.size() > 5) {
                log.info(v);
                problems.add(v);
            }
        });
        TestReport report = new TestReport(stats, firstWord, problems);
        testReports.put(config, report);
        var countMap = Arrays.stream(report.stats().getSortedValues()).mapToInt(num -> (int) num).boxed().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        log.info(config);
        log.info("Mean: {}, StDv: {}, Median: {}, Counts: {}", report.stats().getMean(), report.stats().getStandardDeviation(), report.stats().getPercentile(50), countMap);
        logReport(config, report);
    }

    static private void logReport(WordCalculationConfig config, TestReport report) {
        //format data to copy into an annoying spreadsheet
        var countMap = Arrays.stream(report.stats().getSortedValues()).mapToInt(num -> (int) num).boxed().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        log.warn("{}\t{}\t{}\t{}\t{}\t{}\t\t\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}",
                report.stats().getMean(),
                report.stats().getMin(),
                report.stats().getMax(),
                report.stats().getPercentile(50),
                report.stats().getStandardDeviation(),
                config.fishingThreshold(),
                config.rightLocationMultiplier(),
                config.uniquenessMultiplier(),
                config.partitionThreshold(),
                config.viableWordPreference(),
                config.useHarmonic(),
                countMap.getOrDefault(1, 0l),
                countMap.getOrDefault(2, 0l),
                countMap.getOrDefault(3, 0l),
                countMap.getOrDefault(4, 0l),
                countMap.getOrDefault(5, 0l),
                countMap.getOrDefault(6, 0l),
                countMap.getOrDefault(7, 0l),
                (countMap.getOrDefault(8, 0l) + countMap.getOrDefault(9, 0l) + countMap.getOrDefault(10, 0l)),
                (countMap.getOrDefault(1, 0l) + countMap.getOrDefault(2, 0l) + countMap.getOrDefault(3, 0l))
        );
    }

    @ParameterizedTest
    @CsvSource({
            "2, 20",
            "2, 10",
            "2, 50",
    })
    public void dictionaryRemainingSolver( int fishingThreshold, int permutationThreshold ) {
        log.info("Starting permutation solver {}, {}", fishingThreshold, permutationThreshold);
        String firstWord = "";
        WordCalculationConfig config = new WordCalculationConfig(3, 5, true, permutationThreshold, 2, 0.0);
        solvleService.solveDictionary(new RemainingSolver(solvleService, config), firstWord, config);
    }

    @ParameterizedTest
    @CsvSource({
            "2, 4, 9, 50, false, .007",
    })
    public void dictionaryRemainingPermutationSolver( int fishingThreshold, double rightLocationMultiplier, double uniquenessMultiplier, int permutationThreshold, boolean useHarmonic, double viableWordPreference) {
        log.info("Starting permutation solver {}, {}", fishingThreshold, permutationThreshold);
        String firstWord = "";
        WordCalculationConfig config = new WordCalculationConfig(rightLocationMultiplier, uniquenessMultiplier, useHarmonic, permutationThreshold, fishingThreshold, viableWordPreference);
        addStats(config, solvleService.solveDictionary(new RemainingSolver(solvleService, config), firstWord, config));
    }

    @ParameterizedTest
    @MethodSource("dictionaryPermutationParameters")
    public void dictionaryRemainingPermutationSolver2( WordCalculationConfig config) {
        log.info("Starting permutation solver {}", config);
        String firstWord = "";
        addStats(config, solvleService.solveDictionary(new RemainingSolver(solvleService, config), firstWord, config));
    }

    private static Stream<Arguments> dictionaryPermutationParameters() {
        List<Arguments> args = new ArrayList<>();

        for(int i = 2; i <= 5; i++) {
//            for(int j = 1; j <= 10; j++) {
//                for(int k = 5; k <= 50; k+= 5) {
                    args.add(Arguments.of(new WordCalculationConfig(i, (2*i)+1, false, 200, 2, .007)));
//                }
//            }
        }
        return args.stream();
    }


    @Test
    public void compareStandardConfigs() {
        String firstWord = "";
        WordCalculationConfig.DEFAULT_CONFIGS.forEach((name, config) -> {
            log.info("Running " + name);
            addStats(config, solvleService.solveDictionary(new RemainingSolver(solvleService, config), firstWord, config));
        });
    }


}
