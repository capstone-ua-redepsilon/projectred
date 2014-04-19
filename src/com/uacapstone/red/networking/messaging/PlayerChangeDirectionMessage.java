package com.uacapstone.red.networking.messaging;

import org.msgpack.annotation.Message;

import com.uacapstone.red.networking.NetworkingConstants;


@Message
public class PlayerChangeDirectionMessage extends NetworkMessage {
	public int playerId;
	public int direction;
	
	public PlayerChangeDirectionMessage() {
	}
	
	public short getFlag() {
		return NetworkingConstants.MessageFlags.MESSAGE_FROM_CLIENT_PLAYER_DIRECTION;
	}
}