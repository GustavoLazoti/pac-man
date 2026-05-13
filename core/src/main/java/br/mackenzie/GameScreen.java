package br.mackenzie;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;


public class GameScreen implements Screen {
    private final Game game;

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
    Array<Rectangle> bananas;
    int bananasCollected = 0;
    final int BANANA_COUNT = 10;
    int score = 0;
    final int COLLECTIBLE_POINTS = 10;
    final float COLLECTIBLE_SIZE = MONKEY_IDLE_SIZE / 2f;
    ScreenViewport uiViewport;
    Array<Rectangle> redBlocks;
    final int RED_BLOCK_COUNT = 5;
    final float RED_BLOCK_SIZE = 0.7f;
    int lives = 3;
    boolean gameOver = false;
    boolean win = false;
    Rectangle backToMenuButton;
    final float GAME_OVER_BUTTON_WIDTH = 260f;
    final float GAME_OVER_BUTTON_HEIGHT = 60f;
    float damageCooldown = 0f;
    final float DAMAGE_COOLDOWN_TIME = 1.2f;
    BitmapFont hudFont;

    public GameScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        idleTexture = new Texture("monkey.png");
        runTexture = new Texture("monkeyR.png");

        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
        uiViewport = new ScreenViewport();

        hudFont = new BitmapFont();
        hudFont.getData().setScale(2f);
        backToMenuButton = new Rectangle();

        monkeySprite = new Sprite(idleTexture);
        monkeySprite.setSize(MONKEY_IDLE_SIZE, MONKEY_IDLE_SIZE);
        monkeySprite.setPosition(1, 4f);
        shapeRenderer = new ShapeRenderer();
        redBlocks = new Array<>();
        createRedBlocks();
        bananas = new Array<>();
        createBananas();
    }

    public void createRedBlocks() {
        redBlocks.clear();

        for (int i = 0; i < RED_BLOCK_COUNT; i++) {
            float x = MathUtils.random(2f, WORLD_WIDTH - RED_BLOCK_SIZE);
            float y = MathUtils.random(0.5f, WORLD_HEIGHT - RED_BLOCK_SIZE);

            Rectangle redBlock = new Rectangle(x, y, RED_BLOCK_SIZE, RED_BLOCK_SIZE);

            redBlocks.add(redBlock);
        }
    }

    public void createBananas() {
        bananas.clear();

        int attempts = 0;

        while (bananas.size < BANANA_COUNT && attempts < 500) {
            attempts++;

            float x = MathUtils.random(0.5f, WORLD_WIDTH - COLLECTIBLE_SIZE);
            float y = MathUtils.random(0.5f, WORLD_HEIGHT - COLLECTIBLE_SIZE);

            Rectangle banana = new Rectangle(x, y, COLLECTIBLE_SIZE, COLLECTIBLE_SIZE);

            if (isBananaPositionValid(banana)) {
                bananas.add(banana);
            }
        }
    }

    public boolean isBananaPositionValid(Rectangle banana) {
        for (Rectangle redBlock : redBlocks) {
            if (banana.overlaps(redBlock)) {
                return false;
            }
        }

        for (Rectangle otherBanana : bananas) {
            if (banana.overlaps(otherBanana)) {
                return false;
            }
        }

        if (banana.overlaps(monkeySprite.getBoundingRectangle())) {
            return false;
        }

        return true;
    }

    @Override
    public void render(float delta) {
        if (gameOver || win) {
            if (checkGameOverButtonClick()) {
                return;
            }

            draw();
            return;
        }

        input();
        logic();
        draw();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;

        if (viewport != null) {
            viewport.update(width, height, true);
        }

        if (uiViewport != null) {
            uiViewport.update(width, height, true);
        }
    }

    public void input() {
        if (gameOver|| win) {
            return;
        }

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

    public void updateBackToMenuButtonPosition() {
        float buttonX = uiViewport.getWorldWidth() / 2f - GAME_OVER_BUTTON_WIDTH / 2f;
        float buttonY = uiViewport.getWorldHeight() / 2f - 100f;

        backToMenuButton.set(
            buttonX,
            buttonY,
            GAME_OVER_BUTTON_WIDTH,
            GAME_OVER_BUTTON_HEIGHT
        );
    }

    public boolean checkGameOverButtonClick() {
        updateBackToMenuButtonPosition();

        if (Gdx.input.justTouched()) {
            Vector3 touchPosition = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            uiViewport.unproject(touchPosition);

            if (backToMenuButton.contains(touchPosition.x, touchPosition.y)) {
                game.setScreen(new MenuScreen(game));
                return true;
            }
        }

        return false;
    }

    public void logic() {
        if (gameOver || win) {
            return;
        }

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

        checkBananaCollection();

        if (win) {
            return;
        }

        checkRedBlockDamage(delta);
    }

    public void checkBananaCollection() {
        Rectangle monkeyRectangle = monkeySprite.getBoundingRectangle();

        for (int i = bananas.size - 1; i >= 0; i--) {
            Rectangle banana = bananas.get(i);

            if (monkeyRectangle.overlaps(banana)) {
                bananas.removeIndex(i);

                bananasCollected++;
                score += COLLECTIBLE_POINTS;

                if (bananasCollected % 5 == 0) {
                    lives++;
                }

                if (bananasCollected >= BANANA_COUNT) {
                    win = true;
                }

                break;
            }
        }
    }

    public void checkRedBlockDamage(float delta) {
        if (damageCooldown > 0) {
            damageCooldown -= delta;
            return;
        }

        Rectangle monkeyRectangle = monkeySprite.getBoundingRectangle();

        for (int i = redBlocks.size - 1; i >= 0; i--) {
            Rectangle redBlock = redBlocks.get(i);

            if (monkeyRectangle.overlaps(redBlock)) {
                lives--;
                damageCooldown = DAMAGE_COOLDOWN_TIME;

                redBlocks.removeIndex(i);

                if (lives <= 0) {
                    lives = 0;
                    gameOver = true;
                }

                break;
            }
        }
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

        // Desenha objetos do mundo: coletável e blocos vermelhos
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(Color.YELLOW);

        for (Rectangle banana : bananas) {
            shapeRenderer.rect(banana.x, banana.y, banana.width, banana.height);
        }

        shapeRenderer.setColor(Color.RED);
        for (Rectangle redBlock : redBlocks) {
            shapeRenderer.rect(redBlock.x, redBlock.y, redBlock.width, redBlock.height);
        }

        shapeRenderer.end();

        // Desenha o macaco
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        monkeySprite.draw(spriteBatch);
        spriteBatch.end();

        // Desenha a interface: pontos, vidas e Game Over
        uiViewport.apply();

        if (gameOver || win) {
            updateBackToMenuButtonPosition();

            shapeRenderer.setProjectionMatrix(uiViewport.getCamera().combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(
                backToMenuButton.x,
                backToMenuButton.y,
                backToMenuButton.width,
                backToMenuButton.height
            );
            shapeRenderer.end();
        }

        spriteBatch.setProjectionMatrix(uiViewport.getCamera().combined);
        spriteBatch.begin();

        hudFont.setColor(Color.WHITE);
        hudFont.draw(spriteBatch, "Pontos: " + score, 20, uiViewport.getWorldHeight() - 20);
        hudFont.draw(spriteBatch, "Vidas: " + lives, 20, uiViewport.getWorldHeight() - 60);
        hudFont.draw(spriteBatch, "Bananas: " + bananasCollected + "/" + BANANA_COUNT, 20, uiViewport.getWorldHeight() - 100);

        if (gameOver || win) {
            String endMessage = win ? "Win" : "Game Over";

            GlyphLayout gameOverLayout = new GlyphLayout(hudFont, endMessage);

            hudFont.setColor(Color.WHITE);
            hudFont.draw(
                spriteBatch,
                endMessage,
                uiViewport.getWorldWidth() / 2f - gameOverLayout.width / 2f,
                uiViewport.getWorldHeight() / 2f + 40f
            );

            GlyphLayout buttonTextLayout = new GlyphLayout(hudFont, "Voltar ao menu");

            hudFont.setColor(Color.BLACK);
            hudFont.draw(
                spriteBatch,
                "Voltar ao menu",
                backToMenuButton.x + backToMenuButton.width / 2f - buttonTextLayout.width / 2f,
                backToMenuButton.y + backToMenuButton.height / 2f + buttonTextLayout.height / 2f
            );
        }

        spriteBatch.end();

        // Volta para o viewport do mundo
        viewport.apply();
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
    public void dispose() {
        if (idleTexture != null) idleTexture.dispose();
        if (runTexture != null) runTexture.dispose();
        if (spriteBatch != null) spriteBatch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (hudFont != null) hudFont.dispose();
    }

    @Override public void pause() {

    }
    @Override public void resume() {

    }
    @Override public void hide() {

    }
}
