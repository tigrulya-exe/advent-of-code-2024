package exe.tigrulya.day8;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static exe.tigrulya.Utils.getResource;

public class Task1 {
    public static Set<Vector> antinodes = new HashSet<>();

    public record Vector(int x, int y) {
        public Vector difference(Vector other) {
            return new Vector(other.x - x, other.y - y);
        }

        public Vector antinode(Vector other) {
            Vector difference = difference(other);
            return new Vector(x - difference.x, y - difference.y);
        }
    }

    public static void main(String[] args) throws IOException {
        Map<Character, List<Vector>> antennasByFrequencies = new HashMap<>();
        int currentX = 0;
        int currentY = 0;
        int xSize = 0;

        try (var lines = Files.lines(getResource("input/8.txt"))) {
            Iterable<String> linesIterable = lines::iterator;
            for (var line : linesIterable) {
                for (var ch : line.toCharArray()) {
                    if (ch != '.') {
                        antennasByFrequencies.computeIfAbsent(ch, k -> new ArrayList<>())
                                .add(new Vector(currentX, currentY));
                    }
                    ++currentX;
                }

                ++currentY;
                currentX = 0;
                xSize = line.length();
            }
        }

        long antinodesCount = countAntinodes(antennasByFrequencies, xSize, currentY);

        System.out.println("Result: " + antinodesCount);
    }

    private static long countAntinodes(Map<Character, List<Vector>> antennasByFrequencies, int xSize, int ySize) {
        for (var antennasLocations : antennasByFrequencies.values()) {
            for (int i = 0; i < antennasLocations.size(); ++i) {
                for (int j = 0; j < antennasLocations.size(); ++j) {
                    if (i == j) {
                        continue;
                    }

                    Vector antinode = antennasLocations.get(i).antinode(antennasLocations.get(j));
                    if (antinode.x < xSize && antinode.x >= 0
                            && antinode.y < ySize && antinode.y >= 0) {
                        antinodes.add(antinode);
                    }
                }
            }
        }

        return antinodes.size();
    }
}

