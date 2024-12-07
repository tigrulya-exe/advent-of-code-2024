package exe.tigrulya.day7;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import static exe.tigrulya.Utils.getResource;

public class Task2 {
    public record Equation(long result, List<Long> operands) {
        public static Equation parseFrom(String line) {
            String[] split = line.split(": ");
            List<Long> operands = Arrays.stream(split[1].split(" "))
                    .map(Long::parseLong)
                    .toList();

            return new Equation(Long.parseLong(split[0]), operands);
        }
    }

    public static void main(String[] args) throws IOException {
        try (var lines = Files.lines(getResource("input/7.txt"))) {
            long totalSum = lines.map(Equation::parseFrom)
                    .filter(equation -> new EquationSolver(equation).isPossible())
                    .mapToLong(equation -> equation.result)
                    .sum();

            System.out.println("Result: " + totalSum);
        }
    }

    public static class EquationSolver {
        private final Equation equation;

        public EquationSolver(Equation equation) {
            this.equation = equation;
        }

        public boolean isPossible() {
            return isPossible(0, 0, Long::sum);
        }


        private boolean isPossible(long partialResult,
                                   int currentOperandPos,
                                   BiFunction<Long, Long, Long> operator) {
            if (currentOperandPos == equation.operands.size()) {
                return partialResult == equation.result;
            }

            long operand = equation.operands.get(currentOperandPos);
            partialResult = operator.apply(partialResult, operand);

            if (partialResult > equation.result && currentOperandPos < equation.operands.size()) {
                return false;
            }

            return isPossible(partialResult, currentOperandPos + 1, Long::sum)
                    || isPossible(partialResult, currentOperandPos + 1, (a, b) -> a * b)
                    || isPossible(partialResult, currentOperandPos + 1, Task2::concatenate);
        }
    }

    public static long concatenate(long a, long b) {
        return Long.parseLong(String.valueOf(a) + b);
    }
}

