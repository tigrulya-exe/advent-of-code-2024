package exe.tigrulya.day3;

import static exe.tigrulya.Utils.getResource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Task2 {
    private static final RegexFinder BASE_MATCHER =
        new RegexFinder("(?<=mul\\()(\\d+?),(\\d+?)(?=\\))");

    private static final RegexFinder DO_MATCHER =
        new RegexFinder("do\\(\\)");

    private static final RegexFinder DONT_MATCHER =
        new RegexFinder("don't\\(\\)");

    private static boolean baseAllowance = true;

    public static void main(String[] args) throws IOException {
        try (var lines = Files.lines(getResource("input/3_2.txt"))) {
            long result = lines.flatMap(Task2::getMultiplicationResults)
                .reduce(0L, Long::sum);

            System.out.println("Result: " + result);
        }
    }

    private static Stream<Long> getMultiplicationResults(String line) {
        List<Interval> skipIntervals = getSkipIntervals(line);

        return BASE_MATCHER.results(line)
            .filter(matchResult -> skipIntervals.stream()
                .noneMatch(interval -> interval.isInside(matchResult)))
            .map(matchResult -> Long.parseLong(matchResult.group(1))
                * Long.parseLong(matchResult.group(2)));
    }

    private static List<Interval> getSkipIntervals(String line) {
        NavigableMap<Integer, Integer> doFuncIdxs = DO_MATCHER.resultIndexes(line)
            .collect(Collectors.toMap(
                Function.identity(),
                Function.identity(),
                (l, r) -> l,
                TreeMap::new
            ));

        List<Interval> results = DONT_MATCHER.resultIndexes(line)
            .map(dontFuncIdx -> getSkipInterval(doFuncIdxs, dontFuncIdx))
            .collect(Collectors.toCollection(ArrayList::new));

        if (!baseAllowance) {
            results.add(getSkipInterval(doFuncIdxs, 0));
        }

        baseAllowance = results.stream()
            .noneMatch(interval -> interval.end == Integer.MAX_VALUE);

        return results;
    }

    private static Interval getSkipInterval(NavigableMap<Integer, Integer> doFuncIdxs, int intervalStart) {
        int intervalEnd = Optional.ofNullable(doFuncIdxs.ceilingEntry(intervalStart))
            .map(Map.Entry::getValue)
            .orElse(Integer.MAX_VALUE);

        return new Interval(intervalStart, intervalEnd);
    }

    public record Interval(int start, int end) {
        public boolean isInside(MatchResult matchResult) {
            return matchResult.start() > start
                && matchResult.start() < end;
        }
    }

    public static class RegexFinder {
        private final Matcher matcher;

        public RegexFinder(String regex) {
            Pattern pattern = Pattern.compile(regex);
            this.matcher = pattern.matcher("");
        }

        public Stream<MatchResult> results(String str) {
            matcher.reset(str);
            return matcher.results();
        }

        public Stream<Integer> resultIndexes(String str) {
            return results(str)
                .map(MatchResult::start);
        }
    }
}

