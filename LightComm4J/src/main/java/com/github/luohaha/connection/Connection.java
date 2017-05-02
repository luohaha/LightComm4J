package com.github.luohaha.connection;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
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

public class Connection implements Conn {
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

	@Override
	public SocketAddress getLocalAddress() throws IOException {
		// TODO Auto-generated method stub
		return this.channel.getLocalAddress();
	}

	@Override
	public SocketAddress getRemoteAddress() throws IOException {
		// TODO Auto-generated method stub
		return this.channel.getRemoteAddress();
	}
	
	/**
	 * set send buffer's size
	 */
	public void setSendBuffer(int size) throws IOException {
		this.channel.setOption(StandardSocketOptions.SO_SNDBUF, size);
	}
	
	/**
	 * get send buffer' size
	 * @return
	 * @throws IOException
	 */
	public int getSendBuffer() throws IOException {
		return this.channel.getOption(StandardSocketOptions.SO_SNDBUF);
	}
	
	/**
	 * set recv buffer's size
	 */
	public void setRecvBuffer(int size) throws IOException {
		this.channel.setOption(StandardSocketOptions.SO_RCVBUF, size);
	}
	
	/**
	 * get recv buffer's size
	 * @return
	 * @throws IOException
	 */
	public int getRecvBuffer() throws IOException {
		return this.channel.getOption(StandardSocketOptions.SO_RCVBUF);
	}
	
	/**
	 * set keep alive
	 */
	public void setKeepAlive(boolean flag) throws IOException {
		this.channel.setOption(StandardSocketOptions.SO_KEEPALIVE, flag);
	}
	
	/**
	 * get keep alive
	 * @return
	 * @throws IOException
	 */
	public boolean getKeepAlive() throws IOException {
		return this.channel.getOption(StandardSocketOptions.SO_KEEPALIVE);
	}
	
	/**
	 * set reuse address
	 */
	public void setReUseAddr(boolean flag) throws IOException {
		this.channel.setOption(StandardSocketOptions.SO_REUSEADDR, flag);
	}
	
	/**
	 * get reuse address
	 * @return
	 * @throws IOException
	 */
	public boolean getReUseAddr() throws IOException {
		return this.channel.getOption(StandardSocketOptions.SO_REUSEADDR);
	}
	
	/**
	 * set no delay
	 */
	public void setNoDelay(boolean flag) throws IOException {
		this.channel.setOption(StandardSocketOptions.TCP_NODELAY, flag);
	}
	
	/**
	 * get no delay
	 * @return
	 * @throws IOException
	 */
	public boolean getNoDelay() throws IOException {
		return this.channel.getOption(StandardSocketOptions.TCP_NODELAY);
	}
}
