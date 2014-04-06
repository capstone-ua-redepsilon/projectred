package com.uacapstone.red.networking.messaging;

public interface ServerMessageFlags {
	public static final short FLAG_MESSAGE_SERVER_CONNECTION_CLOSE = 0;
	public static final short FLAG_MESSAGE_SERVER_CONNECTION_ESTABLISHED = 1;
	public static final short FLAG_MESSAGE_SERVER_CONNECTION_REJECTED_PROTOCOL_MISSMATCH = 2;
	public static final short FLAG_MESSAGE_SERVER_CONNECTION_PONG = 3;
	
	public static final short FLAG_MESSAGE_SERVER_ADD_FACE = 4;
	public static final short FLAG_MESSAGE_SERVER_MOVE_FACE = 5;
}