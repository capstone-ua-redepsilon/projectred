package com.uacapstone.red.object;

import com.badlogic.gdx.physics.box2d.Body;


public class CollisionData extends UserData {
	
	public CollisionData(ICollisionTarget target, Body collisionBody, String description)
	{
		super(description);
		mCollisionTarget = target;
		mCollisionBody = collisionBody;
	}
	
	public Body mCollisionBody;
	public ICollisionTarget mCollisionTarget;
}
