package com.uacapstone.red.networking;

import org.msgpack.annotation.Message;

import com.uacapstone.red.object.Wizard;

@Message
public class WizardPlayerState implements IState<Wizard> {
	
	public PlayerState playerState;
	
	public boolean didCastTornado;
	
	public WizardPlayerState() {
		
	}

	@Override
	public void apply(Wizard o) {
		playerState.apply(o);
		
		if (didCastTornado) {
			o.castTornado(false);
		}
		// TODO Auto-generated method stub
		
	}

}
