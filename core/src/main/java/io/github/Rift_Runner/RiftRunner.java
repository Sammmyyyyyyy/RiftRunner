
package io.github.Rift_Runner;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

public class RiftRunner extends ApplicationAdapter {

    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;

    private Bird bird;
    private Array<Pole> poles;

    private enum GameState {
        READY,
        PLAYING,
        GAME_OVER
    }

    private GameState gameState;

    private float poleTimer;
    private int score;

    private static final float POLE_SPEED = 220f;
    private static final float POLE_SPAWN_INTERVAL = 1.6f;
    private static final float GROUND_HEIGHT = 35f;

    @Override
    public void create() {

        // Camera and game world
        camera = new OrthographicCamera();

        camera.setToOrtho(
            false,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );

        // Rendering tools
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();

        // Initialize the game
        restartGame();
    }

    private void restartGame() {

        float worldWidth = camera.viewportWidth;
        float worldHeight = camera.viewportHeight;

        // Create the bird
        bird = new Bird(
            worldWidth * 0.22f,
            worldHeight * 0.5f
        );

        // Create the pole list
        poles = new Array<>();

        // First pole starts outside the right edge
        poles.add(new Pole(worldWidth + 100f, worldHeight));

        poleTimer = 0f;
        score = 0;

        gameState = GameState.READY;
    }

    @Override
    public void render() {

        // Prevent a large movement jump after a lag spike
        float delta = Math.min(
            Gdx.graphics.getDeltaTime(),
            0.033f
        );

        handleInput();

        // Update gameplay only while playing
        if (gameState == GameState.PLAYING) {
            updateGame(delta);
        }

        renderGame();
    }

    private void handleInput() {

        boolean justPressed =
            Gdx.input.justTouched()
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE);

        if (!justPressed) {
            return;
        }

        if (gameState == GameState.READY) {

            gameState = GameState.PLAYING;
            bird.flap();

        } else if (gameState == GameState.PLAYING) {

            bird.flap();

        } else if (gameState == GameState.GAME_OVER) {

            restartGame();
        }
    }

    private void updateGame(float delta) {

        float worldWidth = camera.viewportWidth;
        float worldHeight = camera.viewportHeight;

        // Update bird physics
        bird.update(delta);

        // Check ground and ceiling collision
        if (bird.getY() <= GROUND_HEIGHT
            || bird.getY() + Bird.SIZE >= worldHeight) {

            gameState = GameState.GAME_OVER;
            return;
        }

        // Count time until the next pole spawns
        poleTimer += delta;

        if (poleTimer >= POLE_SPAWN_INTERVAL) {

            poles.add(new Pole(worldWidth, worldHeight));

            poleTimer = 0f;
        }

        // Move poles, detect collisions and update score
        for (Pole pole : poles) {

            pole.update(delta, POLE_SPEED);

            if (pole.collides(bird)) {

                gameState = GameState.GAME_OVER;
                return;
            }

            // Give one point when the bird passes a pole
            if (!pole.scored
                && pole.x + Pole.WIDTH < bird.getX()) {

                pole.scored = true;
                score++;
            }
        }

        // Remove poles that have left the screen
        for (int i = poles.size - 1; i >= 0; i--) {

            if (poles.get(i).isOffScreen()) {
                poles.removeIndex(i);
            }
        }
    }

    private void renderGame() {

        // Clear the screen
        Gdx.gl.glClearColor(
            0.05f,
            0.05f,
            0.12f,
            1f
        );

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // Draw game objects
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Ground
        shapeRenderer.setColor(
            0.25f,
            0.20f,
            0.35f,
            1f
        );

        shapeRenderer.rect(
            0,
            0,
            camera.viewportWidth,
            GROUND_HEIGHT
        );

        // Draw all poles
        for (Pole pole : poles) {
            pole.render(shapeRenderer);
        }

        // Draw bird
        bird.render(shapeRenderer);

        shapeRenderer.end();

        // Draw text
        batch.begin();

        font.getData().setScale(2f);

        font.draw(
            batch,
            "Score: " + score,
            20f,
            camera.viewportHeight - 25f
        );

        font.getData().setScale(1.5f);

        if (gameState == GameState.READY) {

            font.draw(
                batch,
                "Tap or press SPACE to start",
                camera.viewportWidth * 0.10f,
                camera.viewportHeight * 0.55f
            );

        } else if (gameState == GameState.GAME_OVER) {

            font.draw(
                batch,
                "GAME OVER",
                camera.viewportWidth * 0.32f,
                camera.viewportHeight * 0.55f
            );

            font.draw(
                batch,
                "Tap or press SPACE to restart",
                camera.viewportWidth * 0.08f,
                camera.viewportHeight * 0.48f
            );
        }

        batch.end();
    }

    @Override
    public void dispose() {

        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
