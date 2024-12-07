package exe.tigrulya.day4;

import java.io.IOException;
import java.nio.file.Files;

import static exe.tigrulya.Utils.getResource;

public class Task1 {
    public static void main(String[] args) throws IOException {
        try (var lines = Files.lines(getResource("input/4.txt"))) {
            char[][] field = lines.map(String::toCharArray)
                    .toArray(char[][]::new);

            var wordFinder = new WordFinder(field, "XMAS");
            System.out.println("Result: " + wordFinder.countWord());
        }
    }


    public static class WordFinder {
        private final char[][] field;
        private final char[] word;

        public WordFinder(char[][] field, String word) {
            this.field = field;
            this.word = word.toCharArray();
        }

        private long countWord() {
            long wordsCount = 0;

            for (int y = 0; y < field.length; ++y) {
                for (int x = 0; x < field[y].length; ++x) {
                    // move in x-axis
                    wordsCount += wordOccurrences(x, y, 1, 0);
                    wordsCount += wordOccurrences(x, y, -1, 0);

                    // move in y-axis
                    wordsCount += wordOccurrences(x, y, 0, 1);
                    wordsCount += wordOccurrences(x, y, 0, -1);

                    // move diagonally
                    wordsCount += wordOccurrences(x, y, -1, 1);
                    wordsCount += wordOccurrences(x, y, 1, 1);
                    wordsCount += wordOccurrences(x, y, 1, -1);
                    wordsCount += wordOccurrences(x, y, -1, -1);
                }
            }

            return wordsCount;
        }

        private int wordOccurrences(int x, int y, int xShift, int yShift) {
            int wordLetter = 0;

            while (wordLetter < word.length) {
                if (!isSafeX(x) || !isSafeY(y) || field[y][x] != word[wordLetter++]) {
                    return 0;
                }
                
                x += xShift;
                y += yShift;
            }
            
            return 1;
        }

        private boolean isSafeY(int idx) {
            return idx < field.length && idx >= 0;
        }

        private boolean isSafeX(int idx) {
            return field.length > 0 && idx < field[0].length && idx >= 0;
        }
    }
}

