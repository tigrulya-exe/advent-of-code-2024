package exe.tigrulya.day12;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static exe.tigrulya.Utils.getResource;

public class Task1 {
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

        public List<Coordinates> samePlants(Coordinates coordinates) {
            Optional<Character> currentPlant = get(coordinates);
            return currentPlant.map(plant -> Stream.of(
                                    new Coordinates(0, 1),
                                    new Coordinates(1, 0),
                                    new Coordinates(0, -1),
                                    new Coordinates(-1, 0)
                            ).map(diff -> diff.sum(coordinates))
                            .filter(nextCoords -> get(nextCoords)
                                    .filter(neighbor -> neighbor == plant)
                                    .isPresent())
                            .toList())
                    .orElseGet(List::of);
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
        long fencesCount = 0;

        while (!plantsQueue.isEmpty()) {
            var plantCoords = plantsQueue.pollFirst();
            ++plantsCount;

            List<Coordinates> samePlants = field.samePlants(plantCoords);
            fencesCount += 4 - samePlants.size();

            newCoordinates.remove(plantCoords);
            samePlants.stream()
                    .filter(newCoordinates::contains)
                    .forEach(plant -> {
                        plantsQueue.addLast(plant);
                        newCoordinates.remove(plant);
                    });
        }

        return plantsCount * fencesCount;
    }
}

