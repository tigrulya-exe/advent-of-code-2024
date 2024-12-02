package exe.tigrulya.day2;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static exe.tigrulya.Utils.getResource;

public class Task2 {
    public static void main(String[] args) throws IOException {
        try (var lines = Files.lines(getResource("input/2_2.txt"))) {
            long safeReports = lines.map(Task2::parseReport)
                    .filter(report -> isSafeReport(report, -1))
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

    private static boolean isSafeReport(List<Long> levels, int positionToSkip) {
        int sign = 0;
        int lastIdx = 0;
        boolean isFirstNum = true;

        for (int i = 0; i < levels.size(); ++i) {
            if (positionToSkip == i) {
                continue;
            }

            if (isFirstNum) {
                isFirstNum = false;
                lastIdx = i;
                continue;
            }

            long diff = Math.abs(levels.get(i) - levels.get(lastIdx));
            int newSign = (int) Math.signum(levels.get(i) - levels.get(lastIdx));

            if (diff > 3 || diff < 1 || newSign + sign == 0) {
                return positionToSkip < levels.size() && isSafeReport(levels, positionToSkip + 1);
            }

            sign = newSign;
            lastIdx = i;
        }

        return true;
    }
}

