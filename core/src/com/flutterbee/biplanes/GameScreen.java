package com.flutterbee.biplanes;

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

public class GameScreen implements Screen, InputProcessor {
	final Biplanes game;

	Texture bg;
	OrthographicCamera camera;

	Texture fightTexture, win, lose;

	Texture leftButtonTexture, rightButtonTexture, fireButtonTexture;
	Texture[] player1Textures, player1FiredTextures;
	Texture[] player2Textures, player2FiredTextures;
	Texture[] scoreTextures;
	Texture bulletTexture;
	Player player1, player2;

	Player currentPlayer;
	Player opponentPlayer;
	boolean touchedLeft = false, touchedRight = false, touchedFire = false;
	int pointerLeft, pointerRight, pointerFire;

	Rectangle leftButton, rightButton, fireButton;
	public static final Rectangle ARENA = new Rectangle(0,
			Config.BOTTOMBAR_HEIGHT, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT
					- Config.BOTTOMBAR_HEIGHT);

	boolean gameOver = false;
	boolean playerWon = true;

	long startTime;

	private boolean isServer;
	private ConnectionManager connectionManager;

	public GameScreen(final Biplanes game, boolean isServer, ConnectionManager manager) {
		this.game = game;
		this.isServer = isServer;
		connectionManager = manager;
		Gdx.input.setInputProcessor(this);
		Gdx.input.setCatchBackKey(true);

		bg = new Texture("bg.png");
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);

		fightTexture = new Texture("fight.png");
		win = new Texture("Won.png");
		lose = new Texture("Lose.png");

		player1Textures = new Texture[Player.MAX_HEALTH + 1];
		player1FiredTextures = new Texture[Player.MAX_HEALTH + 1];
		for (int i = 0; i <= Player.MAX_HEALTH; i++) {
			player1Textures[i] = new Texture("plane1_" + i + ".png");
			player1FiredTextures[i] = new Texture("plane1_" + i + "_f.png");
		}
		player2Textures = new Texture[Player.MAX_HEALTH + 1];
		player2FiredTextures = new Texture[Player.MAX_HEALTH + 1];
		for (int i = 0; i <= Player.MAX_HEALTH; i++) {
			player2Textures[i] = new Texture("plane2_" + i + ".png");
			player2FiredTextures[i] = new Texture("plane2_" + i + "_f.png");
		}

		scoreTextures = new Texture[11];
		for (int i = 0; i < 11; i++) {
			scoreTextures[i] = new Texture(i + ".png");
		}

		bulletTexture = new Texture("bullet.png");

		leftButton = new Rectangle(20, 20, 64, 64);
		rightButton = new Rectangle(100, 20, 64, 64);
		fireButton = new Rectangle(700, 20, 64, 64);

		leftButtonTexture = new Texture("left_button.png");
		rightButtonTexture = new Texture("right_button.png");
		fireButtonTexture = new Texture("fire_button.png");

		player1 = new Player();
		player1.lastDeathTime = TimeUtils.nanoTime();
		resetPlayer1();
		player2 = new Player();
		player2.lastDeathTime = TimeUtils.nanoTime();
		resetPlayer2();

		currentPlayer = isServer ? player1 : player2;
		opponentPlayer = isServer ? player2 : player1;

		startTime = TimeUtils.nanoTime();
	}

	void resetPlayer1() {
		player1.width = 86;
		player1.height = 39;
		player1.x = 50;
		player1.y = Config.BOTTOMBAR_HEIGHT + player1.width / 2;
		player1.theta = 0f;
	}

	void resetPlayer2() {
		player2.width = 86;
		player2.height = 39;
		player2.x = Config.SCREEN_WIDTH - 50;
		player2.y = Config.BOTTOMBAR_HEIGHT + player2.width / 2; // So that
																	// plane
																	// does not
																	// touch the
																	// ground
		player2.theta = -180f;
	}

	private void drawPlayer(SpriteBatch batch, Player player, Texture texture,
			boolean flipY) {
		if (player.health > 0) {
			batch.draw(texture, player.x - player.width / 2, player.y
					- player.height / 2, // bot left corner
					player.width / 2, player.height / 2, // origin for rotation
					player.width, player.height, // width height
					1f, 1f, player.theta, // scale and rotation
					0, 0, (int) player.width, (int) player.height, // texture
																	// part
					true, flipY); // flip
		} else {
			batch.draw(texture, player.x - player.width / 2,
					player.y - player.height / 2, // bot left corner
					player.width / 2,
					player.height / 2, // origin for rotation
					player.width,
					player.height, // width height
					player.width / texture.getWidth(),
					player.height / texture.getHeight(), player.theta, // scale
																		// and
																		// rotation
					0, 0, texture.getWidth(), texture.getHeight(), // texture
																	// part
					true, flipY); // flip
		}

		for (Bullet bullet : player.bullets) {
			batch.draw(bulletTexture, bullet.xc - Bullet.width / 2, bullet.yc
					- Bullet.height / 2, Bullet.width, Bullet.height);
		}
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		SpriteBatch batch = game.batch;
		camera.update();

		synchronized (currentPlayer) {
			if (touchedLeft)
				currentPlayer.rotateAntiClockwiseByDelta();
			else if (touchedRight) {
				currentPlayer.rotateClockwiseByDelta();
			}
			if (touchedFire) {
				currentPlayer.fireBullet();
				touchedFire = false; // Fire should not happen continuously
			}

			if (Gdx.input.isKeyPressed(Keys.LEFT)) {
				currentPlayer.rotateAntiClockwiseByDelta();
			} else if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
				currentPlayer.rotateClockwiseByDelta();
			}

			player1.step();
			player2.step();
			checkCollisions();
		}

		Texture player1Texture = player1.isFired ? player1FiredTextures[player1.health]
				: player1Textures[player1.health];
		Texture player2Texture = player2.isFired ? player2FiredTextures[player2.health]
				: player2Textures[player2.health];

		/*****************************************************
		 * NO MODIFYING MODELS AFTER THIS TILL DRAWING COMPLETE
		 *****************************************************/

		batch.begin();
		batch.draw(bg, 0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);

		if (TimeUtils.nanoTime() - startTime < 1000000000) {
			batch.draw(fightTexture, Config.SCREEN_WIDTH / 2 - 415 / 2,
					Config.SCREEN_HEIGHT / 2 - 145 / 2, 415, 145);
		}
		if (gameOver) {
			batch.draw(playerWon ? win : lose,
					Config.SCREEN_WIDTH / 2 - 415 / 2,
					Config.SCREEN_HEIGHT / 2 - 145 / 2, 415, 145);
		} else {
			batch.draw(scoreTextures[Player.MAX_LIVES - player1.lives],
					300 - 64, 400, 64, 64);
			batch.draw(scoreTextures[Player.MAX_LIVES - player2.lives],
					600 - 64, 400, 64, 64);

			batch.draw(leftButtonTexture, leftButton.x, leftButton.y,
					leftButton.width, leftButton.height);
			batch.draw(rightButtonTexture, rightButton.x, rightButton.y,
					rightButton.width, rightButton.height);
			batch.draw(fireButtonTexture, fireButton.x, fireButton.y,
					fireButton.width, fireButton.height);

			drawPlayer(batch, player1, player1Texture, false);
			drawPlayer(batch, player2, player2Texture, true);
		}
		batch.end();

		if (player1.health == 0
				&& TimeUtils.nanoTime() - player1.lastDeathTime > Config.RESPAWN_TIME) {
			player1.restoreHealth();
			resetPlayer1();
		}
		if (player2.health == 0
				&& TimeUtils.nanoTime() - player2.lastDeathTime > Config.RESPAWN_TIME) {
			player2.restoreHealth();
			resetPlayer2();
		}
		if (player1.lives == 0 || player2.lives == 0) {
			endGame();
		}
		player1.isFired = false;
		player2.isFired = false;
	}

	private void endGame() {
		gameOver = true;
		playerWon = isServer ? player2.lives == 0 : player1.lives == 0;
		connectionManager.close();
	}

	private void checkCollisions() {
		player1.checkCollision(player2.bullets);
		player2.checkCollision(player1.bullets);
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
		bg.dispose();
		for (Texture texture : player1Textures) {
			texture.dispose();
		}
		for (Texture texture : player1FiredTextures) {
			texture.dispose();
		}
		for (Texture texture : player2Textures) {
			texture.dispose();
		}
		for (Texture texture : player2FiredTextures) {
			texture.dispose();
		}
		for (Texture texture : scoreTextures) {
			texture.dispose();
		}
		
		leftButtonTexture.dispose();
		rightButtonTexture.dispose();
		bulletTexture.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.SPACE) {
			touchedFire = true;
		} else if (keycode == Keys.BACK || keycode == Keys.ESCAPE) {
			game.setScreen(new MainMenuScreen(game));
			this.connectionManager.close();
			this.dispose();
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
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		Vector3 screenCoords = new Vector3(screenX, screenY, 0);
		camera.unproject(screenCoords);
		if (leftButton.contains(screenCoords.x, screenCoords.y)) {
			pointerLeft = pointer;
			touchedLeft = true;
		} else if (rightButton.contains(screenCoords.x, screenCoords.y)) {
			pointerRight = pointer;
			touchedRight = true;
		} else if (fireButton.contains(screenCoords.x, screenCoords.y)) {
			pointerFire = pointer;
			touchedFire = true;
		}
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (pointer == pointerLeft) {
			pointerLeft = -1;
			touchedLeft = false;
		} else if (pointer == pointerRight) {
			pointerRight = -1;
			touchedRight = false;
		} else if (pointer == pointerFire) {
			pointerFire = -1;
			touchedFire = false;
		}
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

	public String getData() {
		// send your data current position and fired bullets data
		String data = "";
		synchronized (currentPlayer) {
			data += String.valueOf(currentPlayer.x);
			data += ",";
			data += String.valueOf(currentPlayer.y);
			data += ",";
			data += String.valueOf(currentPlayer.theta);
			data += ",";
			data += String.valueOf(currentPlayer.health);

			data += "_";

			for (Bullet bullet : currentPlayer.bullets) {
				if (bullet.isNew) {
					data += String.valueOf(bullet.xc);
					data += ",";
					data += String.valueOf(bullet.yc);
					data += ",";
					data += String.valueOf(bullet.theta);
					data += "|";
					bullet.isNew = false;
				}
			}

		}

		return data;
	}

	public void setOpponentData(String text) {
		synchronized (currentPlayer) {
			// use text to set opponent data
			String[] tokens = text.split("_");
			String[] player_tokens = tokens[0].split(",");

			if (player_tokens.length != 4)
				return;

			opponentPlayer.x = Float.parseFloat(player_tokens[0]);
			opponentPlayer.y = Float.parseFloat(player_tokens[1]);
			opponentPlayer.theta = Float.parseFloat(player_tokens[2]);
			opponentPlayer.health = Integer.parseInt(player_tokens[3]);

			if (tokens.length == 2) {
				String[] bullets_token = tokens[1].split("\\|");
				for (String token : bullets_token) {
					if (!token.isEmpty()) {
						String bullet_tokens[] = token.split(",");

						if (bullet_tokens.length != 3)
							continue;

						Bullet bullet = new Bullet(
								Float.parseFloat(bullet_tokens[0]),
								Float.parseFloat(bullet_tokens[1]),
								Float.parseFloat(bullet_tokens[2]));
						opponentPlayer.bullets.add(bullet);
						opponentPlayer.isFired = true;
					}
				}
			}	
		}
	}
}
