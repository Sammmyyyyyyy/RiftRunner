
package io.github.Rift_Runner;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Bird {

    private float x;
    private float y;

    private float velocityY;

    public static final float SIZE = 30f;

    private static final float GRAVITY = -1000f;
    private static final float FLAP_STRENGTH = 400f;

    public Bird(float x, float y) {
        this.x = x;
        this.y = y;
        this.velocityY = 0f;
    }

    public void flap() {
        velocityY = FLAP_STRENGTH;
    }

    public void update(float delta) {
        velocityY += GRAVITY * delta;

        y += velocityY * delta;
    }

    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(0f, 1f, 0f, 1f);

        shapeRenderer.circle(
            x + SIZE / 2f,
            y + SIZE / 2f,
            SIZE / 2f
        );
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
