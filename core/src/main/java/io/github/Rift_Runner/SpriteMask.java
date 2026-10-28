package io.github.Rift_Runner;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;

/** Alpha mask used to avoid collisions in transparent sprite pixels. */
public class SpriteMask {

    private final int width;
    private final int height;
    private final byte[] alpha;

    public SpriteMask(String assetPath) {
        Pixmap pixmap = new Pixmap(Gdx.files.internal(assetPath));
        width = pixmap.getWidth();
        height = pixmap.getHeight();
        alpha = new byte[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                alpha[y * width + x] = (byte) (pixmap.getPixel(x, y) & 0xff);
            }
        }

        pixmap.dispose();
    }

    /** u and v are normalized image coordinates, with (0, 0) at top left. */
    public boolean isOpaque(float u, float v) {
        if (u < 0f || u >= 1f || v < 0f || v >= 1f) {
            return false;
        }

        int x = Math.min(width - 1, (int) (u * width));
        int y = Math.min(height - 1, (int) (v * height));
        return (alpha[y * width + x] & 0xff) >= 64;
    }
}
