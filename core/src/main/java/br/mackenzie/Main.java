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
    float verticalVelocity = 0f;
    float gravity = -1.2f;
    float flapStrength = 2.2f;
    float groundY = 0f;
    boolean nextFlapMustBeQ = true;

    @Override
    public void create() {
        // Prepare your application here.
        idleTexture = new Texture("monkey.png");
        runTexture = new Texture("monkeyR.png");
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);

        monkeySprite = new Sprite(idleTexture);
        monkeySprite.setSize(2, 2);
        monkeySprite.setPosition(3, 2.5f); // Teste para ele começar no meio da tela caindo.
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if (width <= 0 || height <= 0) return;

        // Resize your application here. The parameters represent the new window size.
        viewport.update(width, height, true);
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

        State newState = isFlying() ? State.FLY : State.IDLE;

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            monkeySprite.translateX(speed * delta);
            currentDirection = Direction.RIGHT;

            if (!isFlying()) {
                newState = State.RUN;
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            monkeySprite.translateX(-speed * delta);
            currentDirection = Direction.LEFT;

            if (!isFlying()) {
                newState = State.RUN;
            }
        }

        if (nextFlapMustBeQ && Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            verticalVelocity = flapStrength;
            nextFlapMustBeQ = false;
            newState = State.FLY;
        } else if (!nextFlapMustBeQ && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            verticalVelocity = flapStrength;
            nextFlapMustBeQ = true;
            newState = State.FLY;
        }

        applyState(newState);
        updateDirection();
    }

    public void logic() {
        float delta = Gdx.graphics.getDeltaTime();

        verticalVelocity += gravity * delta;
        monkeySprite.translateY(verticalVelocity * delta);

        if (monkeySprite.getY() < groundY) {
            monkeySprite.setY(groundY);
            verticalVelocity = 0f;
        }

        float maxY = viewport.getWorldHeight() - monkeySprite.getHeight();
        if (monkeySprite.getY() > maxY) {
            monkeySprite.setY(maxY);
            verticalVelocity = 0f;
        }
    }

    public void applyState(State newState) {
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
    }

    public boolean isFlying() {
        return monkeySprite.getY() > groundY || verticalVelocity != 0f;
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
