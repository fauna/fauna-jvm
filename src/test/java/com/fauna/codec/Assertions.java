package com.fauna.codec;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Assertions {


    /**
     * Sometimes things can get serialized, but the order can vary. This method asserts that the strings have the same
     * contents, with only the order being different.
     * assertStringEquivalence("abc", "abc") -> good!
     * assertStringEquivalence("abc", "cba") -> good!
     * assertStringEquivalence("abc", "abd") -> fail!
     * assertStringEquivalence("abc", "ab")  -> fail!
     * @param expected
     * @param actual
     */
    public static void assertStringEquivalence(String expected, String actual) {
        assertEquals(expected.length(), actual.length());
        Map<Character, Integer> actualChars = new HashMap<>();
        Map<Character, Integer> expectedChars = new HashMap<>();
        for (Character c : expected.toCharArray()) {
            expectedChars.put(c, expectedChars.getOrDefault(c, 0) + 1);
        }
        for (Character c: actual.toCharArray()) {
            actualChars.put(c, actualChars.getOrDefault(c, 0) + 1);
        }
        expectedChars.forEach((c, i) -> assertEquals(i, actualChars.getOrDefault(c, -1)));
    }
}
