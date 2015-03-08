package com.flutterbee.ecs;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.physics.box2d.World;

import java.util.List;

/**
 * Created by bbc4468 on 3/8/15.
 */
public class PhysicsSystem extends EntitySystem {
    private static final float TIME_STEP = 1 / 45f;
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;

    private ComponentMapper<PositionComponent> positionMapper = ComponentMapper.getFor(PositionComponent.class);
    private ComponentMapper<PhysicsComponent> physicsMapper = ComponentMapper.getFor(PhysicsComponent.class);

    private ImmutableArray<Entity> entities;

    private World world;
    private double accumulator;

    public PhysicsSystem(World world) {
        this.world = world;
    }

    @Override
    public void addedToEngine(Engine engine) {
        this.entities = engine.getEntitiesFor(Family.all(PhysicsComponent.class, PositionComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        // fixed time step
        // max frame time to avoid spiral of death (on slow devices)
        float frameTime = Math.min(deltaTime, 0.25f);

        accumulator += frameTime;
        while (accumulator >= TIME_STEP) {
            world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
            accumulator -= TIME_STEP;
        }

        for (Entity entity : entities) {
            PositionComponent position = positionMapper.get(entity);
            PhysicsComponent physics = physicsMapper.get(entity);

            position.center = physics.body.getPosition();
        }
    }
}
