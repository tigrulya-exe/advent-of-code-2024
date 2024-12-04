package exe.tigrulya.day4;

import java.io.IOException;
import java.nio.file.Files;

import static exe.tigrulya.Utils.getResource;

public class Task2 {
    public static void main(String[] args) throws IOException {
        try (var lines = Files.lines(getResource("input/4_2.txt"))) {
            char[][] field = lines.map(String::toCharArray)
                    .toArray(char[][]::new);

            var wordFinder = new CellFinder(field, 'A', "MAS");
            System.out.println("Result: " + wordFinder.cellsCount());
        }
    }


    public static class CellFinder {
        private final char[][] field;
        private final char kernel;
        private final char[] word;

        public CellFinder(char[][] field, char kernel, String word) {
            this.field = field;
            this.kernel = kernel;
            this.word = word.toCharArray();
        }

        private long cellsCount() {
            long cellsCount = 0;
            int margin = word.length / 2;

            for (int y = margin; y < field.length - margin; ++y) {
                for (int x = margin; x < field[y].length - margin; ++x) {
                    if (field[y][x] != kernel) {
                        continue;
                    }

                    if ((// word starting from bottom left point
                            isWordOccurrence(x - 1, y + 1, 1, -1)
                                    // word starting from top right point
                                    || isWordOccurrence(x + 1, y - 1, -1, 1))
                                    // word starting from top left point
                                    && ((isWordOccurrence(x - 1, y - 1, 1, 1)
                                    // word starting from bottom right point
                                    || isWordOccurrence(x + 1, y + 1, -1, -1)))) {
                        ++cellsCount;
                    }
                }
            }

            return cellsCount;
        }

        private boolean isWordOccurrence(int x, int y, int xShift, int yShift) {
            int wordLetter = 0;

            while (wordLetter < word.length) {
                if (!isSafeX(x) || !isSafeY(y) || field[y][x] != word[wordLetter++]) {
                    return false;
                }

                x += xShift;
                y += yShift;
            }

            return true;
        }

        private boolean isSafeY(int idx) {
            return idx < field.length && idx >= 0;
        }

        private boolean isSafeX(int idx) {
            return field.length > 0 && idx < field[0].length && idx >= 0;
        }
    }
}

