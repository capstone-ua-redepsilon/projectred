package com.uacapstone.red.networking.client;


import org.msgpack.annotation.Message;

import com.uacapstone.red.networking.messaging.MessageType;
import com.uacapstone.red.networking.messaging.NetworkMessage;

@Message
public class ClientSetPlayerPositionMessage extends NetworkMessage {

	protected short messageType = MessageType.ServerMovePlayerMessage;
	
	private int playerId;
	private int newX;
	private int newY;
	
	public ClientSetPlayerPositionMessage(int playerId, int x, int y) {
		this.playerId = playerId;
		this.newX = x;
		this.newY = y;
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
