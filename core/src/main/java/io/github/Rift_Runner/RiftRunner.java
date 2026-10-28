
package io.github.Rift_Runner;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Preferences;
public class RiftRunner extends ApplicationAdapter {

    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;

    // for saving high score

    private Preferences preferences;
    private int highScore;

    private Bird bird;
    private Array<Pole> poles;

    private enum GameState {
        READY,
        PLAYING,
        GAME_OVER
    }

    private Texture background;
    private GameState gameState;

    private float poleTimer;
    private int score;

    private float BASE_POLE_SPEED = 220f;
    private float MAX_POLE_SPEED = 350f;

    private static final float NORMAL_POLE_SPAWN_INTERVAL = 1.6f;
    private static final float CLOSE_POLE_SPAWN_INTERVAL = 1.35f;

    private float poleSpeed;
    private float currentGap;
    private float positionVariation;
    private float currentSpawnInterval;

    private static final float BASE_GAP = 190f;
    private static final float MIN_GAP = 145f;

    private static final float groundHeight = 40f;
    @Override
    public void create() {

        //extracting prefences stored in C drive of RiftRunner

        preferences = Gdx.app.getPreferences("RiftRunner");

        //taking previous highest score or defualt 0

        highScore = preferences.getInteger("highScore", 0);

        background = new Texture(
            Gdx.files.internal("flappy.jpg")
        );
        camera = new OrthographicCamera();

        camera.setToOrtho(
            false,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();

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
        poles.add(new Pole(
            worldWidth + 100f,
            worldHeight,
            currentGap,
            positionVariation,
            groundHeight
        ));

        poleTimer = 0f;
        score = 0;
        updateDifficulty();

        gameState = GameState.READY;
    }

    @Override
    public void render() {

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
            saveHighScore();
            restartGame();
        }
    }

    private void updateGame(float delta) {

        float worldWidth = camera.viewportWidth;
        float worldHeight = camera.viewportHeight;
        updateDifficulty();

        bird.update(delta);

        if (bird.getY() <= groundHeight
            || bird.getY() + Bird.SIZE >= worldHeight) {

            gameState = GameState.GAME_OVER;
            saveHighScore();
            return;
        }

        poleTimer += delta;

        if (poleTimer >= currentSpawnInterval) {
            poles.add(new Pole(
                worldWidth,
                worldHeight,
                currentGap,
                positionVariation,
                groundHeight
            ));

            poleTimer -= currentSpawnInterval;
        }
        for (Pole pole : poles) {

            pole.update(delta, poleSpeed);

            if (pole.collides(bird ,groundHeight)) {
                gameState = GameState.GAME_OVER;
                saveHighScore();
                return;
            }

            if (!pole.scored
                && pole.x + Pole.WIDTH < bird.getX()) {
                pole.scored = true;
                score++;
            }
        }

        for (int i = poles.size - 1; i >= 0; i--) {

            if (poles.get(i).isOffScreen()) {
                poles.removeIndex(i);
            }
        }
    }

    private void renderGame() {

        camera.update();

        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        batch.draw(
            background,
            0,
            0,
            camera.viewportWidth,
            camera.viewportHeight
        );

        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0f, 1f, 0f, 1f);

        shapeRenderer.rect(
            0,
            0,
            camera.viewportWidth,
            groundHeight
        );

        for (Pole pole : poles) {

            pole.render(shapeRenderer , groundHeight);
        }

        bird.render(shapeRenderer);

        shapeRenderer.end();

        batch.begin();

        font.getData().setScale(1.5f);

        font.draw(
            batch,
            "High Score: " + highScore,
            20f,
            camera.viewportHeight - 60f
        );

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

    //helper method for saving high score

    private void saveHighScore() {

        if (score > highScore) {

            highScore = score;

            preferences.putInteger("highScore", highScore);

            //for writing into persistent memory

            preferences.flush();
        }
    }

    private void updateDifficulty() {
        if (score <= 5) {
            poleSpeed = 220f;
            currentGap = 190f;
            positionVariation = 80f;
            currentSpawnInterval = 1.6f;

        } else if (score <= 10) {
            poleSpeed = 260f;
            currentGap = 175f;
            positionVariation = 100f;
            currentSpawnInterval = 1.45f;

        } else if (score <= 15) {
            poleSpeed = 300f;
            currentGap = 160f;
            positionVariation = 120f;
            currentSpawnInterval = 1.30f;

        } else if (score <= 20) {
            poleSpeed = 330f;
            currentGap = 145f;
            positionVariation = 150f;
            currentSpawnInterval = 1.15f;

        } else {
            poleSpeed = 380f;
            currentGap = 130f;
            positionVariation = 180f;
            currentSpawnInterval = 1f;
        }
    }
    @Override
    public void dispose() {

        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        background.dispose();
    }
}
