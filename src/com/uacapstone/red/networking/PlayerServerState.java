package com.uacapstone.red.networking;

import org.msgpack.annotation.Message;

import com.uacapstone.red.object.Player;

@Message
public class PlayerServerState {
	public int id;
	public float bodyPositionX, bodyPositionY;
	public float bodyVelocityX, bodyVelocityY;
	public int playerFeetDown;
	public int direction;
	
	public PlayerServerState() {
		
	}

	public void applyToPlayer(Player p) {
		p.getBody().setTransform(bodyPositionX, bodyPositionY, p.getBody().getAngle());
		p.getBody().setLinearVelocity(bodyVelocityX, bodyPositionY);
		p.setRunDirection(direction);
		
	}
}
