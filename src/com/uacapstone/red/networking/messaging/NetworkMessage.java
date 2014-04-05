package com.uacapstone.red.networking.messaging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class NetworkMessage implements MessageType {
	
	protected short messageType;
	
	public short getMessageType() {
		return messageType;
	}
	
	public byte[] serialize() throws Exception {
		MessagePack msgpack = new MessagePack();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
        Packer packer = msgpack.createPacker(out);
 
        packer.write(this.getMessageType());
        
        return out.toByteArray();
	}
	
	public void deserialize(byte[] bytes) throws Exception {
		
		MessagePack msgpack = new MessagePack();
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		
		Unpacker unpacker = msgpack.createUnpacker(in);
		
		this.messageType = unpacker.readShort();
		
	}
}