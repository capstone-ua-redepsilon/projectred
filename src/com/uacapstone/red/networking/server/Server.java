package com.uacapstone.red.networking.server;

import org.msgpack.MessagePack;
import org.zeromq.ZMQ;

import com.uacapstone.red.networking.client.ClientSetPlayerPositionMessage;
import com.uacapstone.red.networking.messaging.MessageType;
import com.uacapstone.red.networking.messaging.NetworkMessage;

public class Server {
	
	private String port;
	private ZMQ.Context context;
	
	
	public Server(String port) {
		this.context = ZMQ.context(1);
		this.port = port;
	}
	
	public void startServer() throws Exception {
		ZMQ.Socket socket = context.socket(ZMQ.REP);

        socket.bind ("tcp://*:"+port);

//        MessagePack msgpack = new MessagePack();
        System.out.println("Server is running!");
        
        while (!Thread.currentThread().isInterrupted()) {

            byte[] serverMessageBytes = socket.recv(0);
            NetworkMessage serverMessage = new NetworkMessage();
            serverMessage.deserialize(serverMessageBytes);
            
            NetworkMessage reply = null;
            
            switch (serverMessage.getMessageType()) {
            case MessageType.ServerMovePlayerMessage:
            	System.out.println("Move-player message received.");
    			ServerMovePlayerMessage m = new ServerMovePlayerMessage();
    			m.deserialize(serverMessageBytes);
    			
    			reply = new ClientSetPlayerPositionMessage(m.getPlayerId(), m.getX(), m.getY());
    			break;
            case MessageType.ServerNoMessage:
            	System.out.println("No message received....");
    			break;
    		default:
    			System.out.println("Unknown message received.");
    			break;
            }
            
            System.out.println("serverMessage received!: " + serverMessage);
            
//            NetworkMessage clientMessage = processMessage(serverMessage);
            
            NetworkMessage clientMessage = reply;
            
            if (clientMessage != null) {
            	socket.send(clientMessage.serialize());
            }
            
            Thread.sleep(1000);
        }
        
        socket.close();
        this.context.term();
	}
}
