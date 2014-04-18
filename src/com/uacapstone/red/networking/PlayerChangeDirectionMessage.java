package com.uacapstone.red.networking;

import org.msgpack.annotation.Message;

@Message
public class PlayerChangeDirectionMessage extends NetworkMessage {
	public int playerId;
	public int direction;
	
	public PlayerChangeDirectionMessage() {
		
	}
	
	public PlayerChangeDirectionMessage(int playerId, int direction) {
		this.playerId = playerId;
		this.direction = direction;
	}
	
	public short getFlag() {
		return NetworkingConstants.MessageFlags.MESSAGE_FROM_CLIENT_PLAYER_DIRECTION;
	}
}