package br.mackenzie;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
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
    final float WORLD_WIDTH = 12f;
    final float WORLD_HEIGHT = 7.5f;
    final float MONKEY_IDLE_SIZE = 1.2f;
    final float MONKEY_RUN_FLY_SIZE = 1.5f;
    ShapeRenderer shapeRenderer;
    Rectangle yellowSquare;
    boolean collidingWithYellow = false;

    @Override
    public void create() {
        // Prepare your application here.
        idleTexture = new Texture("monkey.png");
        runTexture = new Texture("monkeyR.png");
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT); //Variavelk global pra ficar mais facil o ajuste.

        monkeySprite = new Sprite(idleTexture);
        monkeySprite.setSize(MONKEY_IDLE_SIZE, MONKEY_IDLE_SIZE); //Variavelk global pra ficar mais facil o ajuste.
        monkeySprite.setPosition(1, 4f); // Teste para ele começar no meio da tela caindo.

        shapeRenderer = new ShapeRenderer();
        yellowSquare = new Rectangle(9.5f, 0f, 1f, 1f);
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

        if (monkeySprite.getX() < 0) {
            monkeySprite.setX(0);
        }

        float maxX = viewport.getWorldWidth() - monkeySprite.getWidth();
        if (monkeySprite.getX() > maxX) {
            monkeySprite.setX(maxX);
        }

        collidingWithYellow = monkeySprite.getBoundingRectangle().overlaps(yellowSquare);
    }

    public void applyState(State newState) {
        if (newState != currentState) {
            currentState = newState;

            if (currentState == State.IDLE) {
                monkeySprite.setSize(MONKEY_IDLE_SIZE, MONKEY_IDLE_SIZE);
                monkeySprite.setTexture(idleTexture);
            } else {
                monkeySprite.setSize(MONKEY_RUN_FLY_SIZE, MONKEY_RUN_FLY_SIZE);
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

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(collidingWithYellow ? Color.ORANGE : Color.YELLOW);
        shapeRenderer.rect(yellowSquare.x, yellowSquare.y, yellowSquare.width, yellowSquare.height);
        shapeRenderer.end();

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
