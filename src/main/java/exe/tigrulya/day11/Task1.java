package exe.tigrulya.day11;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static exe.tigrulya.Utils.getResource;

public class Task1 {
    public static void main(String[] args) throws IOException {
        try (var lines = Files.lines(getResource("input/11.txt"))) {
            List<Long> stones = lines
                    .flatMap(line -> Arrays.stream(line.split(" ")))
                    .map(Long::parseLong)
                    .toList();

            List<Long> newStones = blinkNTimes(stones, 25);
            System.out.println("Result: " + newStones.size());
        }
    }

    private static List<Long> blinkNTimes(List<Long> stones, int n) {
        List<Long> newStones = stones;

        int blinksLeft = n;
        while (blinksLeft-- > 0) {
            newStones = blinkInPlace(newStones);
            System.out.println("Blink #" + (n - blinksLeft));
            System.out.println("Size: " + newStones.size());
        }

        return newStones;
    }

    private static List<Long> blinkInPlace(List<Long> stones) {
        return stones.stream()
                .flatMap(Task1::transform)
                .toList();
    }

    private static Stream<Long> transform(Long stone) {
        if (stone == 0) {
            return Stream.of(1L);
        }

        String stringStone = String.valueOf(stone);
        if (stringStone.length() % 2 == 0) {
            return Stream.of(
                    stringStone.substring(0, stringStone.length() / 2),
                    stringStone.substring(stringStone.length() / 2)
            ).map(Long::parseLong);
        }

        return Stream.of(stone * 2024);
    }
}

