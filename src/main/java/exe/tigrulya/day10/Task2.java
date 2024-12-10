package exe.tigrulya.day10;

import static exe.tigrulya.Utils.getResource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class Task2 {
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
        List<Coordinates> trailheads = new ArrayList<>();
        List<List<Integer>> field = new ArrayList<>();

        int currentX = 0;
        int currentY = 0;

        try (var lines = Files.lines(getResource("input/10.txt"))) {
            Iterable<String> linesIterable = lines::iterator;
            for (var line : linesIterable) {
                List<Integer> row = new ArrayList<>();
                for (var ch : line.toCharArray()) {
                    int numericValue = Character.getNumericValue(ch);
                    row.add(numericValue);
                    if (numericValue == 0) {
                        trailheads.add(new Coordinates(currentX, currentY));
                    }
                    ++currentX;
                }

                field.add(row);
                ++currentY;
                currentX = 0;
            }
        }

        long result = countScores(new Field(field), trailheads);
        System.out.println("Result: " + result);
    }

    private static long countScores(Field field, List<Coordinates> trailheads) {
        return trailheads.stream()
            .mapToLong(trailhead -> trailsCount(field, trailhead))
            .sum();
    }

    private static long trailsCount(Field field, Coordinates current) {
        List<Coordinates> nextCoordinates = field.getNext(current);
        if (nextCoordinates.isEmpty()) {
            return field.get(current)
                .filter(height -> height == 9)
                .map(ignore -> 1L)
                .orElse(0L);
        }

        return nextCoordinates.stream()
            .mapToLong(coord -> trailsCount(field, coord))
            .sum();
    }
}

