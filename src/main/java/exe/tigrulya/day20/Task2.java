package exe.tigrulya.day20;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static exe.tigrulya.Utils.getResource;

public class Task2 {
    public record Coordinates(int x, int y) {
        public Coordinates sum(Coordinates other) {
            return new Coordinates(x + other.x, y + other.y);
        }

        public int distance(Coordinates other) {
            return Math.abs(x - other.x) + Math.abs(y - other.y);
        }
    }

    public record ScoreWithHistory(long score, List<Coordinates> historyPath) implements Comparable<ScoreWithHistory> {
        public static final ScoreWithHistory DEFAULT = new ScoreWithHistory(Long.MAX_VALUE, new ArrayList<>());

        @Override
        public int compareTo(ScoreWithHistory o) {
            return Long.compare(score, o.score);
        }

        public List<Coordinates> withNewNode(Coordinates coordinates) {
            return withNewNodes(List.of(coordinates));
        }

        public List<Coordinates> withNewNodes(Collection<Coordinates> coordinates) {
            List<Coordinates> newPath = new ArrayList<>(historyPath);
            newPath.addAll(coordinates);
            return newPath;
        }
    }

    public record Field(List<List<Character>> field) {
        public int rows() {
            return field.size();
        }

        public int columns() {
            return field.getFirst().size();
        }

        public Optional<ScoreWithHistory> findShortestPath(Coordinates start, Coordinates end, boolean collectPath) {
            return Optional.ofNullable(buildPathScores(start, collectPath).get(end));
        }

        private Map<Coordinates, ScoreWithHistory> buildPathScores(Coordinates start, boolean collectPath) {
            Map<Coordinates, ScoreWithHistory> scoresWithHistory = new HashMap<>();
            Set<Coordinates> visitedNodes = new HashSet<>();
            Set<Coordinates> queue = new HashSet<>();

            queue.add(start);
            scoresWithHistory.put(start, new ScoreWithHistory(0, List.of(start)));

            while (!queue.isEmpty()) {
                Coordinates vertex = pollWithMinimalScore(queue, scoresWithHistory);
                for (var neighbour : getNeighbours(vertex)) {
                    if (visitedNodes.contains(neighbour)) {
                        continue;
                    }

                    ScoreWithHistory currentScoreWithHistory = scoresWithHistory.get(vertex);
                    long newNeighbourScore = currentScoreWithHistory.score + 1;

                    ScoreWithHistory oldNeighbourScoreWithHistory = scoresWithHistory.getOrDefault(neighbour, ScoreWithHistory.DEFAULT);
                    if (newNeighbourScore < oldNeighbourScoreWithHistory.score) {
                        List<Coordinates> newPath = collectPath
                                ? currentScoreWithHistory.withNewNode(neighbour)
                                : List.of();

                        scoresWithHistory.put(
                                neighbour,
                                new ScoreWithHistory(newNeighbourScore, newPath));
                    }

                    queue.add(neighbour);
                }

                visitedNodes.add(vertex);
            }

            return scoresWithHistory;
        }

        private Coordinates pollWithMinimalScore(
                Set<Coordinates> queue,
                Map<Coordinates, ScoreWithHistory> scores) {
            return queue.stream()
                    .min(Comparator.comparingLong(v -> scores.get(v).score))
                    .map(coordinates -> {
                        queue.remove(coordinates);
                        return coordinates;
                    })
                    .orElseThrow();
        }

        public Set<Coordinates> getNeighbours(Coordinates position) {
            Set<Coordinates> results = new HashSet<>();
            for (var direction : Direction.values()) {
                Coordinates maybeNeighbour = position.sum(direction.shift);
                getNotObstacle(maybeNeighbour)
                        .ifPresent(neighbour ->
                                results.add(maybeNeighbour));
            }

            return results;
        }

        private Optional<Character> getNotObstacle(Coordinates coordinates) {
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
            Field field, Coordinates startPosition, Coordinates endPosition, int threshold) {
        // for untouched field result is always unique
        ScoreWithHistory baseResult = field.findShortestPath(startPosition, endPosition, true)
                .orElseThrow();

        return findObstaclesToRemove(baseResult.historyPath, 20, threshold);
    }

    private static long findObstaclesToRemove(List<Coordinates> path,
                                              int cheatCodeSize,
                                              int improvementThreshold) {
        Map<Integer, Long> improvementCounts = new HashMap<>();
        for (int currentPos = 0; currentPos < path.size(); ++currentPos) {
            for (int otherPosFromStart = 0; otherPosFromStart < path.size(); ++otherPosFromStart) {
                if (currentPos == otherPosFromStart) {
                    continue;
                }

                int cheatDistance = path.get(currentPos).distance(path.get(otherPosFromStart));
                if (cheatDistance > cheatCodeSize) {
                    continue;
                }

                int newDistanceFromStart = currentPos + cheatDistance;
                if (otherPosFromStart - newDistanceFromStart >= improvementThreshold) {
                    improvementCounts.merge(otherPosFromStart - newDistanceFromStart, 1L, Long::sum);
                }
            }
        }

        System.out.println(improvementCounts.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).toList());
        return improvementCounts.values()
                .stream()
                .reduce(0L, Long::sum);
    }
}

