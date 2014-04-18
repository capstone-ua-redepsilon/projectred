package com.uacapstone.red.networking;

import org.msgpack.annotation.Message;

@Message
public abstract class NetworkMessage implements INetworkMessage{
	
	public abstract short getFlag();
	
	public NetworkMessage() {
		
	}
	
	public byte[] getBytes() {
		try {
			return NetworkingConstants.messagePackInstance.write(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}