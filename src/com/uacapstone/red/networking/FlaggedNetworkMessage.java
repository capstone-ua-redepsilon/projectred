package com.uacapstone.red.networking;

import org.msgpack.annotation.Message;

@Message
public class FlaggedNetworkMessage {
	public short messageFlag;
	public byte[] messageBytes;
	
	public FlaggedNetworkMessage() {
		
	}
	
	public FlaggedNetworkMessage(short flag, byte[] messageBytes) {
		this.messageFlag = flag;
		this.messageBytes = messageBytes;
	}
}