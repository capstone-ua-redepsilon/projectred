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
		
		o.setRunDirection(direction);
		o.setNumberOfFeetDown(playerFeetDown);
		if (o.getHasJumped() == false && hasJumped == true) {
			o.jump(false);
		}
		if (o.getHasJumped() == true && hasJumped == false) {
			o.land(false);
		}
		
		if (stopAnimation)
			o.stopAnimation();
		
		bodyState.apply(o.getBody());
	}
}
