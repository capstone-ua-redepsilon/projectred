package com.uacapstone.red.networking.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.msgpack.MessagePack;
import org.msgpack.annotation.Message;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

import com.uacapstone.red.networking.messaging.MessageType;
import com.uacapstone.red.networking.messaging.NetworkMessage;

@Message
public class ServerMovePlayerMessage extends NetworkMessage {
	
	protected short messageType = MessageType.ServerMovePlayerMessage;
	
	private int playerId;
	private int dx;
	private int dy;
	
	public ServerMovePlayerMessage() {
		
	}
	
	public ServerMovePlayerMessage(int playerId, int x, int y) {
		this.playerId = playerId;
		this.dx = x;
		this.dy = y;
	}
	
	public int getPlayerId() {
		return playerId;
	}
	
	public int getX() {
		return dx;
	}
	
	public int getY() {
		return dy;
	}
	
	@Override
	public byte[] serialize() throws Exception {
		MessagePack msgpack = new MessagePack();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
        Packer packer = msgpack.createPacker(out);
 
        packer.write(this.getMessageType());
        packer.write(getPlayerId());
        packer.write(getX());
        packer.write(getY());
        
        return out.toByteArray();
	}
	
	@Override 
	public void deserialize(byte[] bytes) throws Exception {
		MessagePack msgpack = new MessagePack();
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		
		Unpacker unpacker = msgpack.createUnpacker(in);
		
		this.messageType = unpacker.readShort();
		this.playerId = unpacker.readInt();
		this.dx = unpacker.readInt();
		this.dy = unpacker.readInt();
	}
}
