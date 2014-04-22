package com.uacapstone.red.networking;

import org.msgpack.annotation.Message;

import com.uacapstone.red.object.Avatar;

@Message
public class PlayerState implements IState<Avatar> {
	
	public PhysicsBodyState bodyState;
	
	public int id;
	public int playerFeetDown;
	public int direction;
	public boolean stopAnimation;
	public boolean hasJumped;
	
	public PlayerState() {
		
	}

	@Override
	public void apply(Avatar o) {
		bodyState.apply(o.getBody());
		o.setRunDirection(direction);
		o.setNumberOfFeetDown(playerFeetDown);
		o.setHasjumped(hasJumped);
		if (stopAnimation)
			o.stopAnimation();
	}
}
