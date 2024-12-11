package exe.tigrulya.day11;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static exe.tigrulya.Utils.getResource;

public class Task2 {

    public static void main(String[] args) throws IOException {
        try (var lines = Files.lines(getResource("input/11.txt"))) {
            List<Long> stones = lines
                    .flatMap(line -> Arrays.stream(line.split(" ")))
                    .map(Long::parseLong)
                    .toList();

            long stoneCount = blinkNTimes(stones, 75);
            System.out.println("Result: " + stoneCount);
        }
    }

    private static long blinkNTimes(List<Long> stones, int blinks) {
        Map<Long, Long> stoneCounts = stones.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        ignore -> 1L
                ));

        for (int blink = 0; blink < blinks; ++blink) {
            stoneCounts = blink(stoneCounts);

            System.out.println("Blink #" + (blink + 1) + " size: " + countStones(stoneCounts));
        }

        return countStones(stoneCounts);
    }

    private static Map<Long, Long> blink(Map<Long, Long> stoneCounts) {
        Map<Long, Long> newStoneCounts = new HashMap<>();

        for (var stoneCount : new HashSet<>(stoneCounts.entrySet())) {
            if (stoneCount.getKey() == 0) {
                newStoneCounts.merge(1L, stoneCount.getValue(), Long::sum);
                continue;
            }

            int digits = fastLog10(stoneCount.getKey());

            // equal to digits % 2 == 0
            if ((digits & 1) == 0) {
                long tenPowHalfDigits = fastPow(10, digits / 2);
                newStoneCounts.merge(stoneCount.getKey() / tenPowHalfDigits, stoneCount.getValue(), Long::sum);
                newStoneCounts.merge(stoneCount.getKey() % tenPowHalfDigits, stoneCount.getValue(), Long::sum);
                continue;
            }

            newStoneCounts.merge(stoneCount.getKey() * 2024, stoneCount.getValue(), Long::sum);
        }

        return newStoneCounts;
    }

    private static long fastPow(long base, long exp) {
        long result = 1;
        while (exp > 0) {
            // equal to exp % 2 == 0
            if ((exp & 1) == 1) {
                result *= base;
            }
            exp >>= 1;
            base *= base;
        }
        return result;
    }

    private static int fastLog10(long value) {
        int rank = 1;
        long powOfTen = 10;

        while (powOfTen <= value) {
            ++rank;
            powOfTen *= 10;
        }

        return rank;
    }

    private static long countStones(Map<Long, Long> stoneCounts) {
        return stoneCounts.values()
                .stream()
                .mapToLong(v -> v)
                .sum();
    }
}

