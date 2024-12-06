package exe.tigrulya.day6;

import static exe.tigrulya.Utils.getResource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Task1 {
    public static class Cell {
        private final char value;
        private boolean visited;

        public Cell(char value) {
            this.value = value;
        }

        public void visit() {
            visited = true;
        }
    }

    public static class Vector {
        private int x;
        private int y;

        public Vector(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void rotateRight() {
            int oldX = x;
            x = -y;
            y = oldX;
        }

        public void sum(Vector other) {
            this.x += other.x;
            this.y += other.y;
        }

        public void minus(Vector other) {
            this.x -= other.x;
            this.y -= other.y;
        }
    }

    public static void main(String[] args) throws IOException {
        int currentX = 0;
        int currentY = 0;

        int guardianX = 0;
        int guardianY = 0;

        List<List<Cell>> map = new ArrayList<>();
        try (var lines = Files.lines(getResource("input/6_1.txt"))) {
            Iterable<String> linesIterable = lines::iterator;
            for (var line : linesIterable) {
                currentX = 0;

                List<Cell> row = new ArrayList<>();
                for (var ch : line.toCharArray()) {
                    if ('^' == ch) {
                        guardianX = currentX;
                        guardianY = currentY;
                    }
                    ++currentX;
                    row.add(new Cell(ch));
                }
                ++currentY;
                map.add(row);
            }

            long visitedCells = new Guard(new CellMap(map)).check(guardianX, guardianY);
            System.out.println("Result: " + visitedCells);
        }
    }

    public static class Guard {
        private final CellMap map;

        public Guard(CellMap map) {
            this.map = map;
        }

        public long check(int startX, int startY) {
            Vector shiftVector = new Vector(0, -1);
            Vector coordinates = new Vector(startX, startY);

            Optional<Cell> currentCell = map.getCell(coordinates);
            while (currentCell.isPresent()) {
                if (currentCell.get().value == '#') {
                    // go back
                    coordinates.minus(shiftVector);
                    shiftVector.rotateRight();
                } else {
                    currentCell.get().visit();
                }
                coordinates.sum(shiftVector);
                currentCell = map.getCell(coordinates);
            }

            return map.map.stream()
                .map(row -> row.stream().filter(cell -> cell.visited).count())
                .reduce(Long::sum)
                .orElse(0L);
        }
    }

    public static class CellMap {
        private final List<List<Cell>> map;

        public CellMap(List<List<Cell>> map) {
            this.map = map;
        }

        public Optional<Cell> getCell(Vector coordinates) {
            return getValue(map, coordinates.y)
                .flatMap(line -> getValue(line, coordinates.x));
        }

        private <T> Optional<T> getValue(List<T> list, int idx) {
            return Optional.of(list)
                .filter(ignore -> idx < list.size() && idx >= 0)
                .map(l -> l.get(idx));
        }
    }
}

