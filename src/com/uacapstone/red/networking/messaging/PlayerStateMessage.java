package com.uacapstone.red.networking.messaging;

import java.util.Date;
import java.util.List;

import org.msgpack.annotation.Message;

import com.uacapstone.red.networking.NetworkingConstants;
import com.uacapstone.red.networking.PlayerState;

@Message
public class PlayerStateMessage extends NetworkMessage {
	
//	public List<PlayerState> playerStates;
	public PlayerState state;
	
	public PlayerStateMessage() {
		
	}
	
	@Override
	public short getFlag() {
		// TODO Auto-generated method stub
		return NetworkingConstants.MessageFlags.MESSAGE_FROM_SERVER_PLAYER_STATE;
	}
}