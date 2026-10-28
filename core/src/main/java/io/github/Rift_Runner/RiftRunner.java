
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
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.Random;

public class RiftRunner extends ApplicationAdapter {

    //For creating Sparks

    private Array<Spark> sparks = new Array<>();
    private Random random = new Random();

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
        DYING,
        GAME_OVER
    }

    private Texture background;
    private Texture aliveTexture;
    private Texture deadTexture;
    private Texture timeStoneTexture;
    private TextureRegion bottomPoleRegion;
    private TextureRegion topPoleRegion;
    private SpriteMask birdCollisionMask;
    private SpriteMask bottomPoleCollisionMask;
    private SpriteMask topPoleCollisionMask;
    private TimeStoneScreenEffect timeStoneScreenEffect;
    private GameState gameState;
    private float deathTimer;
    private static final float DEATH_DISPLAY_DURATION = 0.8f;

    private float poleTimer;
    private int score;
    private float currentPoleSpacing = 300f;

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


    //for Time Stone

    private static final float groundHeight = 0;

    private TimeStone timeStone;

    private float timeStoneSpawnTimer = 0f;
    private float slowMotionTimer = 0f;
    private float slowMotionFactor = 1f;

    private static final float TIME_STONE_SPAWN_INTERVAL = 10f;
    private static final float SLOW_MOTION_DURATION = 4f;
    private static final float SLOW_MOTION_MULTIPLIER = 0.5f;

    @Override
    public void create() {

        //extracting prefences stored in C drive of RiftRunner

        preferences = Gdx.app.getPreferences("RiftRunner");

        //taking previous highest score or defualt 0

        highScore = preferences.getInteger("highScore", 0);

        background = new Texture(Gdx.files.internal("background2.png"), true);
        aliveTexture = new Texture(Gdx.files.internal("drStrangeAlive.png"), true);
        deadTexture = new Texture(Gdx.files.internal("drStrangeDead.png"), true);
        timeStoneTexture = new Texture(Gdx.files.internal("timeStone.png"), true);
        Texture bottomPoleTexture = new Texture(Gdx.files.internal("bottomPole.png"), true);
        Texture topPoleTexture = new Texture(Gdx.files.internal("topPole.png"), true);
        bottomPoleRegion = new TextureRegion(bottomPoleTexture, 337, 50, 179, 1230);
        topPoleRegion = new TextureRegion(topPoleTexture, 337, 0, 179, 1230);
        birdCollisionMask = new SpriteMask("drStrangeAlive.png");
        bottomPoleCollisionMask = new SpriteMask("bottomPole.png");
        topPoleCollisionMask = new SpriteMask("topPole.png");
        timeStoneScreenEffect = new TimeStoneScreenEffect();
        background.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        aliveTexture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        deadTexture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        timeStoneTexture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        bottomPoleTexture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        topPoleTexture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
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

        sparks.clear();

        //for timeStone

        timeStone = null;
        timeStoneSpawnTimer = 0f;
        slowMotionTimer = 0f;
        slowMotionFactor = 1f;
        timeStoneScreenEffect.reset();

        float worldWidth = camera.viewportWidth;
        float worldHeight = camera.viewportHeight;

        // Create the bird
        bird = new Bird(
            worldWidth * 0.22f,
            worldHeight * 0.5f,
            aliveTexture,
            deadTexture
        );

        // Create the pole list
        poles = new Array<>();

        poleTimer = 0f;
        score = 0;
        updateDifficulty();

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
        } else if (gameState == GameState.DYING) {
            deathTimer -= delta;
            if (deathTimer <= 0f) {
                gameState = GameState.GAME_OVER;
            }
        }

        boolean powerUpActive = gameState == GameState.PLAYING
            && slowMotionTimer > 0f;
        timeStoneScreenEffect.update(delta, powerUpActive);

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
            spawnThrustSparks();

        } else if (gameState == GameState.PLAYING) {

            bird.flap();
            spawnThrustSparks();

        } else if (gameState == GameState.GAME_OVER) {
            saveHighScore();
            restartGame();
        }
    }

    private void updateGame(float delta) {

        //sparks update after every frame

        for (int i = sparks.size - 1; i >= 0; i--) {
            Spark spark = sparks.get(i);

            spark.update(delta);

            if (spark.isDead()) {
                sparks.removeIndex(i);
            }
        }

        float worldWidth = camera.viewportWidth;
        float worldHeight = camera.viewportHeight;
        updateDifficulty();

        bird.update(delta);

        if (bird.getY() <= groundHeight
            || bird.getY() + Bird.HEIGHT >= worldHeight) {

            beginDeath();
            return;
        }


        // Time Stone spawn timer
        timeStoneSpawnTimer += delta;

        if (timeStone == null
            && timeStoneSpawnTimer >= TIME_STONE_SPAWN_INTERVAL) {

            timeStone = new TimeStone(
                getTimeStoneSpawnX(worldWidth),
                worldHeight,
                groundHeight,
                timeStoneTexture
            );

            timeStoneSpawnTimer = 0f;
        }

        // Slow-motion countdown
        if (slowMotionTimer > 0f) {
            slowMotionTimer -= delta;

            if (slowMotionTimer < 0f) {
                slowMotionTimer = 0f;
            }
        }

        // Move and collect Time Stone
        if (timeStone != null) {
            timeStone.update(delta, poleSpeed);

            if (timeStone.collides(bird)) {
                slowMotionTimer = SLOW_MOTION_DURATION;
                timeStone = null;
            } else if (timeStone.isOffScreen()) {
                timeStone = null;
            }
        }

        // Ease into and out of slow motion instead of changing speed instantly.
        float targetSlowMotionFactor = slowMotionTimer > 0f
            ? SLOW_MOTION_MULTIPLIER : 1f;
        float factorStep = 1.5f * delta;
        if (slowMotionFactor < targetSlowMotionFactor) {
            slowMotionFactor = Math.min(targetSlowMotionFactor,
                slowMotionFactor + factorStep);
        } else {
            slowMotionFactor = Math.max(targetSlowMotionFactor,
                slowMotionFactor - factorStep);
        }
        float actualPoleSpeed = poleSpeed * slowMotionFactor;


        if (poles.size > 0) {
            Pole lastPole = poles.peek();

            if (lastPole.x <= worldWidth - currentPoleSpacing) {
                poles.add(new Pole(
                    lastPole.x + currentPoleSpacing,
                    worldHeight,
                    currentGap,
                    positionVariation,
                    groundHeight
                ));
            }
        }
        for (Pole pole : poles) {

            pole.update(delta, actualPoleSpeed);

            if (pole.collides(bird, groundHeight,
                birdCollisionMask, bottomPoleCollisionMask, topPoleCollisionMask,
                bottomPoleRegion, topPoleRegion)) {
                beginDeath();
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

        //draw sparks on screen

        for (Spark spark : sparks) {
            spark.render(shapeRenderer);
        }

        shapeRenderer.setColor(0f, 1f, 0f, 1f);
        shapeRenderer.rect(
            0,
            0,
            camera.viewportWidth,
            groundHeight
        );

        shapeRenderer.end();

        batch.begin();

        for (Pole pole : poles) {
            pole.render(batch, bottomPoleRegion, topPoleRegion, groundHeight);
        }

        if (timeStone != null) {
            timeStone.render(batch);
        }

        bird.render(batch);

        batch.end();

        batch.begin();
        timeStoneScreenEffect.render(batch, camera.viewportWidth, camera.viewportHeight);

        if (slowMotionTimer > 0f) {
            font.getData().setScale(1.5f);
            font.draw(
                batch,
                "TIME SLOWED: " + String.format("%.1f", slowMotionTimer) + "s",
                camera.viewportWidth * 0.55f,
                camera.viewportHeight - 30f
            );
        }

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

    private void beginDeath() {
        bird.setDead(true);
        gameState = GameState.DYING;
        deathTimer = DEATH_DISPLAY_DURATION;
        saveHighScore();
    }

    private void updateDifficulty() {
        if (score <= 5) {
            poleSpeed = 220f;
            currentGap = 190f;
            positionVariation = 80f;
            currentPoleSpacing = 300f;

        } else if (score <= 10) {
            poleSpeed = 260f;
            currentGap = 175f;
            positionVariation = 100f;
            currentPoleSpacing = 300f;

        } else if (score <= 15) {
            poleSpeed = 300f;
            currentGap = 160f;
            positionVariation = 120f;
            currentPoleSpacing = 290f;

        } else if (score <= 20) {
            poleSpeed = 330f;
            currentGap = 145f;
            positionVariation = 150f;
            currentPoleSpacing = 280f;

        } else {
            poleSpeed = 350f;
            currentGap = 140f;
            positionVariation = 180f;
            currentPoleSpacing = 270f;
        }
    }

    //helper function for getting X of timeStone

    private float getTimeStoneSpawnX(float worldWidth) {
        if (poles.size > 0) {
            Pole rightmostPole = poles.peek();

            for (Pole pole : poles) {
                if (pole.x > rightmostPole.x) {
                    rightmostPole = pole;
                }
            }

            return rightmostPole.x + currentPoleSpacing / 2f;
        }

        return worldWidth + currentPoleSpacing / 2f;
    }

    // generates sparks at the time of thrust

    private void spawnThrustSparks() {
        float x = bird.getX() + Bird.WIDTH * 0.20f;
        float y = bird.getY() + Bird.HEIGHT * 0.08f;

        for (int i = 0; i < 8; i++) {

            // Sparks move towards south-west
            float vx = -80f - random.nextFloat() * 100f;
            float vy = -80f - random.nextFloat() * 100f;

            float life = 0.2f + random.nextFloat() * 0.25f;
            float size = 2f + random.nextFloat() * 3f;

            sparks.add(new Spark(
                x, y, vx, vy, life, size
            ));
        }
    }

    @Override
    public void dispose() {

        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        background.dispose();
        aliveTexture.dispose();
        deadTexture.dispose();
        timeStoneTexture.dispose();
        bottomPoleRegion.getTexture().dispose();
        topPoleRegion.getTexture().dispose();
        timeStoneScreenEffect.dispose();
    }
}
