package battleaimod.experimental;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Images {
    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws IOException {
        int originaHeight = originalImage.getHeight();
        int originalWidth = originalImage.getWidth();

        double originalRatio = (double) originaHeight / (double) originalWidth;
        double targetRatio = (double) targetHeight / (double) targetWidth;

        int actualWidth;
        int actualHeight;

        if (originalRatio > targetRatio) {
            // match height
            actualHeight = Math.min(targetHeight, originaHeight);
            actualWidth = (int) (actualHeight / originalRatio);
        } else {
            actualWidth = Math.min(targetWidth, originalWidth);
            actualHeight = (int) (actualWidth * originalRatio);
        }

        BufferedImage resizedImage = new BufferedImage(actualWidth, actualHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(originalImage, 0, 0, actualWidth, actualHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }
}
