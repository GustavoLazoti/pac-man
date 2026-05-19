package br.mackenzie;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class DifficultyScreen implements Screen {

    private final Game game;

    private FitViewport viewport;
    private ScreenViewport uiViewport;
    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont titleFont;
    private BitmapFont buttonFont;

    private static final float WORLD_WIDTH  = 12f;
    private static final float WORLD_HEIGHT = 7.5f;

    private static final float BTN_WIDTH  = 3.5f;
    private static final float BTN_HEIGHT = 0.8f;
    private static final float BTN_X      = (WORLD_WIDTH - BTN_WIDTH) / 2f;

    // Uma posição Y para cada botão
    private static final float BTN_Y_EASY   = 4.5f;
    private static final float BTN_Y_NORMAL = 3.3f;
    private static final float BTN_Y_HARD   = 2.1f;

    private final Rectangle btnEasy   = new Rectangle(BTN_X, BTN_Y_EASY,   BTN_WIDTH, BTN_HEIGHT);
    private final Rectangle btnNormal = new Rectangle(BTN_X, BTN_Y_NORMAL, BTN_WIDTH, BTN_HEIGHT);
    private final Rectangle btnHard   = new Rectangle(BTN_X, BTN_Y_HARD,   BTN_WIDTH, BTN_HEIGHT);

    public DifficultyScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        viewport      = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
        uiViewport    = new ScreenViewport();
        spriteBatch   = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        titleFont     = new BitmapFont();
        buttonFont    = new BitmapFont();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        checkClick();
        draw();
    }

    private void checkClick() {
        if (!Gdx.input.justTouched()) return;

        Vector3 touch = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.getCamera().unproject(touch,
            viewport.getScreenX(), viewport.getScreenY(),
            viewport.getScreenWidth(), viewport.getScreenHeight());

        if (btnEasy.contains(touch.x, touch.y)) {
            GameManager.getInstance().setDifficulty(Difficulty.EASY);
            game.setScreen(new GameScreen(game));
        } else if (btnNormal.contains(touch.x, touch.y)) {
            GameManager.getInstance().setDifficulty(Difficulty.NORMAL);
            game.setScreen(new GameScreen(game));
        } else if (btnHard.contains(touch.x, touch.y)) {
            GameManager.getInstance().setDifficulty(Difficulty.HARD);
            game.setScreen(new GameScreen(game));
        }
    }

    private void draw() {
        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.getCamera().unproject(mouse,
            viewport.getScreenX(), viewport.getScreenY(),
            viewport.getScreenWidth(), viewport.getScreenHeight());

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        drawButton(btnEasy,   mouse, Color.GREEN);
        drawButton(btnNormal, mouse, Color.ORANGE);
        drawButton(btnHard,   mouse, Color.RED);

        shapeRenderer.end();

        uiViewport.apply();
        spriteBatch.setProjectionMatrix(uiViewport.getCamera().combined);
        spriteBatch.begin();

        float screenW = uiViewport.getWorldWidth();
        float screenH = uiViewport.getWorldHeight();

        titleFont.getData().setScale(3f);
        titleFont.setColor(Color.WHITE);
        GlyphLayout title = new GlyphLayout(titleFont, "Dificuldade");
        titleFont.draw(spriteBatch, "Dificuldade",
            (screenW - title.width) / 2f, screenH * 0.82f);

        buttonFont.getData().setScale(2f);
        buttonFont.setColor(Color.BLACK);
        drawButtonLabel("Fácil",   screenW, screenH * 0.655f);
        drawButtonLabel("Normal",  screenW, screenH * 0.525f);
        drawButtonLabel("Difícil", screenW, screenH * 0.385f);

        spriteBatch.end();

        viewport.apply();
    }

    private void drawButton(Rectangle btn, Vector3 mouse, Color color) {
        boolean hovering = btn.contains(mouse.x, mouse.y);
        shapeRenderer.setColor(hovering ? Color.YELLOW : color);
        shapeRenderer.rect(btn.x, btn.y, btn.width, btn.height);
    }

    private void drawButtonLabel(String text, float screenW, float y) {
        GlyphLayout layout = new GlyphLayout(buttonFont, text);
        buttonFont.draw(spriteBatch, text, (screenW - layout.width) / 2f, y);
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height, true);
        uiViewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        shapeRenderer.dispose();
        titleFont.dispose();
        buttonFont.dispose();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
