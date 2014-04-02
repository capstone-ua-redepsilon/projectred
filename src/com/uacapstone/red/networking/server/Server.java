package com.uacapstone.red.networking.server;

import org.msgpack.MessagePack;
import org.zeromq.ZMQ;

import com.uacapstone.red.networking.client.ClientSetPlayerPositionMessage;
import com.uacapstone.red.networking.messaging.NetworkMessage;

public class Server {
	
	private String port;
	private ZMQ.Context context;
	
	
	public Server(String port) {
		this.context = ZMQ.context(1);
		this.port = port;
	}
	
	public void startServer() throws Exception {
		System.out.println("ZZZ");
		ZMQ.Socket socket = context.socket(ZMQ.REP);

        socket.bind ("tcp://*:"+port);

        MessagePack msgpack = new MessagePack();
        System.out.println("Server is running!");
        
        while (!Thread.currentThread().isInterrupted()) {

            byte[] serverMessageBytes = socket.recv(0);
            NetworkMessage serverMessage = msgpack.read(serverMessageBytes, NetworkMessage.class);
            
            System.out.println("serverMessage received!: " + serverMessage);
            
            NetworkMessage clientMessage = processMessage(serverMessage);
            
            if (clientMessage != null) {
            	byte[] clientMessageBytes = msgpack.write(clientMessage);
            	socket.send(clientMessageBytes);
            }
            
            Thread.sleep(1000);
        }
        
        socket.close();
        this.context.term();
	}
	
	private NetworkMessage processMessage(NetworkMessage message) {
		
		NetworkMessage reply = null;
		
		switch(message.getMessageType()) {
		case ServerMovePlayerMessage:
			System.out.println("Move-player message received.");
			ServerMovePlayerMessage m = (ServerMovePlayerMessage)message;
			reply = new ClientSetPlayerPositionMessage(m.getPlayerId(), m.getX(), m.getY());
			break;
		case ServerNoMessage:
			System.out.println("No message received....");
			break;
		default:
			System.out.println("Unknown message received.");
			break;
		}
		
		return reply;
	}
}
