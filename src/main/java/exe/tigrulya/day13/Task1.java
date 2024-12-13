package exe.tigrulya.day13;

import static exe.tigrulya.Utils.getResource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class Task1 {
    public record LongVector(long x, long y) {
    }

    private record Rule(Vector aButtonShift,
                        Vector bButtonShift,
                        Vector prizeLocation) {}

    public record Vector(double x, double y) {
        private static final double DELTA = 1e-3;

        public Vector sum(Vector other) {
            return new Vector(x + other.x, y + other.y);
        }

        public Optional<LongVector> asLong() {
            if (isLong(x) && isLong(y)) {
                return Optional.of(new LongVector(Math.round(x), Math.round(y)));
//                    .filter(v -> v.x <= 100 && v.y <= 100);
            }
            return Optional.empty();
        }

        private boolean isLong(double value) {
            return Math.abs(Math.round(value) - value) <= DELTA;
        }
    }

    public record Matrix2(double[][] data) {
        public Optional<Matrix2> inverse() {
            return Optional.of(det())
                .filter(det -> det != 0)
                .map(det -> 1.0 / det)
                .map(val -> adjoint().multiply(val));
        }

        public double det() {
            return elem(0, 0) * elem(1, 1) -
                elem(0, 1) * elem(1, 0);
        }

        public double elem(int i, int j) {
            return data[i][j];
        }

        public Matrix2 multiply(double scalar) {
            return new Matrix2(
                new double[][] {
                    {scalar * elem(0, 0), scalar * elem(0, 1)},
                    {scalar * elem(1, 0), scalar * elem(1, 1)},
                }
            );
        }

        public Vector multiply(Vector vector) {
            return new Vector(
                elem(0, 0) * vector.x + elem(0, 1) * vector.y,
                elem(1, 0) * vector.x + elem(1, 1) * vector.y
            );
        }

        private Matrix2 adjoint() {
            return new Matrix2(
                new double[][] {
                    {elem(1, 1), -elem(0, 1)},
                    {-elem(1, 0), elem(0, 0)}
                }
            );
        }
    }


    public static void main(String[] args) throws IOException {
        List<Rule> rules = new ArrayList<>();
        try (var lines = Files.lines(getResource("input/13.txt"))) {
            Iterator<String> linesIterable = lines.iterator();
            while (linesIterable.hasNext()) {
                Vector aButtonRule = parseButton(linesIterable.next(), "A");
                Vector bButtonRule = parseButton(linesIterable.next(), "B");
                Vector prizeRule = parsePrize(linesIterable.next());

                if (linesIterable.hasNext()) {
                    linesIterable.next();
                }

                rules.add(new Rule(aButtonRule, bButtonRule, prizeRule));
            }

            long result = requiredTokens(rules);
            System.out.println("Result: " + result);
        }
    }

    private static Vector parseButton(String rule, String button) {
        String[] split = rule.replaceAll("(Button " + button + ":|X\\+| |Y\\+)", "")
            .split(",");
        return new Vector(Double.parseDouble(split[0]), Double.parseDouble(split[1]));
    }

    private static Vector parsePrize(String rule) {
        String[] split = rule.replaceAll("(Prize:|X=| |Y=)", "")
            .split(",");
        return new Vector(
            Double.parseDouble(split[0]),
            Double.parseDouble(split[1])
        );
    }

    private static long requiredTokens(List<Rule> rules) {
        return rules.stream()
            .map(Task1::getSolution)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .mapToLong(v -> v.x * 3 + v.y)
            .sum();
    }

    private static Optional<LongVector> getSolution(Rule rule) {
        Matrix2 aMatrix = new Matrix2(
            new double[][] {
                {rule.aButtonShift.x, rule.bButtonShift.x},
                {rule.aButtonShift.y, rule.bButtonShift.y},
            }
        );

        return aMatrix.inverse()
            .map(inversedA -> inversedA.multiply(rule.prizeLocation))
            .flatMap(Vector::asLong);
    }
}

