
package io.github.Rift_Runner;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Bird {
    private float x;
    private float y;
    private final float radius = 15f;
    private float flapStrenth = 400f;
    private float gravity = -1000f;
    private float speedY = 0;

    public void flap(){
        speedY = flapStrenth;
    }

    public Bird(float x , float y){
        this.x = x;
        this.y = y;
        this.speedY = 0;
    }

    public void update(float deltaTime){
        speedY *= gravity*deltaTime;
        y += speedY*deltaTime;
    }

    public void draw(ShapeRenderer shape){
        shape.circle(x,y,radius);
    }

    public float getLeft(){
        return x-radius/2f;
    }

    public float getRight(){
        return x+radius/2f;
    }

    public float getTop(){
        return y+radius/2f;
    }

    public float getBottom(){
        return y-radius/2f;
    }

    public float getX() {
        return x;
    }
    public float getY(){
        return y;
    }
}


