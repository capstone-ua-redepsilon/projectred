package com.uacapstone.red;

import org.msgpack.MessagePack;
import org.msgpack.annotation.Message;

import org.zeromq.ZMQ;

public class NetworkHandler {
	
	// ==================================
	// Some methods to test Serialization
	@Message
	public static class MyMessage {
		public String name;
		public double version;
	}
	
	// ==================================
	
	public void runtest() throws Exception {
		MyMessage src = new MyMessage();
		src.name = "msgpack";
		src.version = 0.6;
		MessagePack msgpack = new MessagePack();
		
		// Serialize
		System.out.println("Serializing Object to Bytes...");
		byte[] bytes = msgpack.write(src);
		
		System.out.println("result: " + bytes);
		
		// Deserialize
		System.out.println("Deserializing Bytes to Object...");
		MyMessage dst = msgpack.read(bytes, MyMessage.class);
		
		System.out.println("name: " + dst.name);
		System.out.println("version: " + dst.version);
	}
	
	public void testZMQ() {
		ZMQ.Context context = ZMQ.context(1);
	//  Socket to talk to server
        System.out.println("Connecting to hello world server…");

        ZMQ.Socket requester = context.socket(ZMQ.REQ);
        requester.connect("tcp://localhost:5555");

        for (int requestNbr = 0; requestNbr != 10; requestNbr++) {
            String request = "Hello";
            System.out.println("Sending Hello " + requestNbr);
            requester.send(request.getBytes(), 0);

            byte[] reply = requester.recv(0);
            System.out.println("Received " + new String(reply) + " " + requestNbr);
        }
        requester.close();
        context.term();
	}
	
}
