package io.github.Rift_Runner;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class RiftRunner extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;
    private ShapeRenderer shape;
    private Bird bird;
    @Override
    public void create() {
        shape = new ShapeRenderer();
        float screenHeight = Gdx.graphics.getHeight();
        bird = new Bird(30f , screenHeight/2f);
    }

    @Override
    public void render() {
        Pole pole = new Pole();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        bird.draw(shape);
        shape.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
    }
}
