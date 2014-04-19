package com.uacapstone.red.networking.messaging;

import org.msgpack.annotation.Message;

import com.uacapstone.red.networking.NetworkingConstants;


@Message
public class PlayerJumpMessage extends NetworkMessage {

	public int playerId;
	
	public PlayerJumpMessage() {

	}
	
	@Override
	public short getFlag() {
		// TODO Auto-generated method stub
		return NetworkingConstants.MessageFlags.MESSAGE_FROM_CLIENT_PLAYER_JUMP;
	}

}