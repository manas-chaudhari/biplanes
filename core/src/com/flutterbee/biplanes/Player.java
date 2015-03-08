package com.flutterbee.biplanes;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.TimeUtils;

public class Player {

	private static final float COLLISION_THRESHOLD = 1000;
	public static final int MAX_HEALTH = 3, MAX_LIVES = 10;

	// Positions
	float x;
	float y;
	float theta;
	float height;
	float width;
	final float speed = Config.PLANE_SPEED;

	long lastDeathTime;

	// Gameplay stats
	int health = MAX_HEALTH;
	int lives = MAX_LIVES;
	boolean isFired = false;

	public synchronized void restoreHealth() {
		health = MAX_HEALTH;
	}

	// Vector2 direction;
	// Vector2 position;

	public ArrayList<Bullet> bullets = new ArrayList<Bullet>();

	public static class Bullet extends Rectangle {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4811621881274960597L;

		float xc;
		float yc;
		float theta;
		
		boolean isNew = false;
		final static float height = 20;
		final static float width = 20;
		final float speed = Config.BULLET_SPEED;

		public Bullet(float xc, float yc, float theta) {
			super(xc, yc, width, height);
			this.theta = theta;
			this.xc = xc;
			this.yc = yc;
		}

		public void step() {
			float dt = Gdx.graphics.getDeltaTime();
			float dx = (float) (Math.cos(theta * Math.PI / 180) * speed * dt);
			float dy = (float) (Math.sin(theta * Math.PI / 180) * speed * dt);
			xc += dx;
			yc += dy;
			x += dx;
			y += dy;
		}
	}

	public void step() {
		// Move plane only if alive
		if (health > 0) {
			float dt = Gdx.graphics.getDeltaTime();
			float dx = (float) (Math.cos(theta * Math.PI / 180) * speed * dt);
			float dy = (float) (Math.sin(theta * Math.PI / 180) * speed * dt);
			x += dx;
			y += dy;

			if (x > GameScreen.ARENA.width) {
				x = x - GameScreen.ARENA.width;
			} else if (x < 0) {
				x = GameScreen.ARENA.width + x;
			}

			if (y > GameScreen.ARENA.height + GameScreen.ARENA.y) {
				y = GameScreen.ARENA.height + GameScreen.ARENA.y;
			}
		}

		for (Bullet bullet : bullets) {
			bullet.step();
		}
	}

	public void rotateClockwise(float dt) {
		if (health > 0) {
			theta -= Config.TURN_RATE * dt;			
		}
	}

	public void rotateAntiClockwise(float dt) {
		if (health > 0) {
			theta += Config.TURN_RATE * dt;
		}
	}

	public void rotateClockwiseByDelta() {
		rotateClockwise(Gdx.graphics.getDeltaTime());
	}

	public void rotateAntiClockwiseByDelta() {
		rotateAntiClockwise(Gdx.graphics.getDeltaTime());
	}

	public void fireBullet() {
		if (health > 0) {
			Bullet bullet = new Bullet(x, y, theta);
			bullet.isNew = true;
			bullets.add(bullet);
			isFired = true;
		}
	}

	public boolean checkHit(Bullet bullet) {
		return ((bullet.xc - x) * (bullet.xc - x) + (bullet.yc - y)
				* (bullet.yc - y)) < COLLISION_THRESHOLD;
	}

	public boolean centerFitsIn(Rectangle arena) {
		return arena.contains(x, y);
	}

	public boolean fitsIn(Rectangle arena) {
		float dx = (float) (height * Math.cos(theta));
		float dy = (float) (height * Math.sin(theta));
		return (arena.contains(x + dx, y + dy)
				&& arena.contains(x - dx, y + dy)
				&& arena.contains(x + dx, y - dy) && arena.contains(x - dx, y
				- dy));
	}

	public void checkCollision(ArrayList<Bullet> enemyBullets) {
		// Check collisions only if alive
		if (health > 0) {
			if (!centerFitsIn(GameScreen.ARENA) || y < Config.BOTTOMBAR_HEIGHT + height / 2 ) {
				kill();
			}
			for (int i = 0; i < enemyBullets.size();) {
				Bullet bullet = enemyBullets.get(i);
				if (checkHit(bullet)) {
					if (health > 0)
						health--;
					if (health == 0) {
						kill();
					}
					enemyBullets.remove(i);
					continue;
				}

				if (!GameScreen.ARENA.contains(bullet.xc, bullet.yc)) {
					enemyBullets.remove(i);
					continue;
				}
				i++;
			}
		}
	}

	private void kill() {
		health = 0;
		lives--;
		lastDeathTime = TimeUtils.nanoTime();
	}
}
