package br.mackenzie;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MenuScreen implements Screen {

    private final Game game;

    private FitViewport viewport;
    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private ScreenViewport uiViewport;

    // Dimensões do mundo (mesmas do GameScreen)
    private static final float WORLD_WIDTH  = 12f;
    private static final float WORLD_HEIGHT = 7.5f;

    // Botão "Jogar" centralizado
    private static final float BTN_WIDTH  = 3f;
    private static final float BTN_HEIGHT = 0.8f;
    private static final float BTN_X      = (WORLD_WIDTH  - BTN_WIDTH)  / 2f;
    private static final float BTN_Y      = (WORLD_HEIGHT / 2f) - 1.5f;

    private boolean hoveringButton = false;

    public MenuScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        viewport      = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
        spriteBatch   = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        uiViewport = new ScreenViewport();

        // Fontes padrão do LibGDX — substitua por fontes customizadas se quiser
        titleFont  = new BitmapFont();
        buttonFont = new BitmapFont();

        // Escala da fonte do título
        titleFont.getData().setScale(0.06f);
        buttonFont.getData().setScale(0.04f);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();

        checkHover();
        checkClick();

        drawMenu();
    }

    /** Verifica se o mouse está sobre o botão "Jogar" */
    private void checkHover() {
        // Converte posição do mouse para coordenadas do mundo
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.input.getY();

        // Unproject: converte pixels da tela para unidades do mundo
        float[] worldCoords = unproject(mouseX, mouseY);
        float wx = worldCoords[0];
        float wy = worldCoords[1];

        hoveringButton = wx >= BTN_X && wx <= BTN_X + BTN_WIDTH
            && wy >= BTN_Y && wy <= BTN_Y + BTN_HEIGHT;
    }

    /** Se clicar no botão, vai para o GameScreen */
    private void checkClick() {
        if (hoveringButton && Gdx.input.justTouched()) {
            game.setScreen(new GameScreen(game));
        }
    }

    /** Desenha o título e o botão */
    private void drawMenu() {
        // --- Fundo do botão (usa o viewport do mundo) ---
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(hoveringButton ? Color.YELLOW : Color.WHITE);
        shapeRenderer.rect(BTN_X, BTN_Y, BTN_WIDTH, BTN_HEIGHT);
        shapeRenderer.end();

        // --- Textos (usa o viewport em pixels) ---
        uiViewport.apply();
        spriteBatch.setProjectionMatrix(uiViewport.getCamera().combined);
        spriteBatch.begin();

        float screenW = uiViewport.getWorldWidth();
        float screenH = uiViewport.getWorldHeight();

        // Título centralizado no topo
        titleFont.getData().setScale(3f);
        titleFont.setColor(Color.WHITE);
        com.badlogic.gdx.graphics.g2d.GlyphLayout titleLayout =
            new com.badlogic.gdx.graphics.g2d.GlyphLayout(titleFont, "MonkeyMaze");
        titleFont.draw(spriteBatch, "MonkeyMaze",
            (screenW - titleLayout.width) / 2f,
            screenH * 0.7f);

        // Texto do botão centralizado
        buttonFont.getData().setScale(2f);
        buttonFont.setColor(Color.BLACK);
        com.badlogic.gdx.graphics.g2d.GlyphLayout btnLayout =
            new com.badlogic.gdx.graphics.g2d.GlyphLayout(buttonFont, "Jogar");
        buttonFont.draw(spriteBatch, "Jogar",
            (screenW - btnLayout.width) / 2f,
            screenH * 0.41f);

        spriteBatch.end();

        // Volta o viewport do mundo para o hover/click funcionar
        viewport.apply();
    }

    /** Converte coordenadas de tela (pixels) para coordenadas do mundo */
    private float[] unproject(float screenX, float screenY) {
        com.badlogic.gdx.math.Vector3 vec = new com.badlogic.gdx.math.Vector3(screenX, screenY, 0);
        viewport.getCamera().unproject(vec,
            viewport.getScreenX(), viewport.getScreenY(),
            viewport.getScreenWidth(), viewport.getScreenHeight());
        return new float[]{ vec.x, vec.y };
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height, true);
        uiViewport.update(width, height, true);
    }

    @Override
    public void hide() {

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
}
