package corp.redacted.game.views;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import corp.redacted.game.Game;
import corp.redacted.game.WorldBuilder;
import corp.redacted.game.entity.systems.RenderingSystem;

/**
 * Écran de debug
 */
public class DebugScreen implements Screen {
    private final Game PARENT;

    private PooledEngine engine;
    private WorldBuilder worldBuilder;

    private CameraInputController camController;

    public DebugScreen(Game parent){
        super();

        PARENT = parent;
        engine = new PooledEngine();

        worldBuilder = new WorldBuilder(engine);
        worldBuilder.generateWorld();

        RenderingSystem renderSys = new RenderingSystem();
        engine.addSystem(renderSys);

        camController = new CameraInputController(renderSys.getCam());
        Gdx.input.setInputProcessor(camController);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        engine.update(delta);
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() { engine.clearPools(); }
}
