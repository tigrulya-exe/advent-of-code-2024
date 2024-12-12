package exe.tigrulya.day12;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static exe.tigrulya.Utils.getResource;

public class Task2 {
    public record Coordinates(int x, int y) {
        public Coordinates sum(Coordinates other) {
            return new Coordinates(x + other.x, y + other.y);
        }
    }

    public record Field(List<List<Character>> field) {
        public int rows() {
            return field.size();
        }

        public int columns() {
            return field.getFirst().size();
        }

        public Optional<Character> get(Coordinates coordinates) {
            return get(field, coordinates.y)
                    .flatMap(row -> get(row, coordinates.x));
        }

        public Map<Boolean, List<Coordinates>> samePlants(Coordinates coordinates) {
            Optional<Character> currentPlant = get(coordinates);
            return currentPlant.map(plant -> Stream.of(
                                    new Coordinates(0, 1),
                                    new Coordinates(1, 0),
                                    new Coordinates(0, -1),
                                    new Coordinates(-1, 0)
                            ).map(diff -> diff.sum(coordinates))
                            .collect(Collectors.partitioningBy(nextCoords -> get(nextCoords)
                                    .filter(neighbor -> neighbor == plant)
                                    .isPresent())))
                    .orElseGet(Map::of);
        }

        private <T> Optional<T> get(List<T> values, int idx) {
            return Optional.of(values)
                    .filter(ignore -> idx >= 0 && idx < values.size())
                    .map(ignore -> values.get(idx));
        }
    }

    public static void main(String[] args) throws IOException {
        try (var lines = Files.lines(getResource("input/12.txt"))) {
            List<List<Character>> field = lines.map(line -> line.chars()
                            .mapToObj(intCh -> (char) intCh)
                            .toList())
                    .toList();


            long result = findAreaPrices(new Field(field));
            System.out.println("Result: " + result);
        }
    }

    private static long findAreaPrices(Field field) {
        Set<Coordinates> newCoordinates = IntStream.range(0, field.rows())
                .boxed()
                .flatMap(y -> IntStream.range(0, field.columns())
                        .mapToObj(x -> new Coordinates(x, y)))
                .collect(Collectors.toSet());

        long areaPrices = 0;
        while (!newCoordinates.isEmpty()) {
            Coordinates nextPlant = newCoordinates.stream().findAny().get();
            areaPrices += findAreaPrice(field, nextPlant, newCoordinates);
        }
        return areaPrices;
    }

    private static long findAreaPrice(Field field, Coordinates coordinates, Set<Coordinates> newCoordinates) {
        Deque<Coordinates> plantsQueue = new ArrayDeque<>();
        plantsQueue.addFirst(coordinates);

        long plantsCount = 0;

        Map<Integer, Set<Coordinates>> xSides = new HashMap<>();
        Map<Integer, Set<Coordinates>> ySides = new HashMap<>();

        while (!plantsQueue.isEmpty()) {
            var plantCoords = plantsQueue.pollFirst();
            ++plantsCount;

            Map<Boolean, List<Coordinates>> neighbours = field.samePlants(plantCoords);
            List<Coordinates> samePlants = Optional.ofNullable(neighbours.get(true))
                    .orElseGet(List::of);

            Optional.ofNullable(neighbours.get(false))
                    .orElseGet(List::of)
                    .forEach(plant -> {
                        if (plant.x != plantCoords.x && isNewXSide(xSides, plant)) {
                            xSides.computeIfAbsent(plant.x, ignore -> new HashSet<>())
                                    .add(plantCoords);
                        }
                        if (plant.y != plantCoords.y && isNewYSide(ySides, plant)) {
                            ySides.computeIfAbsent(plant.y, ignore -> new HashSet<>())
                                    .add(plant);
                        }
                    });

            newCoordinates.remove(plantCoords);
            samePlants.stream()
                    .filter(newCoordinates::contains)
                    .forEach(plant -> {
                        plantsQueue.addFirst(plant);
                        newCoordinates.remove(plant);
                    });
        }

        String message = "For plants '%s' plantsCount = %d, sides = %d, xSides = %d, ySides = %d".formatted(
                field.get(coordinates).get(),
                plantsCount,
                xSides.size() + ySides.size(),
                xSides.size(),
                ySides.size()
        );
        System.out.println(message);
        // xSides.size() + ySides.size() == sides
        return plantsCount * (xSides.size() + ySides.size());
    }

    private static boolean isNewXSide(Map<Integer, Set<Coordinates>> alreadySeenCoordinates, Coordinates coordinate) {
        Set<Coordinates> xSide = alreadySeenCoordinates.getOrDefault(coordinate.x, Set.of());
        return xSide.isEmpty() || xSide.stream()
                .anyMatch(oldCoordinate -> oldCoordinate.x == coordinate.x &&
                        Math.abs(oldCoordinate.y - coordinate.y) > 1);
    }

    private static boolean isNewYSide(Map<Integer, Set<Coordinates>> alreadySeenCoordinates, Coordinates coordinate) {
        Set<Coordinates> ySide = alreadySeenCoordinates.getOrDefault(coordinate.y, Set.of());
        return ySide.isEmpty() || ySide.stream()
                .anyMatch(oldCoordinate -> oldCoordinate.y == coordinate.y &&
                        Math.abs(oldCoordinate.x - coordinate.x) > 1);
    }
}

