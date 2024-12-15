package exe.tigrulya.day14;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static exe.tigrulya.Utils.getResource;

public class Task2 {
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

        private record EntropyPair(double entropy, int idx) implements Comparable<EntropyPair> {
            @Override
            public int compareTo(EntropyPair o) {
                return Double.compare(entropy, o.entropy);
            }
        }

        public long safetyScore(int seconds) {
            double maxEntropy = Double.MIN_VALUE;
            int minEntropyIdx = 0;

            PriorityQueue<EntropyPair> entropies = new PriorityQueue<>(Comparator.reverseOrder());
            while (seconds-- > 0) {
                for (var robot : robots) {
                    robot.position = move(robot.position, robot.shift);
                }

                double entropy = entropy();
                if (entropy > maxEntropy) {
                    maxEntropy = entropy;
                    minEntropyIdx = seconds;
                }
                entropies.add(new EntropyPair(entropy, seconds));
//                saveImage(this, seconds, columns, rows);
            }

            System.out.println("entropy min: " + minEntropyIdx);

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

        private double entropy() {
            return field.stream()
                    .flatMap(List::stream)
                    // uniform distribution
                    .map(elem -> (double) elem / (rows * columns))
                    .mapToDouble(this::entropy)
                    .sum();
        }

        private double entropy(double p) {
            return p == 0 ? 0 : -p * Math.log(p);
        }

        public long getRobotsCount(int x, int y) {
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
            List<Robot> robots = lines.map(Task2::parseRobot)
                    .toList();

            Field field = new Field(getBaseField(103, 101), robots);

            long result = field.safetyScore(103 * 101);
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

    public static void saveImage(Field field, int iteration, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                long value = field.getRobotsCount(x, y);
                int color = (value == 0L) ? 0x000000 : 0xFFFFFF; // Black for 0, White for 1
                image.setRGB(x, y, color);
            }
        }

        try {
            File output = new File("D:\\IdeaProjects\\advent-of-code-2024\\src\\main\\resources\\imgs\\" + iteration + ".png");
            ImageIO.write(image, "png", output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

