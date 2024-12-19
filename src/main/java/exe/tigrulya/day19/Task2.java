package exe.tigrulya.day19;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static exe.tigrulya.Utils.getResource;

public class Task2 {

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

        public Set<String> possiblePrefixes(String string) {
            Set<String> possiblePrefixes = new HashSet<>();
            StringBuilder currentPrefix = new StringBuilder();

            TrieNode currentNode = this;
            for (var ch: string.toCharArray()) {
                currentNode = currentNode.children.get(ch);
                if (currentNode == null) {
                    return possiblePrefixes;
                }

                currentPrefix.append(ch);
                if (currentNode.isPossibleEnd) {
                    possiblePrefixes.add(currentPrefix.toString());
                }
            }

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

        boolean delimiterHandled = false;
        long result = 0;

        try (var lines = Files.lines(getResource("input/19.txt"))) {
            Iterable<String> linesIterable = lines::iterator;
            for (var line : linesIterable) {
                if (line.isBlank()) {
                    delimiterHandled = true;
                    continue;
                }

                if (!delimiterHandled) {
                    trie.add(line.split(", "));
                    continue;
                }

                long combinations = possibleCombinations(new HashMap<>(), line, trie);
                System.out.println("Possible " + combinations + " for " + line);
                result += combinations;
            }

            System.out.println("Result: " + result);
        }
    }

    private static long possibleCombinations(Map<String, Long> memo, String combination, Trie trie) {
        if (combination.isEmpty()) {
            return 1;
        }
        if (memo.containsKey(combination)) {
            return memo.get(combination);
        }

        long possibleCombinations = 0;
        for (var prefix : trie.possiblePrefixes(combination)) {
            possibleCombinations += possibleCombinations(memo, combination.substring(prefix.length()), trie);
        }

        memo.put(combination, possibleCombinations);
        return possibleCombinations;
    }
}

