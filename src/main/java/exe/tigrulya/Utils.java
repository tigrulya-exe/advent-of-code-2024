package exe.tigrulya;

import java.net.URI;
import java.nio.file.Path;

public class Utils {
    public static Path getResource(String name) {
        try {
            URI uri = Utils.class.getClassLoader()
                    .getResource(name)
                    .toURI();
            return Path.of(uri);
        } catch (Exception e) {
            throw new IllegalArgumentException("File not found: " + name);
        }
    }
}
