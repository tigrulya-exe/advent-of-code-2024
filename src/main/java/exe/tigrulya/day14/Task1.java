package exe.tigrulya.day14;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static exe.tigrulya.Utils.getResource;

public class Task1 {
    public record Coordinates(int x, int y) {
        public Coordinates sum(Coordinates other) {
            return new Coordinates(x + other.x, y + other.y);
        }
    }

    public static class Robot {
        private Coordinates position;
        private final Coordinates shift;

        public Robot(Coordinates position, Coordinates shift) {
            this.position = position;
            this.shift = shift;
        }
    }

    public static class Field {
        private final List<List<Long>> field;
        private final List<Robot> robots;
        private final int rows;
        private final int columns;

        public Field(List<List<Long>> field, List<Robot> robots) {
            this.field = field;
            this.robots = robots;
            this.rows = field.size();
            this.columns = field.get(0).size();

            robots.forEach(robot -> changeRobotsCount(robot.position, 1));
        }

        public int rows() {
            return field.size();
        }

        public int columns() {
            return field.getFirst().size();
        }

        public long safetyScore(int seconds) {
            while (seconds-- > 0) {
                for (var robot : robots) {
                    robot.position = move(robot.position, robot.shift);
                }
                System.out.println("================= Iteration: " + seconds);
                System.out.println(this);
            }

            return safetyScore();
        }

        private long safetyScore() {
            int middleRow = rows / 2;
            int middleColumn = columns / 2;
            return robotsCount(0, middleRow, 0, middleColumn)
                    * robotsCount(0, middleRow, middleColumn + 1, columns)
                    * robotsCount(middleRow + 1, rows, 0, middleColumn)
                    * robotsCount(middleRow + 1, rows, middleColumn + 1, columns);
        }

        private long robotsCount(int minRow, int maxRow, int minCol, int maxCol) {
            return IntStream.range(minRow, maxRow)
                    .boxed()
                    .flatMap(rowNum -> IntStream.range(minCol, maxCol)
                            .mapToObj(colNum -> getRobotsCount(colNum, rowNum)))
                    .mapToLong(v -> v)
                    .sum();
        }

        private Coordinates move(Coordinates origin, Coordinates shift) {
            Coordinates newPosition = new Coordinates(
                    (origin.x + shift.x + columns) % columns,
                    (origin.y + shift.y + rows) % rows
            );

            changeRobotsCount(origin, -1);
            changeRobotsCount(newPosition, 1);
            return newPosition;
        }

        private void changeRobotsCount(Coordinates coordinates, long delta) {
            List<Long> row = field.get(coordinates.y);
            row.set(coordinates.x, row.get(coordinates.x) + delta);
        }

        private long getRobotsCount(int x, int y) {
            return field.get(y).get(x);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (var row : field) {
                for (var column : row) {
//                    builder.append(column == 0 ? "." : column);
                    builder.append(column == 0 ? "." : "*");
                }
                builder.append("\n");
            }

            return builder.toString();
        }
    }


    public static void main(String[] args) throws IOException {
        try (var lines = Files.lines(getResource("input/14.txt"))) {
            List<Robot> robots = lines.map(Task1::parseRobot)
                    .toList();

            Field field = new Field(getBaseField(103, 101), robots);

            long result = field.safetyScore(3000);
            System.out.println("Result: " + result);
        }
    }

    public static final Pattern ROBOTS_PATTERN = Pattern.compile("-?\\d+,-?\\d+");
    public static Robot parseRobot(String rawRobot) {
        List<MatchResult> matchResult = ROBOTS_PATTERN.matcher(rawRobot).results().toList();
        String[] rawPosition = matchResult.get(0).group().split(",");
        String[] rawShift = matchResult.get(1).group().split(",");
        return new Robot(
                new Coordinates(Integer.parseInt(rawPosition[0]), Integer.parseInt(rawPosition[1])),
                new Coordinates(Integer.parseInt(rawShift[0]), Integer.parseInt(rawShift[1]))
        );
    }

    public static List<List<Long>> getBaseField(int rows, int columns) {
        return IntStream.range(0, rows)
                .mapToObj(idx -> IntStream.range(0, columns)
                        .mapToObj(ignore -> 0L)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }
}

