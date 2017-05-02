package com.github.luohaha.inter;

import java.nio.channels.SocketChannel;

import com.github.luohaha.connection.Connection;
import com.github.luohaha.context.Context;

public interface OnAccept {
	public void onAccept(Connection connection);
}
