package com.uacapstone.red.networking;

import org.msgpack.annotation.Message;

@Message
public interface IState<E extends Object> {

	public void apply(E o);
}
