package com.uacapstone.red.networking;

import org.msgpack.annotation.Message;

@Message
public class PlayerJumpMessage extends NetworkMessage {

	public PlayerJumpMessage() {
		
	}
	
	@Override
	public short getFlag() {
		// TODO Auto-generated method stub
		return 0;
	}

}