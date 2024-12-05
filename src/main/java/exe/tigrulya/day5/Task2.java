package exe.tigrulya.day5;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static exe.tigrulya.Utils.getResource;

public class Task2 {
    public static void main(String[] args) throws IOException {
        Map<Integer, Set<Integer>> rules = new HashMap<>();

        boolean delimiterHandled = false;
        long result = 0;

        try (var lines = Files.lines(getResource("input/5_2.txt"))) {
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
                result += getMiddleFixedPage(rules, pages).orElse(0);
            }

            System.out.println("Result: " + result);
        }
    }

    private static List<Integer> parsePages(String line, String delimiter) {
        return Arrays.stream(line.split(delimiter))
                .map(Integer::parseInt)
                .toList();
    }

    private static Optional<Integer> getMiddleFixedPage(Map<Integer, Set<Integer>> rules, List<Integer> pages) {
        for (int i = 0; i < pages.size(); ++i) {
            for (int j = i + 1; j < pages.size(); ++j) {
                if (rules.getOrDefault(pages.get(j), Set.of()).contains(pages.get(i))) {
                    List<Integer> fixedPages = new TopoSort(rules, pages).sort();
                    return Optional.of(fixedPages.get(fixedPages.size() / 2));
                }
            }
        }

        return Optional.empty();
    }

    public static class TopoSort {
        private final Map<Integer, Set<Integer>> graph;
        private final List<Integer> vertices;
        private final Map<Integer, Boolean> visitedVertices;

        public TopoSort(Map<Integer, Set<Integer>> graph, List<Integer> vertices) {
            this.graph = graph;
            this.vertices = vertices;
            this.visitedVertices = vertices.stream()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            page -> false
                    ));
        }

        public List<Integer> sort() {
            Deque<Integer> sortQueue = new ArrayDeque<>();

            for (int vertex : vertices) {
                visit(vertex, sortQueue);
            }

            return new ArrayList<>(sortQueue.reversed());
        }

        private void visit(int vertex, Queue<Integer> sortQueue) {
            if (visitedVertices.getOrDefault(vertex, true)) {
                return;
            }

            Set<Integer> children = graph.getOrDefault(vertex, Set.of());

            for (int child : children) {
                if (visitedVertices.containsKey(child)) {
                    visit(child, sortQueue);
                }
            }

            sortQueue.add(vertex);
            visitedVertices.put(vertex, true);
        }
    }
}

