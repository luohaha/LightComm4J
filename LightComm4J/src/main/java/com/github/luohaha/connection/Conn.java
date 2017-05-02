package com.github.luohaha.connection;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;

import com.github.luohaha.exception.ConnectionCloseException;

public interface Conn {
	public void write(byte[] data) throws ConnectionCloseException, ClosedChannelException;
	public void close() throws IOException;
	public void doClose() throws IOException;
	public SocketAddress getLocalAddress() throws IOException;
	public SocketAddress getRemoteAddress() throws IOException;
	public void setSendBuffer(int size) throws IOException;
	public void setRecvBuffer(int size) throws IOException;
	public void setKeepAlive(boolean flag) throws IOException;
	public void setReUseAddr(boolean flag) throws IOException;
	public void setNoDelay(boolean flag) throws IOException;
	public int getSendBuffer() throws IOException;
	public int getRecvBuffer() throws IOException;
	public boolean getKeepAlive() throws IOException;
	public boolean getReUseAddr() throws IOException;
	public boolean getNoDelay() throws IOException;
}
