
package io.github.Rift_Runner;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.Random;

public class TimeStone {

    private float x;
    private final float y;

    public static final float RADIUS = 16f;

    private static final Random RANDOM = new Random();

    public TimeStone(float x, float worldHeight, float groundHeight) {
        this.x = x;

        float minY = groundHeight + 80f;
        float maxY = worldHeight - 80f;

        this.y = minY + RANDOM.nextFloat() * (maxY - minY);
    }

    public void update(float delta, float speed) {
        x -= speed * delta;
    }

    public void render(ShapeRenderer shapeRenderer) {
        // Outer glow
        shapeRenderer.setColor(1f, 0.7f, 0f, 1f);
        shapeRenderer.circle(x, y, RADIUS + 5f);

        // Time Stone
        shapeRenderer.setColor(0.2f, 1f, 0.3f, 1f);
        shapeRenderer.circle(x, y, RADIUS);
    }

    public boolean collides(Bird bird) {
        float closestX = Math.max(
            bird.getX(),
            Math.min(x, bird.getX() + Bird.SIZE)
        );

        float closestY = Math.max(
            bird.getY(),
            Math.min(y, bird.getY() + Bird.SIZE)
        );

        float dx = x - closestX;
        float dy = y - closestY;

        return dx * dx + dy * dy <= RADIUS * RADIUS;
    }

    public boolean isOffScreen() {
        return x + RADIUS < 0;
    }
}
