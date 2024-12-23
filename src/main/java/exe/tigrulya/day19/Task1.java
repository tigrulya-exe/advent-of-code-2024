package exe.tigrulya.day19;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static exe.tigrulya.Utils.getResource;

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

        public List<Integer> possiblePrefixes(String string) {
            List<Integer> possiblePrefixes = new ArrayList<>();
            TrieNode currentNode = this;
            for (int position = 0; position < string.length(); position++) {
                currentNode = currentNode.children.get(string.charAt(position));
                if (currentNode == null) {
                    return possiblePrefixes;
                }

                if (currentNode.isPossibleEnd) {
                    possiblePrefixes.add(position + 1);
                }
            }

            possiblePrefixes.add(string.length());
            return possiblePrefixes;
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

                if (isPossible(line, 0, trie)) {
                    ++result;
                }

                if (simpleIsPossible(line, towels)) {
                    ++simpleResult;
                }
            }

            System.out.println("Result: " + result);
            System.out.println("simple Result: " + simpleResult);
        }
    }

    private static boolean simpleIsPossible(String combination, Set<String> towels) {
        return simpleIsPossible(combination, 0, towels);
    }

    private static boolean simpleIsPossible(String combination, int idx, Set<String> towels) {
        if (idx == combination.length()) {
            return true;
        }

        int j = combination.length();
        for (; j > idx; --j) {
            if (towels.contains(combination.substring(idx, j))) {
                boolean possible = simpleIsPossible(combination, j, towels);
                if (possible) {
                    return true;
                }
            }
        }

        return false;
    }


    private static boolean isPossible(String combination, int idx, Trie trie) {
        if (idx == combination.length()) {
            return true;
        }

        for (var prefixSize: trie.possiblePrefixes(combination.substring(idx))) {
            if (isPossible(combination, idx + prefixSize, trie)) {
                return true;
            }
        }

        return false;
    }
}

