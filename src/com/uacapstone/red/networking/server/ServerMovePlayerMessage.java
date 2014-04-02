package com.uacapstone.red.networking.server;

import org.msgpack.MessagePack;
import org.msgpack.annotation.Message;

import com.uacapstone.red.networking.messaging.MessageType;

@Message
public class ServerMovePlayerMessage extends ServerMessage {

	private int playerId;
	private int dx;
	private int dy;
	
	public ServerMovePlayerMessage(int playerId, int x, int y) {
		this.playerId = playerId;
		this.dx = x;
		this.dy = y;
	}
	
	@Override
	public MessageType getMessageType() {
		return MessageType.ServerMovePlayerMessage;
	}
	
	public int getPlayerId() {
		return playerId;
	}
	
	public int getX() {
		return dx;
	}
	
	public int getY() {
		return dy;
	}
	
//	@Override
//	public byte[] getBytes() {
//		MessagePack msgpack = new MessagePack();
//		byte[] ret = null;
//		try {
//			ret = msgpack.write(this);
//		} catch(Exception e) {}
//		return ret;
//	}
}
