package com.uacapstone.red.networking.messaging;

import org.msgpack.annotation.Message;

import com.badlogic.gdx.math.Vector2;
import com.uacapstone.red.networking.NetworkingConstants;

@Message
public class GameStateMessage extends NetworkMessage {
	public int id;
	public Vector2 bodyPosition;
	public Vector2 bodyVelocity;
	public int playerFeetDown;
	public int direction;
	
	public GameStateMessage() {
		
	}
	
	@Override
	public short getFlag() {
		// TODO Auto-generated method stub
		return NetworkingConstants.MessageFlags.MESSAGE_FROM_SERVER_PLAYER_STATE;
	}
}