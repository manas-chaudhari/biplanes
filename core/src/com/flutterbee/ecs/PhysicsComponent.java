package com.flutterbee.ecs;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * Created by bbc4468 on 3/8/15.
 */
public class PhysicsComponent extends Component {
    public Body body;

    public PhysicsComponent(Body body) {
        this.body = body;
    }
}
