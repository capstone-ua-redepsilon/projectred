package com.uacapstone.red.networking;

import org.msgpack.MessagePack;


public class NetworkingConstants
{	
//	public static final String LOCALHOST_IP = "127.0.0.1";
//	public static final int SERVER_PORT = 4444;
	
	public static final MessagePack messagePackInstance = new MessagePack();
	
	public class MessageFlags {
		public static final short MESSAGE_NONE = 0;
		public static final short MESSAGE_FROM_CLIENT_PLAYER_DIRECTION = 1;
		public static final short MESSAGE_FROM_CLIENT_PLAYER_JUMP = 2;
		
		public static final short MESSAGE_FROM_SERVER_PLAYER_STATE = 3;
		public static final short MESSAGE_SET_HOST = 4;
		
	}
}