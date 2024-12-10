package exe.tigrulya.day9;

import static exe.tigrulya.Utils.getResource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Task1 {
    public static void main(String[] args) throws IOException {
        try (var lines = Files.lines(getResource("input/9.txt"))) {
            long totalSum = lines.map(Task1::decode)
                .mapToLong(Task1::checksum)
                .sum();

            System.out.println("Result: " + totalSum);
        }
    }

    private static List<Integer> decode(String encodedString) {
        boolean isFileSize = true;
        int id = 0;
        List<Integer> result = new ArrayList<>();

        for (var ch : encodedString.toCharArray()) {
            int size = Integer.parseInt(String.valueOf(ch));
            fill(result, size, isFileSize ? id++ : null);
            isFileSize = !isFileSize;
        }

        return result;
    }

    private static long checksum(List<Integer> decodedFiles) {
        int firstFreePosition = 0;
        int lastFilePosition = decodedFiles.size() - 1;

        while (firstFreePosition < lastFilePosition) {
            while (firstFreePosition < decodedFiles.size() && decodedFiles.get(firstFreePosition) != null) {
                ++firstFreePosition;
            }

            while (lastFilePosition >= 0 && decodedFiles.get(lastFilePosition) == null) {
                --lastFilePosition;
            }

            System.out.println("first: " + firstFreePosition + " last: " + lastFilePosition);
            if (firstFreePosition >= lastFilePosition) {
                break;
            }

            Integer tmpFirst = decodedFiles.get(firstFreePosition);
            decodedFiles.set(firstFreePosition, decodedFiles.get(lastFilePosition));
            decodedFiles.set(lastFilePosition, tmpFirst);
        }

        return IntStream.range(0, decodedFiles.size())
            .takeWhile(i -> decodedFiles.get(i) != null)
            .mapToLong(i -> (long) i * decodedFiles.get(i))
            .sum();
    }

    private static void fill(List<Integer> result, int count, Integer value) {
        while (count-- > 0) {
            result.add(value);
        }
    }
}

