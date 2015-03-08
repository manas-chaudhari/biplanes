package com.flutterbee.ecs;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.flutterbee.biplanes.Biplanes;
import com.flutterbee.biplanes.Config;

/**
 * Created by bbc4468 on 3/8/15.
 */
public class RenderingSystem extends IteratingSystem {
    private ComponentMapper<PositionComponent> pm = ComponentMapper.getFor(PositionComponent.class);
    private ComponentMapper<RenderableComponent> vm = ComponentMapper.getFor(RenderableComponent.class);

    SpriteBatch batch;
    Texture texture;
    Texture backgroundTexture;

    public RenderingSystem(SpriteBatch batch) {
        super(Family.all(RenderableComponent.class, PositionComponent.class).get());
        this.texture = new Texture("plane1_1.png");
        this.backgroundTexture = new Texture("bg.png");
        this.batch = batch;

    }

    public void processEntity(Entity entity, float deltaTime) {
        PositionComponent position = pm.get(entity);

        batch.begin();
        batch.draw(backgroundTexture, 0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
        batch.draw(texture, position.center.x, position.center.y, // bot left corner
					0, 0 , // origin for rotation
					20, 20, // width height
					1f, 1f, 0, // scale and rotation
					0, 0, 20, 20, // texture
																	// part
					true, false); // flip
        batch.end();
    }
}
