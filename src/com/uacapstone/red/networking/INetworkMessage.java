package com.uacapstone.red.networking;

import org.msgpack.annotation.Message;

@Message
public interface INetworkMessage {
	
	public byte[] getBytes();
}