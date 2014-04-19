package com.uacapstone.red.networking.messaging;

import org.msgpack.annotation.Message;

import com.uacapstone.red.networking.NetworkingConstants;
import com.uacapstone.red.networking.NetworkingConstants.MessageFlags;

@Message
public class NetworkMessage implements INetworkMessage{
	
	public short getFlag() {
		return MessageFlags.MESSAGE_NONE;
	}
	
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