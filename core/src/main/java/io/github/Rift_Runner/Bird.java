
package io.github.Rift_Runner;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Bird {

    private float x;
    private float y;

    private float velocityY;

    public static final float WIDTH = 60f;
    public static final float HEIGHT = 40f;

    private static final float GRAVITY = -1000f;
    private static final float FLAP_STRENGTH = 400f;

    private final Texture aliveTexture;
    private final Texture deadTexture;
    private boolean dead;

    public Bird(float x, float y, Texture aliveTexture, Texture deadTexture) {
        this.x = x;
        this.y = y;
        this.velocityY = 0f;
        this.aliveTexture = aliveTexture;
        this.deadTexture = deadTexture;
    }

    public void flap() {
        velocityY = FLAP_STRENGTH;
    }

    public void update(float delta) {
        velocityY += GRAVITY * delta;

        y += velocityY * delta;
    }

    public void render(SpriteBatch batch) {
        batch.draw(dead ? deadTexture : aliveTexture, x, y, WIDTH, HEIGHT);
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
