package com.uacapstone.red.networking.messaging;

import org.msgpack.annotation.Message;

@Message
public class FlaggedNetworkMessage extends NetworkMessage {
	public short messageFlag;
	public byte[] messageBytes;
	
	public FlaggedNetworkMessage() {
		
	}
	
	public FlaggedNetworkMessage(NetworkMessage message) {
		this.messageFlag = message.getFlag();
		this.messageBytes = message.getBytes();
	}
}