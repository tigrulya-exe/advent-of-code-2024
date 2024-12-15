package exe.tigrulya.day15;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

        public Coordinates move(Coordinates position, Move move) {
            return tryMove(position, move) ? position.sum(move.shift) : position;
        }

        public List<Coordinates> locationsOf(char character) {
            List<Coordinates> locations = new ArrayList<>();
            for (int y = 0; y < rows(); ++y) {
                for (int x = 0; x < columns(); ++x) {
                    if (unsafeGet(x, y) == character) {
                        locations.add(new Coordinates(x, y));
                    }
                }
            }

            return locations;
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

        private boolean tryMove(Coordinates position, Move move) {
            // we are at free position
            if (get(position).map(ch -> ch == '.').orElse(false)) {
                return true;
            }

            // we are at wall now
            if (get(position).map(ch -> ch == '#').orElse(true)) {
                return false;
            }

            Coordinates newPosition = position.sum(move.shift);
            if (tryMove(position.sum(move.shift), move)) {
                unsafeSet(newPosition, unsafeGet(position.x, position.y));
                unsafeSet(position, '.');
                return true;
            }

            return false;
        }

        private void unsafeSet(Coordinates coordinates, char value) {
            field.get(coordinates.y).set(coordinates.x, value);
        }

        private char unsafeGet(int x, int y) {
            return field.get(y).get(x);
        }

        private Optional<Character> get(Coordinates coordinates) {
            return get(field, coordinates.y)
                    .flatMap(row -> get(row, coordinates.x));
        }

        private <T> Optional<T> get(List<T> values, int idx) {
            return Optional.of(values)
                    .filter(ignore -> idx >= 0 && idx < values.size())
                    .map(ignore -> values.get(idx));
        }
    }

    public enum Move {
        UP(0, -1),
        DOWN(0, 1),
        LEFT(-1, 0),
        RIGHT(1, 0);

        private final Coordinates shift;

        Move(int xShift, int yShift) {
            this.shift = new Coordinates(xShift, yShift);
        }

        public static Move from(char symbol) {
            return switch (symbol) {
                case '^' -> UP;
                case 'v' -> DOWN;
                case '>' -> RIGHT;
                case '<' -> LEFT;
                default -> throw new IllegalStateException("Unexpected move symbol: " + symbol);
            };
        }
    }

    public static void main(String[] args) throws IOException {
        boolean delimiterHandled = false;

        List<List<Character>> field = new ArrayList<>();
        List<Move> moves = new ArrayList<>();
        Coordinates robotPosition = new Coordinates(0, 0);
        int y = -1;

        try (var lines = Files.lines(getResource("input/15.txt"))) {
            Iterable<String> linesIterable = lines::iterator;
            for (var line : linesIterable) {
                ++y;
                if (line.isBlank()) {
                    delimiterHandled = true;
                    continue;
                }

                if (!delimiterHandled) {
                    List<Character> row = new ArrayList<>();
                    int x = 0;
                    for (var cell : line.toCharArray()) {
                        row.add(cell);
                        if (cell == '@') {
                            robotPosition = new Coordinates(x, y);
                        }
                        ++x;
                    }
                    field.add(row);
                    continue;
                }

                line.chars()
                        .mapToObj(ch -> Move.from((char) ch))
                        .forEach(moves::add);
            }

            long result = boxesCoordinatesSum(new Field(field), robotPosition, moves);
            System.out.println("Result: " + result);
        }
    }

    private static long boxesCoordinatesSum(Field field, Coordinates robotPosition, List<Move> moves) {
        for (var move : moves) {
//            System.out.println("Move: " + move);
            robotPosition = field.move(robotPosition, move);
//            field.print();
        }

        return field.locationsOf('O')
                .stream()
                .mapToLong(position -> position.y * 100L + position.x)
                .sum();
    }

}

