package exe.tigrulya.day5;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static exe.tigrulya.Utils.getResource;

public class Task1 {
    public static void main(String[] args) throws IOException {
        Map<Integer, Set<Integer>> rules = new HashMap<>();

        boolean delimiterHandled = false;
        long result = 0;

        try (var lines = Files.lines(getResource("input/5.txt"))) {
            Iterable<String> linesIterable = lines::iterator;
            for (var line : linesIterable) {
                if (line.isBlank()) {
                    delimiterHandled = true;
                    continue;
                }

                if (!delimiterHandled) {
                    List<Integer> pages = parsePages(line, "\\|");
                    rules.computeIfAbsent(pages.getFirst(), k -> new HashSet<>())
                            .add(pages.getLast());
                    continue;
                }

                List<Integer> pages = parsePages(line, ",");
                if (isCorrect(rules, pages)) {
                    result += pages.get(pages.size() / 2);
                }
            }

            System.out.println("Result: " + result);
        }
    }

    private static List<Integer> parsePages(String line, String delimiter) {
        return Arrays.stream(line.split(delimiter))
                .map(Integer::parseInt)
                .toList();
    }

    private static boolean isCorrect(Map<Integer, Set<Integer>> rules, List<Integer> pages) {
        for (int i = 0; i < pages.size(); ++i) {
            for (int j = i + 1; j < pages.size(); ++j) {
                if (rules.getOrDefault(pages.get(j), Set.of()).contains(pages.get(i))) {
                    return false;
                }
            }
        }

        return true;
    }
}

