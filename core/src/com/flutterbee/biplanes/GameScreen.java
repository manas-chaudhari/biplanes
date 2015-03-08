package com.flutterbee.biplanes;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
import com.flutterbee.biplanes.Player.Bullet;
import com.flutterbee.ecs.GameEngine;

public class GameScreen implements Screen {
	final Biplanes game;
    Engine engine;

	OrthographicCamera camera;

	public static final Rectangle ARENA = new Rectangle(0,
			Config.BOTTOMBAR_HEIGHT, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT
					- Config.BOTTOMBAR_HEIGHT);



    public GameScreen(final Biplanes game, boolean isServer, ConnectionManager manager) {

        this.game = game;
        engine = new GameEngine(game.batch);

        Gdx.input.setCatchBackKey(true);

		camera = new OrthographicCamera();
		camera.setToOrtho(false, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);

	}


    @Override
    public void show() {
    }

    @Override
	public void render(float delta) {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
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
    public void dispose() {

    }


    public String getData() {
		String data = "";
		return data;
	}

    public void setOpponentData(String text) {
	}
}
