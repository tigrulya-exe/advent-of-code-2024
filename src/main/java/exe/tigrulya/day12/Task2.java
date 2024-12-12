package exe.tigrulya.day12;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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

    public record SideCoordinates(int x, int y, boolean direction) {
        public Coordinates sum(Coordinates other) {
            return new Coordinates(x + other.x, y + other.y);
        }

        public static SideCoordinates from(Coordinates base, Coordinates neighbour) {
            boolean direction = neighbour.x == base.x && neighbour.y > base.y
                    || neighbour.y == base.y && neighbour.x > base.x;
            return new SideCoordinates(neighbour.x, neighbour.y, direction);
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

        public Map<Boolean, List<Coordinates>> neighbours(Coordinates coordinates) {
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
            areaPrices += findAreaPriceNew(field, nextPlant, newCoordinates);
        }
        return areaPrices;
    }

    private static long findAreaPriceNew(Field field, Coordinates coordinates, Set<Coordinates> newCoordinates) {
        Deque<Coordinates> plantsQueue = new ArrayDeque<>();
        plantsQueue.addFirst(coordinates);

        long plantsCount = 0;

        List<SideCoordinates> xSides = new ArrayList<>();
        List<SideCoordinates> ySides = new ArrayList<>();

        while (!plantsQueue.isEmpty()) {
            var plantCoords = plantsQueue.pollFirst();
            ++plantsCount;

            Map<Boolean, List<Coordinates>> neighbours = field.neighbours(plantCoords);
            List<Coordinates> samePlants = Optional.ofNullable(neighbours.get(true))
                    .orElseGet(List::of);

            Optional.ofNullable(neighbours.get(false))
                    .orElseGet(List::of)
                    .forEach(plant -> {
                        if (plant.x != plantCoords.x) {
                            xSides.add(SideCoordinates.from(plantCoords, plant));
                        }
                        if (plant.y != plantCoords.y) {
                            ySides.add(SideCoordinates.from(plantCoords, plant));
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

        long xSidesCount = calculateSides(xSides, SideCoordinates::x, SideCoordinates::y);
        long ySidesCount = calculateSides(ySides, SideCoordinates::y, SideCoordinates::x);

        String message = "For plants '%s' plantsCount = %d, sides = %d, xSides = %d, ySides = %d".formatted(
                field.get(coordinates).get(),
                plantsCount,
                xSidesCount + ySidesCount,
                xSidesCount,
                ySidesCount
        );
        System.out.println(message);

        return plantsCount * (xSidesCount + ySidesCount);
    }

    private static long calculateSides(List<SideCoordinates> sideCoordinates,
                                       Function<SideCoordinates, Integer> mainCoordProvider,
                                       Function<SideCoordinates, Integer> secondCoordProvider) {
        Comparator<SideCoordinates> comparator = Comparator.comparingInt(mainCoordProvider::apply)
                .thenComparing((a, b) -> Boolean.compare(a.direction, b.direction))
                .thenComparingInt(secondCoordProvider::apply);

        sideCoordinates.sort(comparator);

        long sides = 0;

        SideCoordinates previousCoord = null;

        for (var sideCoord : sideCoordinates) {
            if (previousCoord == null
                    || !mainCoordProvider.apply(sideCoord).equals(mainCoordProvider.apply(previousCoord))
                    || Math.abs(secondCoordProvider.apply(previousCoord) - secondCoordProvider.apply(sideCoord)) > 1
                    || !Objects.equals(previousCoord.direction, sideCoord.direction)) {
                ++sides;
            }
            previousCoord = sideCoord;
        }

        return sides;
    }
}

