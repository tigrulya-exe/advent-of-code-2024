package exe.tigrulya.day19;

import static exe.tigrulya.Utils.getResource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Task1 {

    public static class TrieNode {
        protected final Map<Character, TrieNode> children = new HashMap<>();
        private boolean isPossibleEnd = false;
        private final Character value;

        public TrieNode(Character value) {
            this.value = value;
        }

        public void markAsEndChar() {
            this.isPossibleEnd = true;
        }

        public TrieNode addChild(char ch) {
            return children.computeIfAbsent(ch, TrieNode::new);
        }

        public boolean contains(String string, int idx) {
            return (value == null || value.equals(string.charAt(idx)));
        }
    }

    public static class Trie extends TrieNode {
        private int maxDepth = 0;

        public Trie() {
            super(null);
        }

        public int sizeOfPrefix(String string) {
            int lastPossibleEnd = 0;

            TrieNode currentNode = this;
            for (int position = 0; position < string.length(); position++) {
                currentNode = currentNode.children.get(string.charAt(position));
                if (currentNode == null) {
                    return lastPossibleEnd;
                }

                if (currentNode.isPossibleEnd) {
                    lastPossibleEnd = position + 1;
                }
            }

            return string.length();
        }

        public void add(String... values) {
            for (String value : values) {
                add(value);
            }
        }

        public void add(String value) {
            TrieNode node = this;
            for (int i = 0; i < value.length(); i++) {
                node = node.addChild(value.charAt(i));
            }
            Optional.ofNullable(node)
                .ifPresent(TrieNode::markAsEndChar);

            maxDepth = Math.max(maxDepth, value.length());
        }
    }

    public static void main(String[] args) throws IOException {
        Trie trie = new Trie();

        Set<String> towels = new HashSet<>();

        boolean delimiterHandled = false;
        long result = 0;
        long simpleResult = 0;

        try (var lines = Files.lines(getResource("input/19.txt"))) {
            Iterable<String> linesIterable = lines::iterator;
            for (var line : linesIterable) {
                if (line.isBlank()) {
                    delimiterHandled = true;
                    continue;
                }

                if (!delimiterHandled) {
                    trie.add(line.split(", "));
                    towels = Stream.of(line.split(", ")).collect(Collectors.toSet());
                    continue;
                }

                int possible = isPossible(line, trie);
                if (possible == -1) {
                    ++result;
                } else {
                    System.out.println("Max matched " + possible + " for " + line);
                }

                if (simpleIsPossible(line, towels)) {
                    ++simpleResult;
                } else {
                    System.out.println("Not simple matched for " + line);
                }
            }

            System.out.println("Result: " + result);
            System.out.println("simple Result: " + simpleResult);
        }
    }

    private static boolean simpleIsPossible(String combination, Set<String> towels) {
        for (int i = 0; i < combination.length();) {
            int j = combination.length();
            for (; j > i; --j) {
                if (towels.contains(combination.substring(i, j))) {
                    break;
                }
            }

            if (j == i) {
                return false;
            }

            i = j;
        }

        return true;
    }


    private static int isPossible(String combination, Trie trie) {
        for (int i = 0; i < combination.length(); ) {
            int prefixSize = trie.sizeOfPrefix(combination.substring(i));
            if (prefixSize == 0) {
                return i;
            }
            i += prefixSize;
        }

        return -1;
    }
}

