package com.appsoil.solvle.wordler;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Data
@Log4j2
public class WordleInfo extends Word {
    private Set<Character> requiredLetters = new HashSet<>();
    private Map<Integer, Character> letterPositions = new HashMap<>();
    private Map<Integer, Set<Character>> positionExclusions = new HashMap<>();

    //parses a string of letters. Letters may be followed by either a number, a question mark, or both
    // for example abc3d!e!45fg! will be parsed into (a)(b)(c3)(d!)(e!45)(f)(g!)
    Pattern wordleRegex = Pattern.compile("(\\S)(\\d)*(\\!\\d*)*");

    /**
     * Creates a description of known wordle knowledge based on provided input string.
     * @param word String containing all available letters from which to guess. If a letter is followed by
     *             numbers, those numbers indicate required positions for the letter. If a letter is followed
     *             by an exclamation (!), that letter is required, but we don't know where. If the ! is followed
     *             by numbers, those indicate positions we know are not available.
     *
     *             For example: ac1t*2u - this string tells us:
     *                  a   - available
     *                  c1  - required in position 1
     *                  t!12- required, but NOT in positions 1 or 2
     *                  u   - available
     */
    public WordleInfo(String word) {
        super(word.replaceAll("[^A-Za-z]", "")); //create a base Word with only alpha

        log.info("Parsing wordle characters " + word);

        Matcher matcher = wordleRegex.matcher(word);
        while(matcher.find()) {
            char c = matcher.group(1).charAt(0);
            boolean hasPos = matcher.group(2) != null;
            boolean required = matcher.group(3) != null;

            if(required || hasPos) {
                requiredLetters.add(c);
            }
            if(hasPos) {
                matcher.group(2).chars().mapToObj(i -> Character.getNumericValue(i)).forEach(pos ->  {
                    letterPositions.put(pos, c);
                });
            }
            //if we have position numbers after the '!', add those to the position exclusions map
            if(required) {
                matcher.group(3).chars().mapToObj(i -> Character.getNumericValue(i)).skip(1).forEach(pos -> {
                    if(!positionExclusions.containsKey(pos)) {
                        positionExclusions.put(pos, new HashSet<>());
                    }
                    positionExclusions.get(pos).add(c);
                });
            }
        }

    }
}
