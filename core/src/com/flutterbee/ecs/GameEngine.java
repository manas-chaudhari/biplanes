package com.flutterbee.ecs;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Created by bbc4468 on 3/8/15.
 */
public class GameEngine extends Engine {

    public GameEngine(SpriteBatch batch){
        super();
        // initialize me

        Entity entity = new Entity();
        addEntity(entity);

        World world = new World(new Vector2(5, 10), true);

        entity.add(new PositionComponent());
        entity.add(new PhysicsComponent(Player.createBody(world)));
        entity.add(new RenderableComponent());

        PhysicsSystem physicsSystem = new PhysicsSystem(world);
        RenderingSystem renderingSystem = new RenderingSystem(batch);

        addSystem(physicsSystem);
        addSystem(renderingSystem);
    }
}
