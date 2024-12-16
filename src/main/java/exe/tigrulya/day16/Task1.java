package exe.tigrulya.day16;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static exe.tigrulya.Utils.getResource;

public class Task1 {
    public record Coordinates(int x, int y) {
        public Coordinates sum(Coordinates other) {
            return new Coordinates(x + other.x, y + other.y);
        }
    }

    public record CoordinatesWithDirection(Coordinates coordinates, Direction direction) {
    }

    public record Field(List<List<Character>> field) {
        public Optional<Long> findShortestPath(CoordinatesWithDirection start, Coordinates end) {
            Map<CoordinatesWithDirection, Long> scores = buildPathScores(start);
            return Stream.of(Direction.values())
                    .map(d -> new CoordinatesWithDirection(end, d))
                    .map(scores::get)
                    .filter(Objects::nonNull)
                    .sorted()
                    .findFirst();
        }

        private Map<CoordinatesWithDirection, Long> buildPathScores(CoordinatesWithDirection start) {
            Map<CoordinatesWithDirection, Long> scores = new HashMap<>();
            Set<CoordinatesWithDirection> visitedNodes = new HashSet<>();
            Set<CoordinatesWithDirection> queue = new HashSet<>();

            queue.add(start);
            scores.put(start, 0L);

            while (!queue.isEmpty()) {
                CoordinatesWithDirection vertex = pollWithMinimalScore(queue, scores);
                for (var neighbour : getNeighbours(vertex.coordinates)) {
                    if (visitedNodes.contains(neighbour)) {
                        continue;
                    }

                    int rotationPenalty = vertex.direction.getRotationPenalty(neighbour.direction);
                    long newNeighbourScore = scores.get(vertex) + rotationPenalty + 1;
                    if (newNeighbourScore < scores.getOrDefault(neighbour, Long.MAX_VALUE)) {
                        scores.put(neighbour, newNeighbourScore);
                    }

                    queue.add(neighbour);
                }

                visitedNodes.add(vertex);
            }

            return scores;
        }

        private CoordinatesWithDirection pollWithMinimalScore(
                Set<CoordinatesWithDirection> queue,
                Map<CoordinatesWithDirection, Long> scores) {
            return queue.stream()
                    .min(Comparator.comparingLong(scores::get))
                    .map(coordinates -> {
                        queue.remove(coordinates);
                        return coordinates;
                    })
                    .orElseThrow();
        }

        private List<CoordinatesWithDirection> getNeighbours(Coordinates position) {
            List<CoordinatesWithDirection> results = new ArrayList<>();
            for (var direction : Direction.values()) {
                Coordinates maybeNeighbour = position.sum(direction.shift);
                get(maybeNeighbour).ifPresent(neighbour ->
                        results.add(new CoordinatesWithDirection(maybeNeighbour, direction)));
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

        public int getRotationPenalty(Direction other) {
            int diff = Math.abs(ordinal() - other.ordinal());
            int rotationPenaltyMultiplier = diff > 2 ? 1 : diff;
            return rotationPenaltyMultiplier * 1000;
        }
    }

    public static void main(String[] args) throws IOException {
        List<List<Character>> field = new ArrayList<>();
        int y = -1;

        Coordinates startPosition = new Coordinates(0, 0);
        Coordinates targetPosition = new Coordinates(0, 0);

        try (var lines = Files.lines(getResource("input/16.txt"))) {
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
                field.add(row);
            }

            long result = new Field(field).findShortestPath(
                    new CoordinatesWithDirection(startPosition, Direction.RIGHT),
                    targetPosition
            ).orElse(-1L);
            System.out.println("Result: " + result);
        }
    }

}

