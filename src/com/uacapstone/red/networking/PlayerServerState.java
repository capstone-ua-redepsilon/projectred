package com.uacapstone.red.networking;

import org.msgpack.annotation.Message;

import com.uacapstone.red.object.Avatar;

@Message
public class PlayerServerState {
	public int id;
	public float bodyPositionX, bodyPositionY;
	public float bodyVelocityX, bodyVelocityY;
	public int playerFeetDown;
	public int direction;
	public boolean hasJumped;
	
	public PlayerServerState() {
		
	}

	public void applyToPlayer(Avatar p) {
		p.getBody().setTransform(bodyPositionX, bodyPositionY, p.getBody().getAngle());
		p.getBody().setLinearVelocity(bodyVelocityX, bodyVelocityY);
		p.setRunDirection(direction);
		p.setNumberOfFeetDown(playerFeetDown);
		p.setHasjumped(hasJumped);
	}
}
