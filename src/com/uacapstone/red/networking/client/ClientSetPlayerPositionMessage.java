package com.uacapstone.red.networking.client;


import org.msgpack.annotation.Message;

import com.uacapstone.red.networking.messaging.MessageType;
import com.uacapstone.red.networking.server.ServerMessage;

@Message
public class ClientSetPlayerPositionMessage extends ServerMessage {

	private int playerId;
	private int newX;
	private int newY;
	
	public ClientSetPlayerPositionMessage(int playerId, int x, int y) {
		this.playerId = playerId;
		this.newX = x;
		this.newY = y;
	}
	
	@Override
	public MessageType getMessageType() {
		return MessageType.ServerMovePlayerMessage;
	}
	
	public int getPlayerId() {
		return playerId;
	}
	
	public int getX() {
		return newX;
	}
	
	public int getY() {
		return newY;
	}
}
