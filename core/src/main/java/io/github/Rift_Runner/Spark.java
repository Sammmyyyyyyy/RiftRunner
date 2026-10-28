package io.github.Rift_Runner;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Spark {
    float x, y;
    float vx, vy;
    float life;
    float maxLife;
    float size;

    public Spark(float x, float y, float vx, float vy,
                 float life, float size) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.life = life;
        this.maxLife = life;
        this.size = size;
    }

    public void update(float delta) {
        x += vx * delta;
        y += vy * delta;
        life -= delta;
    }

    public void render(ShapeRenderer renderer) {
        float alpha = Math.max(0f, life / maxLife);

        renderer.setColor(1f, 0.55f, 0.08f, alpha);
        renderer.circle(x, y, size * alpha);
    }

    public boolean isDead() {
        return life <= 0f;
    }
}
