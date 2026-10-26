
package io.github.Rift_Runner;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Bird {
    private float x;
    private float y;

    private float flapStrenth = 400f;

    private float gravity = -1000f;

    private float speedY = 0;

    public void flap(){
        speedY = flapStrenth;
    }

    public void update(float deltaTime){
        speedY *= gravity*deltaTime;
        y += speedY*deltaTime;
    }
}
