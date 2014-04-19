package com.uacapstone.red.networking.messaging;

import org.msgpack.annotation.Message;

@Message
public interface INetworkMessage {
	
	public byte[] getBytes();
}