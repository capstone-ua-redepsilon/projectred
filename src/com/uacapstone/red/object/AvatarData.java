package com.uacapstone.red.object;


public class AvatarData extends UserData {
	
	public CollisionData mCollisionData;
	
	public AvatarData(CollisionData collisionData)
	{
		super(collisionData.mDescription);
		mCollisionData = collisionData;
	}
	
}
