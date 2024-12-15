package exe.tigrulya.day14;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ListToImage {

    public static void saveListAsImage(List<List<Integer>> data, String filePath) {
        // Get the height and width of the image based on the data
        int height = data.size();
        int width = data.get(0).size();

        // Create a BufferedImage with the desired dimensions and type
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Set pixel colors based on the list of lists
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int value = data.get(y).get(x);
                int color = (value == 0) ? 0x000000 : 0xFFFFFF; // Black for 0, White for 1
                image.setRGB(x, y, color);
            }
        }

        // Save the BufferedImage as a PNG file
        try {
            File outputFile = new File(filePath);
            ImageIO.write(image, "png", outputFile);
            System.out.println("Image saved successfully at: " + filePath);
        } catch (IOException e) {
            System.err.println("Error while saving the image: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Example data: a 5x5 list of lists
        List<List<Integer>> data = List.of(
            List.of(0, 0, 1, 0, 0),
            List.of(0, 1, 1, 1, 0),
            List.of(1, 1, 1, 1, 1),
            List.of(0, 1, 1, 1, 0),
            List.of(0, 0, 1, 0, 0)
        );

        // Path to save the image
        String filePath = "output.png";

        // Save the list of lists as an image
        saveListAsImage(data, filePath);
    }
}