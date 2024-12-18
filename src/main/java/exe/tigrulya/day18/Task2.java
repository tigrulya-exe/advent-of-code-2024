package exe.tigrulya.day18;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static exe.tigrulya.Utils.getResource;

public class Task2 {
    public record Coordinates(int x, int y) {
        public Coordinates sum(Coordinates other) {
            return new Coordinates(x + other.x, y + other.y);
        }
    }

    public record Field(List<List<Character>> field) {
        public Optional<Long> findShortestPath(Coordinates start, Coordinates end) {
            Map<Coordinates, Long> scores = buildPathScores(start);
            return Optional.ofNullable(scores.get(end));
        }

        private Map<Coordinates, Long> buildPathScores(Coordinates start) {
            Map<Coordinates, Long> scores = new HashMap<>();
            Set<Coordinates> visitedNodes = new HashSet<>();
            Set<Coordinates> queue = new HashSet<>();

            queue.add(start);
            scores.put(start, 0L);

            while (!queue.isEmpty()) {
                Coordinates vertex = pollWithMinimalScore(queue, scores);
                for (var neighbour : getNeighbours(vertex)) {
                    if (visitedNodes.contains(neighbour)) {
                        continue;
                    }

                    long newNeighbourScore = scores.get(vertex) + 1;
                    if (newNeighbourScore < scores.getOrDefault(neighbour, Long.MAX_VALUE)) {
                        scores.put(neighbour, newNeighbourScore);
                    }

                    queue.add(neighbour);
                }

                visitedNodes.add(vertex);
            }

            return scores;
        }

        private Coordinates pollWithMinimalScore(
                Set<Coordinates> queue,
                Map<Coordinates, Long> scores) {
            return queue.stream()
                    .min(Comparator.comparingLong(scores::get))
                    .map(coordinates -> {
                        queue.remove(coordinates);
                        return coordinates;
                    })
                    .orElseThrow();
        }

        private List<Coordinates> getNeighbours(Coordinates position) {
            List<Coordinates> results = new ArrayList<>();
            for (var direction : Direction.values()) {
                Coordinates maybeNeighbour = position.sum(direction.shift);
                get(maybeNeighbour).ifPresent(neighbour ->
                        results.add(maybeNeighbour));
            }

            return results;
        }

        private Optional<Character> get(Coordinates coordinates) {
            return get(field, coordinates.y)
                    .flatMap(row -> get(row, coordinates.x))
                    .filter(ch -> ch != '#');
        }

        private <T> Optional<T> get(List<T> values, int idx) {
            return Optional.of(values)
                    .filter(ignore -> idx >= 0 && idx < values.size())
                    .map(ignore -> values.get(idx));
        }

        public void set(Coordinates coordinates, char value) {
            field.get(coordinates.y).set(coordinates.x, value);
        }

        public void print() {
            for (var line : field) {
                String str = line.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining());
                System.out.println(str);
            }
            System.out.println();
        }
    }

    public enum Direction {
        UP(0, -1),
        LEFT(-1, 0),
        DOWN(0, 1),
        RIGHT(1, 0);

        private final Coordinates shift;

        Direction(int xShift, int yShift) {
            this.shift = new Coordinates(xShift, yShift);
        }
    }

    public static void main(String[] args) throws IOException {
        int takeFirst = 1024;
        int size = 71;

        try (var lines = Files.lines(getResource("input/18.txt"))) {
            List<Coordinates> obstacles = lines.map(line -> line.split(","))
                    .map(coords -> new Coordinates(Integer.parseInt(coords[0]), Integer.parseInt(coords[1])))
                    .toList();

            Optional<Long> result = Optional.of(-1L);
            while (result.isPresent()) {
                System.out.println("progress: " + (1.0 * takeFirst / obstacles.size()));

                Field field = buildField(obstacles.subList(0, takeFirst++), size);
                result = field.findShortestPath(
                        new Coordinates(0, 0),
                        new Coordinates(size - 1, size - 1)
                );
            }

            System.out.println("Result: " + obstacles.get(takeFirst - 2));
        }
    }

    private static Field buildField(List<Coordinates> obstacles, int size) {
        List<List<Character>> fieldRows = IntStream.range(0, size)
                .mapToObj(row -> IntStream.range(0, size)
                        .mapToObj(v -> '.')
                        .collect(Collectors.toList()))
                .toList();

        Field field = new Field(fieldRows);
        obstacles.forEach(o -> field.set(o, '#'));
//        field.print();
        return field;
    }

}

