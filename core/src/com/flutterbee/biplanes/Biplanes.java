package com.flutterbee.biplanes;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Biplanes extends Game {
	public SpriteBatch batch;
	public BitmapFont font;
	
	@Override
	public void create() {
		batch = new SpriteBatch();

		//Use LibGDX's default Arial font.
        font = new BitmapFont();
        this.setScreen(new MainMenuScreen(this));
	}
	
	@Override
	public void render() {
		super.render();
	}
	
	public void dispose() {
        batch.dispose();
        font.dispose();
    }
	
	public static interface InvokeInMainThread {
		public void invokeInMainThread(Runnable runnable);
	}
}
