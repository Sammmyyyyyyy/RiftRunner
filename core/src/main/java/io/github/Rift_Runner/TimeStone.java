
package io.github.Rift_Runner;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.Random;

public class TimeStone {

    private float x;
    private final float y;

    public static final float WIDTH = 42f;
    public static final float HEIGHT = 42f;
    private final Texture texture;

    private static final Random RANDOM = new Random();

    public TimeStone(float x, float worldHeight, float groundHeight, Texture texture) {
        this.x = x;
        this.texture = texture;

        float minY = groundHeight + 80f;
        float maxY = worldHeight - 80f;

        this.y = minY + RANDOM.nextFloat() * (maxY - minY);
    }

    public void update(float delta, float speed) {
        x -= speed * delta;
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, x - WIDTH / 2f, y - HEIGHT / 2f, WIDTH, HEIGHT);
    }

    public boolean collides(Bird bird) {
        float stoneLeft = x - WIDTH / 2f;
        float stoneRight = x + WIDTH / 2f;
        float stoneBottom = y - HEIGHT / 2f;
        float stoneTop = y + HEIGHT / 2f;

        return bird.getX() < stoneRight
            && bird.getX() + Bird.WIDTH > stoneLeft
            && bird.getY() < stoneTop
            && bird.getY() + Bird.HEIGHT > stoneBottom;
    }

    public boolean isOffScreen() {
        return x + WIDTH / 2f < 0;
    }
}
