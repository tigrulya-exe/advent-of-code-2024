package exe.tigrulya.day1;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static exe.tigrulya.Utils.getResource;

public class Task2 {
    public static void main(String[] args) throws IOException {
        List<Long> left = new ArrayList<>();
        Map<Long, Long> counts = new HashMap<>();

        try (var lines = Files.lines(getResource("input/1_2.txt"))) {
            lines.map(line -> line.split("\\s+"))
                    .forEach(numbers -> {
                        left.add(Long.parseLong(numbers[0]));
                        counts.compute(Long.parseLong(numbers[1]), (key, val) -> Optional.ofNullable(val)
                                .map(count -> count + 1)
                                .orElse(1L));
                    });
        }

        Long similarityScore = left.stream()
                .map(num -> num * counts.getOrDefault(num, 0L))
                .reduce(0L, Long::sum);

        System.out.println("Result: " + similarityScore);
    }
}

