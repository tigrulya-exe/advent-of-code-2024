package exe.tigrulya.day3;

import static exe.tigrulya.Utils.getResource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Task1 {
    private static final Pattern PATTERN =
        Pattern.compile("(?<=mul\\()(\\d+?),(\\d+?)(?=\\))");

    private static final Matcher MATCHER =
        PATTERN.matcher("");

    public static void main(String[] args) throws IOException {
        try (var lines = Files.lines(getResource("input/3.txt"))) {
            long result = lines.flatMap(Task1::getMultiplicationResults)
                .reduce(0L, Long::sum);

            System.out.println("Result: " + result);
        }
    }

    private static Stream<Long> getMultiplicationResults(String line) {
        MATCHER.reset(line);
        return MATCHER.results()
            .map(matchResult -> Long.parseLong(matchResult.group(1))
                * Long.parseLong(matchResult.group(2)));
    }
}

