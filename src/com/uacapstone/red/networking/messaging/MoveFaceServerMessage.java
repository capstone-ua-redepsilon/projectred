package com.uacapstone.red.networking.messaging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.adt.message.server.ServerMessage;

import com.uacapstone.red.networking.NetworkingConstants;


public class MoveFaceServerMessage extends ServerMessage {
    public int mID;
    public float mX;
    public float mY;

    public MoveFaceServerMessage() {

    }

    public MoveFaceServerMessage(final int pID, final float pX, final float pY) {
        this.mID = pID;
        this.mX = pX;
        this.mY = pY;
    }

    public void set(final int pID, final float pX, final float pY) {
        this.mID = pID;
        this.mX = pX;
        this.mY = pY;
    }

    @Override
    public short getFlag() {
        return NetworkingConstants.ServerMessageFlags.MOVE_FACE;
    }

    @Override
    protected void onReadTransmissionData(final DataInputStream pDataInputStream) throws IOException {
        this.mID = pDataInputStream.readInt();
        this.mX = pDataInputStream.readFloat();
        this.mY = pDataInputStream.readFloat();
    }

    @Override
    protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream) throws IOException {
        pDataOutputStream.writeInt(this.mID);
        pDataOutputStream.writeFloat(this.mX);
        pDataOutputStream.writeFloat(this.mY);
    }
}
