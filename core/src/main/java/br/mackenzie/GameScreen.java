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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;


public class GameScreen implements Screen {
    private final Game game;

    // --- dificuldade ---
    private Difficulty difficulty;

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
    float knockbackVelocityX = 0f;
    float knockbackVelocityY = 0f;
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

    // --- blocos vermelhos (inimigos lineares) ---
    Array<Rectangle> redBlocks;
    Array<Vector2> redBlockDirections; // direção de movimento de cada bloco
    final float RED_BLOCK_SIZE = 0.7f;
    final float RED_BLOCK_SPEED = 2.5f;

    // --- perseguidor (só no HARD) ---
    Rectangle chaserBlock;
    final float CHASER_SIZE = 0.9f;
    final float CHASER_SPEED = 1.8f;

    // --- projéteis dos inimigos (só no HARD) ---
    Array<Rectangle> bullets;
    Array<Vector2> bulletDirections;
    final float BULLET_SIZE = 0.25f;
    final float BULLET_SPEED = 4f;
    float bulletTimer = 0f;
    final float BULLET_INTERVAL = 2f;

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
        // lê a dificuldade escolhida pelo jogador
        difficulty = GameManager.getInstance().getDifficulty();

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

        redBlocks = new Array<>();
        redBlockDirections = new Array<>();
        bullets = new Array<>();
        bulletDirections = new Array<>();

        createRedBlocks();

        // perseguidor só existe no HARD
        if (difficulty.enemiesShoot) {
            chaserBlock = new Rectangle(
                WORLD_WIDTH - 1.5f, WORLD_HEIGHT / 2f,
                CHASER_SIZE, CHASER_SIZE
            );
        }

        bananas = new Array<>();
        createBananas();
    }

    public void createRedBlocks() {
        redBlocks.clear();
        redBlockDirections.clear();

        // EASY: sem inimigos
        if (!difficulty.hasEnemies) return;

        // NORMAL: 5 blocos  |  HARD: 8 blocos
        int count = difficulty.enemiesShoot ? 8 : 5;

        // direções possíveis: direita, esquerda, cima, baixo
        float[][] dirs = { {1, 0}, {-1, 0}, {0, 1}, {0, -1} };

        for (int i = 0; i < count; i++) {
            float x = MathUtils.random(2f, WORLD_WIDTH - RED_BLOCK_SIZE);
            float y = MathUtils.random(0.5f, WORLD_HEIGHT - RED_BLOCK_SIZE);
            redBlocks.add(new Rectangle(x, y, RED_BLOCK_SIZE, RED_BLOCK_SIZE));

            // cada bloco recebe uma direção aleatória
            float[] dir = dirs[MathUtils.random(0, dirs.length - 1)];
            redBlockDirections.add(new Vector2(dir[0], dir[1]));
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
        if (gameOver || win) {
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

        // decrementa aqui, uma vez por frame
        if (damageCooldown > 0) {
            damageCooldown -= delta;
        }

        // --- física do macaco ---
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

        float knockbackFriction = 8f;
        knockbackVelocityX = knockbackVelocityX - knockbackVelocityX * knockbackFriction * delta;
        knockbackVelocityY = knockbackVelocityY - knockbackVelocityY * knockbackFriction * delta;
        monkeySprite.translateX(knockbackVelocityX * delta);
        monkeySprite.translateY(knockbackVelocityY * delta);

        // --- lógica dos inimigos (NORMAL e HARD) ---
        if (difficulty.hasEnemies) {
            updateRedBlocks(delta);
        }

        // --- perseguidor e projéteis (só HARD) ---
        if (difficulty.enemiesShoot) {
            updateChaser(delta);
            updateBullets(delta);
            spawnBullets(delta);
        }

        checkBananaCollection();

        if (win) return;

        checkRedBlockDamage();
    }

    // move cada bloco na sua direção e rebate nas bordas
    public void updateRedBlocks(float delta) {
        for (int i = 0; i < redBlocks.size; i++) {
            Rectangle block = redBlocks.get(i);
            Vector2 dir = redBlockDirections.get(i);

            block.x += dir.x * RED_BLOCK_SPEED * delta;
            block.y += dir.y * RED_BLOCK_SPEED * delta;

            // rebate horizontalmente
            if (block.x < 0) {
                block.x = 0;
                dir.x = Math.abs(dir.x);
            } else if (block.x + block.width > WORLD_WIDTH) {
                block.x = WORLD_WIDTH - block.width;
                dir.x = -Math.abs(dir.x);
            }

            // rebate verticalmente
            if (block.y < 0) {
                block.y = 0;
                dir.y = Math.abs(dir.y);
            } else if (block.y + block.height > WORLD_HEIGHT) {
                block.y = WORLD_HEIGHT - block.height;
                dir.y = -Math.abs(dir.y);
            }
        }
    }

    // perseguidor se move em direção ao macaco
    public void updateChaser(float delta) {
        if (chaserBlock == null) return;

        float monkeyX = monkeySprite.getX() + monkeySprite.getWidth() / 2f;
        float monkeyY = monkeySprite.getY() + monkeySprite.getHeight() / 2f;

        float dx = monkeyX - (chaserBlock.x + chaserBlock.width / 2f);
        float dy = monkeyY - (chaserBlock.y + chaserBlock.height / 2f);
        float len = (float) Math.sqrt(dx * dx + dy * dy);

        if (len > 0) {
            chaserBlock.x += (dx / len) * CHASER_SPEED * delta;
            chaserBlock.y += (dy / len) * CHASER_SPEED * delta;
        }

        // dano do perseguidor usa o mesmo cooldown
        if (damageCooldown <= 0 && monkeySprite.getBoundingRectangle().overlaps(chaserBlock)) {
            lives--;
            damageCooldown = DAMAGE_COOLDOWN_TIME;

            if (len > 0) {
                float knockbackStrength = 10f;
                knockbackVelocityX = (dx / len) * knockbackStrength;
                knockbackVelocityY = (dy / len) * knockbackStrength;
            }

            if (lives <= 0) {
                lives = 0;
                gameOver = true;
            }
        }
    }

    // spawna projéteis a partir de cada bloco vermelho na direção do macaco
    public void spawnBullets(float delta) {
        bulletTimer += delta;
        if (bulletTimer < BULLET_INTERVAL) return;
        bulletTimer = 0f;

        float monkeyX = monkeySprite.getX() + monkeySprite.getWidth() / 2f;
        float monkeyY = monkeySprite.getY() + monkeySprite.getHeight() / 2f;

        for (Rectangle block : redBlocks) {
            float originX = block.x + block.width / 2f;
            float originY = block.y + block.height / 2f;

            float dx = monkeyX - originX;
            float dy = monkeyY - originY;
            float len = (float) Math.sqrt(dx * dx + dy * dy);

            if (len > 0) {
                bullets.add(new Rectangle(
                    originX - BULLET_SIZE / 2f,
                    originY - BULLET_SIZE / 2f,
                    BULLET_SIZE, BULLET_SIZE
                ));
                bulletDirections.add(new Vector2(dx / len, dy / len));
            }
        }
    }

    // move projéteis e verifica colisão com o macaco
    public void updateBullets(float delta) {
        Rectangle monkeyRect = monkeySprite.getBoundingRectangle();

        for (int i = bullets.size - 1; i >= 0; i--) {
            Rectangle bullet = bullets.get(i);
            Vector2 dir = bulletDirections.get(i);

            bullet.x += dir.x * BULLET_SPEED * delta;
            bullet.y += dir.y * BULLET_SPEED * delta;

            // remove se saiu da tela
            if (bullet.x < 0 || bullet.x > WORLD_WIDTH ||
                bullet.y < 0 || bullet.y > WORLD_HEIGHT) {
                bullets.removeIndex(i);
                bulletDirections.removeIndex(i);
                continue;
            }

            // dano ao macaco
            if (damageCooldown <= 0 && monkeyRect.overlaps(bullet)) {
                lives--;
                damageCooldown = DAMAGE_COOLDOWN_TIME;
                bullets.removeIndex(i);
                bulletDirections.removeIndex(i);

                if (lives <= 0) {
                    lives = 0;
                    gameOver = true;
                }
            }
        }
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

    public void checkRedBlockDamage() {
        if (damageCooldown > 0) return;

        Rectangle monkeyRectangle = monkeySprite.getBoundingRectangle();

        for (Rectangle redBlock : redBlocks) {
            if (monkeyRectangle.overlaps(redBlock)) {
                lives--;
                damageCooldown = DAMAGE_COOLDOWN_TIME;

                // calcula direção oposta ao bloco e empurra o macaco
                float dx = monkeySprite.getX() - redBlock.x;
                float dy = monkeySprite.getY() - redBlock.y;
                float len = (float) Math.sqrt(dx * dx + dy * dy);

                if (len > 0) {
                    float knockbackStrength = 10f;
                    knockbackVelocityX = (dx / len) * knockbackStrength;
                    knockbackVelocityY = (dy / len) * knockbackStrength;
                }

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

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // bananas
        shapeRenderer.setColor(Color.YELLOW);
        for (Rectangle banana : bananas) {
            shapeRenderer.rect(banana.x, banana.y, banana.width, banana.height);
        }

        // blocos vermelhos (inimigos lineares)
        shapeRenderer.setColor(Color.RED);
        for (Rectangle redBlock : redBlocks) {
            shapeRenderer.rect(redBlock.x, redBlock.y, redBlock.width, redBlock.height);
        }

        // perseguidor (roxo) — só no HARD
        if (difficulty.enemiesShoot && chaserBlock != null) {
            shapeRenderer.setColor(Color.PURPLE);
            shapeRenderer.rect(chaserBlock.x, chaserBlock.y, chaserBlock.width, chaserBlock.height);
        }

        // projéteis (laranja) — só no HARD
        if (difficulty.enemiesShoot) {
            shapeRenderer.setColor(Color.ORANGE);
            for (Rectangle bullet : bullets) {
                shapeRenderer.rect(bullet.x, bullet.y, bullet.width, bullet.height);
            }
        }

        shapeRenderer.end();

        // macaco
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        monkeySprite.draw(spriteBatch);
        spriteBatch.end();

        // HUD
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

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
