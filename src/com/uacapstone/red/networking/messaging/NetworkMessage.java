package com.uacapstone.red.networking.messaging;

import org.msgpack.annotation.Message;

@Message
public class NetworkMessage {
	
	public MessageType getMessageType() {
		return MessageType.ServerNoMessage;
	};
}