package exe.tigrulya.day2;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static exe.tigrulya.Utils.getResource;

public class Task1 {
    public static void main(String[] args) throws IOException {
        try (var lines = Files.lines(getResource("input/2_1.txt"))) {
            long safeReports = lines.map(Task1::parseReport)
                    .filter(Task1::isSafeReport)
                    .count();

            System.out.println("Result: " + safeReports);
        }
    }

    private static List<Long> parseReport(String line) {
        String[] tokens = line.split("\\s+");
        return Arrays.stream(tokens)
                .map(Long::parseLong)
                .toList();
    }

    private static boolean isSafeReport(List<Long> levels) {
        int sign = 0;
        for (int i = 1; i < levels.size(); ++i) {
            long diff = Math.abs(levels.get(i) - levels.get(i - 1));
            int newSign = (int) Math.signum(levels.get(i) - levels.get(i - 1));

            if (diff > 3 || diff < 1 || newSign + sign == 0) {
                return false;
            }

            sign = newSign;
        }

        return true;
    }
}

