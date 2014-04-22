package com.uacapstone.red.networking.messaging;

import java.util.Date;

import org.msgpack.annotation.Message;

import com.uacapstone.red.networking.NetworkingConstants;
import com.uacapstone.red.networking.WizardPlayerState;

@Message
public class WizardPlayerStateMessage extends NetworkMessage {
	
	public WizardPlayerState state;
	
	public WizardPlayerStateMessage() {
		
	}
	
	@Override
	public short getFlag() {
		// TODO Auto-generated method stub
		return NetworkingConstants.MessageFlags.MESSAGE_WIZARD_PLAYER_STATE;
	}
}