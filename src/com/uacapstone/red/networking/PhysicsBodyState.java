package com.uacapstone.red.networking;

import org.msgpack.annotation.Message;

import com.badlogic.gdx.physics.box2d.Body;

@Message
public class PhysicsBodyState implements IState<Body> {
	public float x, y;
	public float velocityX, velocityY;
	
	@Override
	public void apply(Body o) {
		o.setTransform(x, y, o.getAngle());
		o.setLinearVelocity(velocityX, velocityY);
	}

}
