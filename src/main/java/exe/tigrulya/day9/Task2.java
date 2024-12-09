package exe.tigrulya.day9;

import static exe.tigrulya.Utils.getResource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class Task2 {
    public record Block(boolean isFile, int size, Integer id) {
    }

    public static void main(String[] args) throws IOException {
        try (var lines = Files.lines(getResource("input/9.txt"))) {
            long totalSum = lines.map(Task2::decode)
                .mapToLong(Task2::checksum)
                .sum();

            System.out.println("Result: " + totalSum);
        }
    }

    private static List<Block> decodeBlocks(String encodedString) {
        boolean isFileSize = true;
        int id = 0;
        List<Block> result = new ArrayList<>();

        for (var ch : encodedString.toCharArray()) {
            int size = Integer.parseInt(String.valueOf(ch));
            result.add(new Block(isFileSize, size, isFileSize ? id++ : null));
            isFileSize = !isFileSize;
        }

        return result;
    }

    private static long defragmentBlocks(List<Block> decodedBlocks) {
        int firstFreePosition = 0;
        int lastFilePosition = decodedBlocks.size() - 1;

        while (firstFreePosition < lastFilePosition) {
            while (firstFreePosition < decodedBlocks.size()
                && decodedBlocks.get(firstFreePosition).isFile()) {
                ++firstFreePosition;
            }

            while (lastFilePosition >= 0
                && !decodedBlocks.get(lastFilePosition).isFile()
                && decodedBlocks.get(lastFilePosition).size <= decodedBlocks.get(firstFreePosition).size) {
                --lastFilePosition;
            }

            System.out.println("first: " + firstFreePosition + " last: " + lastFilePosition);
            if (firstFreePosition >= lastFilePosition) {
                break;
            }

            Block tmpFirst = decodedBlocks.get(firstFreePosition);
            decodedBlocks.set(firstFreePosition, decodedBlocks.get(lastFilePosition));
            decodedBlocks.set(lastFilePosition, tmpFirst);
        }

        return 0L;
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
        defragment(decodedFiles);
        return IntStream.range(0, decodedFiles.size())
            .mapToLong(i -> (long) i * Optional.ofNullable(decodedFiles.get(i)).orElse(0))
            .sum();
    }

    private static void defragment(List<Integer> decodedFiles) {
        int fileBlockIter = decodedFiles.size() - 1;
        int fileBlockSize = 0;

        int lastFileId = -1;

//        print(decodedFiles);

        while (fileBlockIter >= 0) {
            int freeBlockSize = 0;

            while (fileBlockIter >= 0
                && decodedFiles.get(fileBlockIter) == null) {
                --fileBlockIter;
            }

            while (fileBlockIter >= 0
                && decodedFiles.get(fileBlockIter) != null
                && (lastFileId == -1 | decodedFiles.get(fileBlockIter) == lastFileId)) {
                lastFileId = decodedFiles.get(fileBlockIter);
                ++fileBlockSize;
                --fileBlockIter;
            }

            for (int freeBlockIter = 0; freeBlockIter < fileBlockIter; ++freeBlockIter) {
                if (decodedFiles.get(freeBlockIter) != null) {
                    freeBlockSize = 0;
                    continue;
                }

                ++freeBlockSize;
                if (freeBlockSize >= fileBlockSize) {
                    swapN(decodedFiles,
                        fileBlockIter + 1,
                        freeBlockIter + 1 - freeBlockSize,
                        fileBlockSize
                    );
                    break;
                }
            }

//            print(decodedFiles);
            fileBlockSize = 0;
            lastFileId = -1;
        }
    }


    private static void print(List<Integer> values) {
        for (var val: values) {
            System.out.print(val == null ? "." : val + "");
        }
        System.out.println();
    }

    private static void swapN(List<Integer> values, int i, int j, int num) {
        for (int k = 0; k < num; ++k) {
            Integer tmpFirst = values.get(i + k);
            values.set(i + k, values.get(j + k));
            values.set(j + k, tmpFirst);
        }
    }

    private static void fill(List<Integer> result, int count, Integer value) {
        while (count-- > 0) {
            result.add(value);
        }
    }
}

