package com.github.luohaha.connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.github.luohaha.context.Context;
import com.github.luohaha.context.ContextBean;
import com.github.luohaha.exception.ConnectionCloseException;

public class Connection {
	private Context context;
	private SocketChannel channel;
	private Selector selector;
	private BlockingQueue<ByteBuffer> readyToWrite = new LinkedBlockingQueue<>();
	// write function could be call just once
	private boolean onWriteCalled = false;
	private boolean readyToClose = false;
	
	public Connection(Context context, SocketChannel channel, Selector selector) {
		super();
		this.context = context;
		this.channel = channel;
		this.selector = selector;
	}

	public void write(byte[] data) throws ConnectionCloseException, ClosedChannelException {
		if (readyToClose)
			throw new ConnectionCloseException();
		ContextBean bean = context.getChanToContextBean().get(channel);
		ByteBuffer buffer = ByteBuffer.allocate(data.length + 4);
		buffer.putInt(data.length);
		buffer.put(data);
		buffer.flip();
		readyToWrite.add(buffer);
		int ops = bean.getOps();
		ops |= SelectionKey.OP_WRITE;
		bean.setOps(ops);
		this.channel.register(this.selector, ops);
	}
	
	/**
	 * set close flag
	 * @throws IOException 
	 */
	public void close() throws IOException {
		this.readyToClose = true;
		if (this.readyToWrite.isEmpty()) {
			doClose();
		}
	}
	
	/**
	 * close channel
	 * @throws IOException
	 */
	public void doClose() throws IOException {
		this.context.removeContextByChan(channel);
		this.channel.close();
	}

	public BlockingQueue<ByteBuffer> getReadyToWrite() {
		return readyToWrite;
	}

	public boolean isOnWriteCalled() {
		return onWriteCalled;
	}

	public void setOnWriteCalled(boolean onWriteCalled) {
		this.onWriteCalled = onWriteCalled;
	}

	public boolean isReadyToClose() {
		return readyToClose;
	}

	public void setReadyToClose(boolean readyToClose) {
		this.readyToClose = readyToClose;
	}
	
	
}
