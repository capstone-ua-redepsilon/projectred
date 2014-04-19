package com.uacapstone.red.networking.messaging;

import org.msgpack.annotation.Message;

import com.uacapstone.red.networking.NetworkingConstants.MessageFlags;

@Message
public class SetHostMessage extends NetworkMessage {

	public String participantId;
	
	@Override
	public short getFlag() {
		return MessageFlags.MESSAGE_SET_HOST;
	}
	
}
