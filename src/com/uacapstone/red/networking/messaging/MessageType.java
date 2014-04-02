package com.uacapstone.red.networking.messaging;

//public interface MessageType {
//	public short getMessageType();
//}

public enum MessageType {
	ServerConnectionClosed,
	ServerConnectionEstablished,
	ServerConnectionRejected,
	ServerMovePlayerMessage,
	ServerNoMessage;
}