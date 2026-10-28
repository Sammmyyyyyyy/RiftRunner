
package io.github.Rift_Runner;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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

    public void render(SpriteBatch batch, TextureRegion bottomPole,
                       TextureRegion topPole, float groundHeight) {

        float gapBottom = gapY - GAP / 2f;
        float gapTop = gapY + GAP / 2f;
        float bottomHeight = Math.min(gapBottom - groundHeight,
            bottomPole.getRegionHeight() * WIDTH / bottomPole.getRegionWidth());
        float bottomSourceHeight = bottomHeight * bottomPole.getRegionWidth() / WIDTH;
        batch.draw(bottomPole.getTexture(), x, groundHeight, WIDTH, bottomHeight,
            bottomPole.getRegionX(), bottomPole.getRegionY(),
            bottomPole.getRegionWidth(), (int) bottomSourceHeight, false, false);

        float topHeight = Math.min(worldHeight - gapTop,
            topPole.getRegionHeight() * WIDTH / topPole.getRegionWidth());
        float topSourceHeight = topHeight * topPole.getRegionWidth() / WIDTH;
        int topSourceY = topPole.getRegionY() + topPole.getRegionHeight()
            - (int) topSourceHeight;
        batch.draw(topPole.getTexture(), x, gapTop, WIDTH, topHeight,
            topPole.getRegionX(), topSourceY,
            topPole.getRegionWidth(), (int) topSourceHeight, false, false);
    }

    public boolean collides(Bird bird, float groundHeight,
                            SpriteMask birdMask, SpriteMask bottomPoleMask,
                            SpriteMask topPoleMask,
                            TextureRegion bottomPole, TextureRegion topPole) {
        float gapBottom = gapY - GAP / 2f;
        float gapTop = gapY + GAP / 2f;
        float bottomHeight = Math.min(gapBottom - groundHeight,
            bottomPole.getRegionHeight() * WIDTH / bottomPole.getRegionWidth());
        float topHeight = Math.min(worldHeight - gapTop,
            topPole.getRegionHeight() * WIDTH / topPole.getRegionWidth());
        int bottomSourceHeight = (int) (bottomHeight * bottomPole.getRegionWidth() / WIDTH);
        int topSourceHeight = (int) (topHeight * topPole.getRegionWidth() / WIDTH);
        int topSourceY = topPole.getRegionY() + topPole.getRegionHeight() - topSourceHeight;

        // Check opaque points from the bird image against the rendered pole pixels.
        final int samplesPerUnit = 2;
        for (int py = 0; py < Bird.HEIGHT * samplesPerUnit; py++) {
            float worldY = bird.getY() + (py + 0.5f) / samplesPerUnit;
            float birdV = 1f - (worldY - bird.getY()) / Bird.HEIGHT;

            for (int px = 0; px < Bird.WIDTH * samplesPerUnit; px++) {
                float worldX = bird.getX() + (px + 0.5f) / samplesPerUnit;
                float birdU = (worldX - bird.getX()) / Bird.WIDTH;
                if (!birdMask.isOpaque(birdU, birdV)
                    || worldX < x || worldX > x + WIDTH) {
                    continue;
                }

                float poleU = (worldX - x) / WIDTH;
                if (worldY >= groundHeight && worldY <= groundHeight + bottomHeight) {
                    float sourceV = 1f - (worldY - groundHeight) / bottomHeight;
                    float sourceX = bottomPole.getRegionX() + poleU * bottomPole.getRegionWidth();
                    float sourceY = bottomPole.getRegionY() + sourceV * bottomSourceHeight;
                    if (bottomPoleMask.isOpaque(
                        sourceX / bottomPole.getTexture().getWidth(),
                        sourceY / bottomPole.getTexture().getHeight())) {
                        return true;
                    }
                }

                if (worldY >= gapTop && worldY <= gapTop + topHeight) {
                    float sourceV = 1f - (worldY - gapTop) / topHeight;
                    float sourceX = topPole.getRegionX() + poleU * topPole.getRegionWidth();
                    float sourceY = topSourceY + sourceV * topSourceHeight;
                    if (topPoleMask.isOpaque(
                        sourceX / topPole.getTexture().getWidth(),
                        sourceY / topPole.getTexture().getHeight())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean isOffScreen() {
        return x + WIDTH < 0;
    }
}
