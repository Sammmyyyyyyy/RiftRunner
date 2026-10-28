
package io.github.Rift_Runner;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.Random;

public class Pole {

    public float x;
    public float gapY;

    public static final float WIDTH = 65f;
    public float GAP;
    public static float groundHeight;

    private final float worldHeight;

    public boolean scored = false;

    private static final Random RANDOM = new Random();

    public Pole(float x, float worldHeight, float gapSize,
        float positionVariation, float groundHeight) {

            this.x = x;
            this.worldHeight = worldHeight;
            this.GAP = gapSize;
            this.groundHeight = groundHeight;

            float minGapY = groundHeight + gapSize / 2f + 20f;
            float maxGapY = worldHeight - gapSize / 2f - 20f;

            minGapY = Math.max(minGapY, worldHeight * 0.35f - positionVariation);
            maxGapY = Math.min(maxGapY, worldHeight * 0.65f + positionVariation);

            this.gapY = minGapY
                + RANDOM.nextFloat() * (maxGapY - minGapY);
        }

    public void update(float delta, float speed) {
        x -= speed * delta;
    }

    public void render(ShapeRenderer shapeRenderer , float groundHeight) {

        float gapBottom = gapY - GAP / 2f;
        float gapTop = gapY + GAP / 2f;

        shapeRenderer.setColor(0f, 1f, 0f, 1f);

        shapeRenderer.rect(
            x,
            groundHeight,
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

    public boolean collides(Bird bird , float groundHeight) {

        float birdLeft = bird.getX();
        float birdRight = bird.getX() + Bird.SIZE;
        float birdBottom = bird.getY();
        float birdTop = bird.getY() + Bird.SIZE;

        float poleLeft = x;
        float poleRight = x + WIDTH;

        if (birdRight <= poleLeft || birdLeft >= poleRight) {
            return false;
        }

        float gapBottom = groundHeight + gapY - GAP / 2f;
        float gapTop = gapY + GAP / 2f;

        return birdBottom < gapBottom || birdTop > gapTop;
    }

    public boolean isOffScreen() {
        return x + WIDTH < 0;
    }
}
