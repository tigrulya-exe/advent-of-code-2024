package exe.tigrulya.day6;

import static exe.tigrulya.Utils.getResource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Task2 {
    public static class Cell {
        private char value;
        private boolean visited;
        private int visitedXShift;
        private int visitedYShift;

        public Cell(char value) {
            this.value = value;
        }

        public void visit(Vector shiftVector) {
            visited = true;
            visitedXShift = shiftVector.x;
            visitedYShift = shiftVector.y;
        }

        public boolean isVisitedWithShift(Vector shiftVector) {
            return visited && visitedXShift == shiftVector.x && visitedYShift == shiftVector.y;
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
            // rotate 90 degrees right using rotation matrix
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
        try (var lines = Files.lines(getResource("input/6.txt"))) {
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

            int possibleLoops = new Guard(new CellMap(map))
                .checkForLoops(guardianX, guardianY);
            System.out.println("Result: " + possibleLoops);
        }
    }

    public static class Guard {
        private final CellMap map;

        public Guard(CellMap map) {
            this.map = map;
        }

        public int checkForLoops(int startX, int startY) {
            Vector shiftVector = new Vector(0, -1);
            Vector coordinates = new Vector(startX, startY);

            int possibleLoops = 0;
            Optional<Cell> currentCell = map.getCell(coordinates);
            while (currentCell.isPresent()) {
                if (currentCell.get().value == '#') {
                    // go back
                    coordinates.minus(shiftVector);
                    shiftVector.rotateRight();
                } else {
                    if (!currentCell.get().visited && currentCell.get().value != '^') {
                        if (checkForLoop(map.withNewObstacle(coordinates), startX, startY)) {
                            ++possibleLoops;
                        }
                    }

                    currentCell.get().visit(shiftVector);
                }
                coordinates.sum(shiftVector);
                currentCell = map.getCell(coordinates);
            }

            return possibleLoops;
        }

        public boolean checkForLoop(CellMap cellMap, int currentX, int currentY) {
            Vector shiftVector = new Vector(0, -1);
            Vector coordinates = new Vector(currentX, currentY);

            Optional<Cell> currentCell = cellMap.getCell(coordinates);
            while (currentCell.isPresent()) {
                if (currentCell.get().isVisitedWithShift(shiftVector)) {
                    return true;
                }

                if (currentCell.get().value == '#') {
                    // go back
                    coordinates.minus(shiftVector);
                    shiftVector.rotateRight();
                } else {
                    currentCell.get().visit(shiftVector);
                }

                coordinates.sum(shiftVector);
                currentCell = cellMap.getCell(coordinates);
            }

            return false;
        }

    }

    public static class CellMap {
        private final List<List<Cell>> map;

        public CellMap(List<List<Cell>> map) {
            this.map = map;
        }

        public CellMap withNewObstacle(Vector coordinates) {
            List<List<Cell>> newMap = map.stream()
                .map(row -> row.stream()
                    .map(cell -> new Cell(cell.value))
                    .toList())
                .toList();
            CellMap newCellMap = new CellMap(newMap);
            newCellMap.getCell(coordinates).ifPresent(cell -> cell.value = '#');
            return newCellMap;
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

