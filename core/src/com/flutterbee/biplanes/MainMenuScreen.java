package com.flutterbee.biplanes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class MainMenuScreen implements Screen, InputProcessor {
	public final Biplanes game;

	OrthographicCamera camera;
	ConnectionManager connectionManager;
	Rectangle hostButton, joinButton;
	Texture hostButtonTexture, joinButtonTexture, bgMenu, logo, bgWaiting,
			connecting, connectingAnimation, waitingServerTexture,
			waitingClientTexture;
	
	GameScreen gameScreen;

	boolean serverInit = false, clientInit = false;

	boolean touchedStart = false;

	public MainMenuScreen(Biplanes game) {
		this.game = game;
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);

		connectionManager = new ConnectionManager(this);
		hostButtonTexture = new Texture("Host.png");
		joinButtonTexture = new Texture("Join.png");
		hostButton = new Rectangle(325, 200, 150, 40);
		joinButton = new Rectangle(325, 120, 150, 40);

		bgMenu = new Texture("bg_menu.png");
		logo = new Texture("Logo.png");
		bgWaiting = new Texture("Loading BG.png");
		connecting = new Texture("Loading base.png");
		connectingAnimation = new Texture("Loading.png");
		waitingServerTexture = new Texture("waiting_server.png");
		waitingClientTexture = new Texture("waiting_client.png");

		Gdx.input.setInputProcessor(this);
	}

	float rotation = 0.0f;

	float getBottomLeftForScreenCenterX(int width) {
		return (Config.SCREEN_WIDTH - width) / 2;
	}


	float getBottomLeftForScreenCenterY(int height) {
		return (Config.SCREEN_HEIGHT - height) / 2;
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();
		game.batch.setProjectionMatrix(camera.combined);

		SpriteBatch batch = game.batch;
		game.batch.begin();
		if (serverInit || clientInit) {
			batch.draw(bgWaiting, 0, 0, Config.SCREEN_WIDTH,
					Config.SCREEN_HEIGHT);
			batch.draw(connecting, getBottomLeftForScreenCenterX(150),
					getBottomLeftForScreenCenterY(150), 150, 150);
			// batch.draw(connectingAnimation,
			// getBottomLeftForScreenCenterX(150),
			// getBottomLeftForScreenCenterY(150), 150, 150);
			
			Texture text = serverInit ? waitingServerTexture
					: waitingClientTexture;
			batch.draw(text, getBottomLeftForScreenCenterX(500), 70, 500, 100);
			rotation -= 90 * Gdx.graphics.getDeltaTime();
			batch.draw(connectingAnimation, getBottomLeftForScreenCenterX(150),
					getBottomLeftForScreenCenterY(150), 75f, 75f, 150f, 150f,
					1f, 1f, rotation, 0, 0, connectingAnimation.getWidth(),
					connectingAnimation.getHeight(), false, false);

			game.font.draw(batch, "IPs: " + connectionManager.myIpAddresses, 400, 100);

		} else {
			batch.draw(bgMenu, 0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
			batch.draw(logo, getBottomLeftForScreenCenterX(250), 380, 250, 60);

			game.batch.draw(hostButtonTexture, hostButton.x, hostButton.y,
					hostButton.width, hostButton.height);
			game.batch.draw(joinButtonTexture, joinButton.x, joinButton.y,
					joinButton.width, joinButton.height);
		}
		game.batch.end();

	}
	
	public void startGame(){
		// game has started do some crazy shit
		this.game.setScreen(gameScreen);
		this.connectionManager.gameScreen = gameScreen; 
		this.dispose();
	}
	

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void show() {
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		hostButtonTexture.dispose();
		joinButtonTexture.dispose();
		bgMenu.dispose();
		logo.dispose();
		bgWaiting.dispose();
		connecting.dispose();
		connectingAnimation.dispose();
		waitingServerTexture.dispose();
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		Vector3 screenCoords = new Vector3(screenX, screenY, 0);
		camera.unproject(screenCoords);
		if (hostButton.contains(screenCoords.x, screenCoords.y)) {
			serverInit = true;
			gameScreen = new GameScreen(game, true, connectionManager);
			this.connectionManager.init_server();
		} else if (joinButton.contains(screenCoords.x, screenCoords.y)) {
			TextInputListener listener = new TextInputListener() {

				@Override
				public void input(String text) {
					clientInit = true;
					gameScreen = new GameScreen(game, false, connectionManager);
					connectionManager.init_client(text);
				}

				@Override
				public void canceled() {
				}

			};
			Gdx.input.getTextInput(listener, "Enter Server IP", "192.168.43.1", "192.168.*.*");
		} else if (screenCoords.x > 700 && screenCoords.y > 400) {
			touchedStart = true;
		}
		return false;
	}

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.BACK || keycode == Keys.ESCAPE) {
			Gdx.app.exit();
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

}
