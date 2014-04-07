package com.uacapstone.red.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.adt.message.server.ServerMessage;


public class ConnectionCloseServerMessage extends ServerMessage {
	
	public ConnectionCloseServerMessage() {}
	
	@Override
	public short getFlag() {
		return NetworkingConstants.ServerMessageFlags.CONNECTION_CLOSE;
	}

	@Override
	protected void onReadTransmissionData(final DataInputStream pDataInputStream) throws IOException {
		/* Nothing to read. */
	}

	@Override
	protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream) throws IOException {
		/* Nothing to write. */
	}
}