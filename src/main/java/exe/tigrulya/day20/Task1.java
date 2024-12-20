package exe.tigrulya.day20;

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

import static exe.tigrulya.Utils.getResource;

public class Task1 {
    public record Coordinates(int x, int y) {
        public Coordinates sum(Coordinates other) {
            return new Coordinates(x + other.x, y + other.y);
        }
    }

    public record Field(List<List<Character>> field) {
        public Optional<Long> findShortestPath(Coordinates start, Coordinates end) {
            return Optional.ofNullable(
                    buildPathScores(start).get(end)
            );
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
                getNotObstacle(maybeNeighbour).ifPresent(neighbour ->
                        results.add(maybeNeighbour));
            }

            return results;
        }

        private Optional<Character> getNotObstacle(Coordinates coordinates) {
            return get(field, coordinates.y)
                    .flatMap(row -> get(row, coordinates.x))
                    .filter(ch -> ch != '#');
        }

        private Character unsafeGet(Coordinates coordinates) {
            return field.get(coordinates.y).get(coordinates.x);
        }

        private <T> Optional<T> get(List<T> values, int idx) {
            return Optional.of(values)
                    .filter(ignore -> idx >= 0 && idx < values.size())
                    .map(ignore -> values.get(idx));
        }

        public void set(Coordinates coordinates, char value) {
            field.get(coordinates.y).set(coordinates.x, value);
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
        List<List<Character>> fieldRows = new ArrayList<>();
        int y = -1;

        Coordinates startPosition = new Coordinates(0, 0);
        Coordinates targetPosition = new Coordinates(0, 0);

        try (var lines = Files.lines(getResource("input/20.txt"))) {
            Iterable<String> linesIterable = lines::iterator;
            for (var line : linesIterable) {
                ++y;
                List<Character> row = new ArrayList<>();
                for (int x = 0; x < line.length(); ++x) {
                    row.add(line.charAt(x));

                    if (line.charAt(x) == 'S') {
                        startPosition = new Coordinates(x, y);
                    }

                    if (line.charAt(x) == 'E') {
                        targetPosition = new Coordinates(x, y);
                    }
                }
                fieldRows.add(row);
            }
        }

        Field field = new Field(fieldRows);
        long result = findGoodCheatCodes(field, startPosition, targetPosition, 100);
        System.out.println("Result: " + result);
    }

    private static long findGoodCheatCodes(
            Field field, Coordinates startPosition, Coordinates endPosition, long threshold) {
        // for untouched field result is always unique
        long baseResult = field.findShortestPath(startPosition, endPosition).orElseThrow();

        Map<Long, Long> improvementCounts = new HashMap<>();
        for (Coordinates obstacle : findObstaclesToRemove(field)) {
            field.set(obstacle, '.');

            long improvement = baseResult - field.findShortestPath(startPosition, endPosition).orElseThrow();
            if (improvement >= threshold) {
                improvementCounts.merge(improvement, 1L, Long::sum);
            }

            field.set(obstacle, '#');
        }

        System.out.println(improvementCounts);
        return improvementCounts.values()
                .stream()
                .reduce(0L, Long::sum);
    }

    private static List<Coordinates> findObstaclesToRemove(Field field) {
        List<Coordinates> obstacles = new ArrayList<>();
        for (int y = 1; y < field.field.size() - 1; ++y) {
            for (int x = 1; x < field.field.get(y).size() - 1; ++x) {
                Coordinates position = new Coordinates(x, y);
                if (field.unsafeGet(position) == '#' && (
                        field.getNotObstacle(position.sum(Direction.LEFT.shift)).isPresent()
                                && field.getNotObstacle(position.sum(Direction.RIGHT.shift)).isPresent()
                                || field.getNotObstacle(position.sum(Direction.DOWN.shift)).isPresent()
                                && field.getNotObstacle(position.sum(Direction.UP.shift)).isPresent())) {
                    obstacles.add(position);
                }
            }
        }

        return obstacles;
    }
}

