package exe.tigrulya.day13;

import static exe.tigrulya.Utils.getResource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class Task2 {
    public record LongVector(long x, long y) {
    }

    private record Rule(Vector aButtonShift,
                        Vector bButtonShift,
                        Vector prizeLocation) {
    }

    public record Vector(double x, double y) {
        private static final double DELTA = 1e-4;

        public Vector sum(Vector other) {
            return new Vector(x + other.x, y + other.y);
        }

        public Optional<LongVector> asLong() {
            if (isLong(x) && isLong(y)) {
                return Optional.of(new LongVector(Math.round(x), Math.round(y)));
            }
            return Optional.empty();
        }

        private boolean isLong(double value) {
            return Math.abs(Math.round(value) - value) <= DELTA;
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
            Double.parseDouble(split[0]) + 10000000000000.0,
            Double.parseDouble(split[1]) + 10000000000000.0
        );
    }

    private static long requiredTokens(List<Rule> rules) {
        return rules.stream()
            .map(Task2::getSolutionFast)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .mapToLong(v -> v.x * 3 + v.y)
            .sum();
    }

    private static Optional<LongVector> getSolutionFast(Rule rule) {
        double y = (rule.aButtonShift.x * rule.prizeLocation.y - rule.prizeLocation.x * rule.aButtonShift.y)
            / (rule.aButtonShift.x * rule.bButtonShift.y - rule.bButtonShift.x * rule.aButtonShift.y);

        double x = (rule.prizeLocation.x - rule.bButtonShift.x * y) / rule.aButtonShift.x;

        return new Vector(x, y).asLong();
    }
}

