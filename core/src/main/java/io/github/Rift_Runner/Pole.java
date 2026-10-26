
package io.github.Rift_Runner;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.Random;

public class Pole {

    public float x;
    public float gapY;

    public static final float WIDTH = 65f;
    public static final float GAP = 200f;

    private static final float SCREEN_HEIGHT = 800f;
    private static final Random random = new Random();

    public boolean scored = false;

    public Pole(float x) {
        this.x = x;
        gapY = 250f + random.nextFloat() * 300f;
    }

    public void update(float delta, float speed) {
        x -= speed * delta;
    }

    public void render(ShapeRenderer shape) {
        shape.setColor(1f, 0.4f, 0.08f, 1f);

        // Bottom pole
        shape.rect(
            x, 0,
            WIDTH, gapY - GAP / 2f
        );

        // Top pole
        shape.rect(
            x, gapY + GAP / 2f,
            WIDTH, SCREEN_HEIGHT - (gapY + GAP / 2f)
        );

        // Portal-like pole edges
        shape.setColor(1f, 0.75f, 0.2f, 1f);

        shape.rect(
            x - 5, gapY - GAP / 2f - 10,
            WIDTH + 10, 10
        );

        shape.rect(
            x - 5, gapY + GAP / 2f,
            WIDTH + 10, 10
        );
    }

    public boolean collides(Bird bird) {
        boolean overlapsX =
            bird.getRight() > x &&
                bird.getLeft() < x + WIDTH;

        boolean hitsBottom =
            bird.getBottom() < gapY - GAP / 2f;

        boolean hitsTop =
            bird.getTop() > gapY + GAP / 2f;

        return overlapsX && (hitsBottom || hitsTop);
    }

    public boolean isOffScreen() {
        return x + WIDTH < 0;
    }
}
