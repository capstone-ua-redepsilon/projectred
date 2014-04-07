package com.uacapstone.red.networking;

public class NetworkingConstants
{	
	public static final String LOCALHOST_IP = "127.0.0.1";
	public static final int SERVER_PORT = 4444;
	
	public class ClientMessageFlags {
		public static final short CONNECTION_CLOSE = 0;
		public static final short CONNECTION_ESTABLISH = 1;
		public static final short CONNECTION_PING = 2;
	}
	
	public class ServerMessageFlags {
		public static final short CONNECTION_CLOSE = 0;
		public static final short CONNECTION_ESTABLISHED = 1;
		public static final short CONNECTION_REJECTED_PROTOCOL_MISSMATCH = 2;
		public static final short CONNECTION_PONG = 3;
		
		public static final short ADD_FACE = 4;
		public static final short MOVE_FACE = 5;
	}
}