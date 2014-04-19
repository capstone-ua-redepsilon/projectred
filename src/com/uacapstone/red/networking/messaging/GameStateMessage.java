package com.uacapstone.red.networking.messaging;

import java.util.Date;
import java.util.List;

import org.msgpack.annotation.Message;

import com.uacapstone.red.networking.NetworkingConstants;
import com.uacapstone.red.networking.PlayerServerState;

@Message
public class GameStateMessage extends NetworkMessage {
	
	public List<PlayerServerState> playerServerStates;
	public Date timestamp;
	
	public GameStateMessage() {
		
	}
	
	@Override
	public short getFlag() {
		// TODO Auto-generated method stub
		return NetworkingConstants.MessageFlags.MESSAGE_FROM_SERVER_PLAYER_STATE;
	}
}