package exe.tigrulya.day12;

import static exe.tigrulya.Utils.getResource;

import exe.tigrulya.day10.Task2;
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
import java.util.stream.Stream;

public class Task1 {
    public record Coordinates(int x, int y) {
        public Coordinates sum(Coordinates other) {
            return new Coordinates(x + other.x, y + other.y);
        }
    }
    
    public record Field(List<List<Integer>> field) {
        public Optional<Integer> get(Coordinates coordinates) {
            return get(field, coordinates.y)
                .flatMap(row -> get(row, coordinates.x));
        }

        public List<Coordinates> getNext(Coordinates coordinates) {
            Optional<Integer> currentHeight = get(coordinates);
            return currentHeight.map(integer -> Stream.of(
                        new Coordinates(0, 1),
                        new Coordinates(1, 0),
                        new Coordinates(0, -1),
                        new Coordinates(-1, 0)
                    ).map(diff -> diff.sum(coordinates))
                    .filter(nextCoords -> isNext(nextCoords, integer))
                    .toList())
                .orElseGet(List::of);
        }

        private boolean isNext(Coordinates coordinates, int currentHeight) {
            return get(coordinates)
                .filter(value -> value == currentHeight + 1)
                .isPresent();
        }

        private <T> Optional<T> get(List<T> values, int idx) {
            return Optional.of(values)
                .filter(ignore -> idx >= 0 && idx < values.size())
                .map(ignore -> values.get(idx));
        }
    }

    public static void main(String[] args) throws IOException {
        Map<Integer, Set<Integer>> rules = new HashMap<>();

        boolean delimiterHandled = false;
        long result = 0;

        try (var lines = Files.lines(getResource("input/12.txt"))) {
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

