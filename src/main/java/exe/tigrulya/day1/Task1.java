package exe.tigrulya.day1;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static exe.tigrulya.Utils.getResource;

public class Task1 {
    public static void main(String[] args) throws IOException {
        List<Long> left = new ArrayList<>();
        List<Long> right = new ArrayList<>();

        try (var lines = Files.lines(getResource("input/1.txt"))) {
            lines.map(line -> line.split("\\s+"))
                    .forEach(numbers -> {
                        left.add(Long.parseLong(numbers[0]));
                        right.add(Long.parseLong(numbers[1]));
                    });
        }

        System.out.println("Result: " + getSum(left, right));
    }


    private static long getSum(List<Long> left, List<Long> right) {
        left.sort(Long::compareTo);
        right.sort(Long::compareTo);
        return IntStream.range(0, left.size())
                .mapToLong(i -> Math.abs(left.get(i) - right.get(i)))
                .reduce(0L, Long::sum);
    }
}

