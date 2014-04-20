package com.uacapstone.red.networking.messaging;

import java.util.Date;

import org.msgpack.annotation.Message;

@Message
public class FlaggedNetworkMessage extends NetworkMessage {
	public short messageFlag;
	public byte[] messageBytes;
	public long timestamp = new Date().getTime();
	
	
	public FlaggedNetworkMessage() {
		
	}
	
	public FlaggedNetworkMessage(NetworkMessage message) {
		this.messageFlag = message.getFlag();
		this.messageBytes = message.getBytes();
	}
}