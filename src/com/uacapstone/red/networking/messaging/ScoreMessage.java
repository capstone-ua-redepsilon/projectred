package com.uacapstone.red.networking.messaging;

import org.msgpack.annotation.Message;

import com.uacapstone.red.networking.NetworkingConstants.MessageFlags;

@Message
public class ScoreMessage extends NetworkMessage {
	
	public int score;
	
	public ScoreMessage() {
		
	}
	
	public short getFlag() {
		return MessageFlags.MESSAGE_SCORE;
	}
}
