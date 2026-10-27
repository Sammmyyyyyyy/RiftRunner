
package io.github.Rift_Runner;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.Random;

public class Pole {

    public float x;
    public float gapY;

    public static final float WIDTH = 65f;
    public static final float GAP = 190f;

    private final float worldHeight;

    public boolean scored = false;

    private static final Random RANDOM = new Random();

    public Pole(float x, float worldHeight) {
        this.x = x;
        this.worldHeight = worldHeight;
        this.gapY = worldHeight * 0.35f
            + RANDOM.nextFloat() * worldHeight * 0.30f;
    }

    public void update(float delta, float speed) {
        x -= speed * delta;
    }

    public void render(ShapeRenderer shapeRenderer) {

        float gapBottom = gapY - GAP / 2f;
        float gapTop = gapY + GAP / 2f;

        shapeRenderer.setColor(1f, 0.45f, 0.05f, 1f);

        shapeRenderer.rect(
            x,
            0,
            WIDTH,
            gapBottom
        );

        shapeRenderer.rect(
            x,
            gapTop,
            WIDTH,
            worldHeight - gapTop
        );
    }

    public boolean collides(Bird bird) {

        float birdLeft = bird.getX();
        float birdRight = bird.getX() + Bird.SIZE;
        float birdBottom = bird.getY();
        float birdTop = bird.getY() + Bird.SIZE;

        float poleLeft = x;
        float poleRight = x + WIDTH;

        if (birdRight <= poleLeft || birdLeft >= poleRight) {
            return false;
        }

        float gapBottom = gapY - GAP / 2f;
        float gapTop = gapY + GAP / 2f;

        return birdBottom < gapBottom || birdTop > gapTop;
    }

    public boolean isOffScreen() {
        return x + WIDTH < 0;
    }
}
