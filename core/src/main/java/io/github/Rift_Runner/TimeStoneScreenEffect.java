package io.github.Rift_Runner;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/** A soft, pulsing emerald vignette driven by the Time Stone power-up state. */
public class TimeStoneScreenEffect {

    private static final float FADE_OUT_DURATION = 0.4f;
    private static final float FADE_IN_DURATION = 0.10f;

    private static final String VERTEX_SHADER =
        "attribute vec4 a_position;\n"
            + "attribute vec4 a_color;\n"
            + "attribute vec2 a_texCoord0;\n"
            + "uniform mat4 u_projTrans;\n"
            + "varying vec4 v_color;\n"
            + "varying vec2 v_texCoords;\n"
            + "void main() {\n"
            + "    v_color = a_color;\n"
            + "    v_texCoords = a_texCoord0;\n"
            + "    gl_Position = u_projTrans * a_position;\n"
            + "}\n";

    private static final String FRAGMENT_SHADER =
        "#ifdef GL_ES\n"
            + "precision mediump float;\n"
            + "#endif\n"
            + "uniform sampler2D u_texture;\n"
            + "uniform vec2 u_resolution;\n"
            + "uniform float u_time;\n"
            + "uniform float u_intensity;\n"
            + "varying vec4 v_color;\n"
            + "varying vec2 v_texCoords;\n"
            + "void main() {\n"
            + "    vec2 pixel = v_texCoords * u_resolution;\n"
            + "    float dx = min(pixel.x, u_resolution.x - pixel.x);\n"
            + "    float dy = min(pixel.y, u_resolution.y - pixel.y);\n"
            + "    float edgeDistance = min(dx, dy);\n"
            + "    float thickness = min(u_resolution.x, u_resolution.y) * 0.17;\n"
            + "    float edgeGlow = 1.0 - smoothstep(0.0, thickness, edgeDistance);\n"
            + "    float cornerGlow = 1.0 - smoothstep(0.0, thickness * 1.55, length(vec2(dx, dy)));\n"
            + "    float breath = 0.84 + 0.16 * (0.5 + 0.5 * sin(u_time * 8.796));\n"
            + "    vec3 emerald = vec3(0.02, 0.86, 0.43);\n"
            + "    vec3 lime = vec3(0.48, 1.0, 0.30);\n"
            + "    vec3 glowColor = mix(emerald, lime, cornerGlow * 0.48);\n"
            + "    float alpha = edgeGlow * (0.12 + cornerGlow * 0.08) * breath * u_intensity;\n"
            + "    vec4 textureColor = texture2D(u_texture, v_texCoords);\n"
            + "    gl_FragColor = vec4(glowColor, alpha) * v_color * textureColor;\n"
            + "}\n";

    private final ShaderProgram shader;
    private final Texture whitePixel;

    private float intensity;
    private float pulseTime;

    public TimeStoneScreenEffect() {
        shader = new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        if (!shader.isCompiled()) {
            throw new IllegalStateException("Time Stone screen effect shader failed: "
                + shader.getLog());
        }

        Pixmap pixel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixel.setColor(Color.WHITE);
        pixel.fill();
        whitePixel = new Texture(pixel);
        pixel.dispose();
    }

    /** Follows the existing slow-motion timer without keeping a second power-up timer. */
    public void update(float delta, boolean powerUpActive) {
        pulseTime += delta;
        if (powerUpActive) {
            intensity = Math.min(1f, intensity + delta / FADE_IN_DURATION);
        } else {
            intensity = Math.max(0f, intensity - delta / FADE_OUT_DURATION);
        }
    }

    public void reset() {
        intensity = 0f;
        pulseTime = 0f;
    }

    public void render(SpriteBatch batch, float viewportWidth, float viewportHeight) {
        if (intensity <= 0f) {
            return;
        }

        float previousColor = batch.getPackedColor();
        batch.setShader(shader);
        shader.setUniformf("u_resolution", viewportWidth, viewportHeight);
        shader.setUniformf("u_time", pulseTime);
        shader.setUniformf("u_intensity", intensity);
        batch.setColor(Color.WHITE);
        batch.draw(whitePixel, 0f, 0f, viewportWidth, viewportHeight);
        // The game uses SpriteBatch's default shader outside this effect.
        batch.setShader(null);
        batch.setPackedColor(previousColor);
    }

    public void dispose() {
        shader.dispose();
        whitePixel.dispose();
    }
}
