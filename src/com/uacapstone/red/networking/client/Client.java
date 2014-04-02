package com.uacapstone.red.networking.client;

import org.msgpack.MessagePack;
import org.zeromq.ZMQ;

import com.uacapstone.red.networking.messaging.NetworkMessage;

public class Client {
	
	private String serverPort;
	private String serverAddress;
	private ZMQ.Context context;
	private ZMQ.Socket socket;
	
	public Client(String serverAddress, String serverPort) {
		this.context = ZMQ.context(1);
		this.serverPort = serverPort;
		this.serverAddress = serverAddress;
	}
	
	public void connectToServer() {
		socket = context.socket(ZMQ.REQ);

		socket.connect(String.format("tcp://%s:%s",serverAddress,serverPort));
	}
	
	public void sendMessage(NetworkMessage message) throws Exception {
		
		MessagePack msgpack = new MessagePack();
		System.out.println("Sending message: " + msgpack);
		socket.send(msgpack.write(message));
	}
	
	public void closeConnection() {
		socket.close();
        this.context.term();
	}

}
