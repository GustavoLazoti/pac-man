package br.mackenzie;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main implements ApplicationListener {

    Texture idleTexture;
    Texture runTexture;
    SpriteBatch spriteBatch;
    FitViewport viewport;
    Sprite monkeySprite;
    State currentState = State.IDLE;
    Direction currentDirection = Direction.RIGHT;


    @Override
    public void create() {
        // Prepare your application here.
        idleTexture = new Texture("monkey.png");
        runTexture = new Texture("monkeyR.png");
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);

        monkeySprite = new Sprite(idleTexture);
        monkeySprite.setSize(2, 2);
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if(width <= 0 || height <= 0) return;

        // Resize your application here. The parameters represent the new window size.
        viewport.update(width, height,  true);
    }

    @Override
    public void render() {
        // Draw your application here.
        input();
        logic();
        draw();
    }

    public void input() {
        float speed = 3.5f;
        float delta = Gdx.graphics.getDeltaTime();

        State newState = State.IDLE;

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            monkeySprite.translateX(speed * delta);
            newState = State.RUN;
            currentDirection = Direction.RIGHT;
        } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            monkeySprite.translateX(-speed * delta);
            newState = State.RUN;
            currentDirection = Direction.LEFT;
        }

        if (newState != currentState) {
            currentState = newState;

            if (currentState == State.IDLE) {
                monkeySprite.setSize(2, 2);
                monkeySprite.setTexture(idleTexture);
            } else {
                monkeySprite.setSize(2.5f, 2.5f);
                monkeySprite.setTexture(runTexture);
            }
        }

        updateDirection();
    }

    public void logic() {

    }

    public void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        monkeySprite.draw(spriteBatch);
        spriteBatch.end();
    }

    public void updateDirection() {

        if (currentDirection == Direction.LEFT && !monkeySprite.isFlipX()) {
            monkeySprite.flip(true, false);
        }

        if (currentDirection == Direction.RIGHT && monkeySprite.isFlipX()) {
            monkeySprite.flip(true, false);
        }
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void dispose() {
        // Destroy application's resources here.
    }
}
