package com.github.luohaha.handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Queue;

import com.github.luohaha.connection.Connection;
import com.github.luohaha.connection.DataBag;
import com.github.luohaha.context.Context;
import com.github.luohaha.context.ContextBean;
import com.github.luohaha.inter.OnClose;
import com.github.luohaha.inter.OnRead;
import com.github.luohaha.inter.OnWrite;

public class IoHandler {
	private Context context;
	private Selector selector;
	private static final int BUFFER_SIZE = 1024;

	public IoHandler(Selector selector, Context context) {
		super();
		this.context = context;
		this.selector = selector;
	}

	/**
	 * read data from remote site by channel
	 * 
	 * @param channel
	 * @param onRead
	 * @param onClose
	 * @throws IOException
	 */
	public void readDataFromRemoteSite(SocketChannel channel, OnRead onRead, OnClose onClose) throws IOException {
		// store current data
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
		// read from remote side
		int count = channel.read(buffer);
		ContextBean bean = this.context.getChanToContextBean().get(channel);
		if (count >= 0) {
			// set buffer's position to 0
			buffer.flip();
			while (buffer.hasRemaining()) {
				DataBag bag = bean.getReadyToRead();
				bag.readFrom(buffer);
				if (bag.isFinish()) {
					// finish read one data bag
					bean.getAlreadyReadData().add(bag.getBytes());
					bean.setReadyToRead(new DataBag());
				}
			}
			// call user's custom function
			Queue<byte[]> dataQueue = bean.getAlreadyReadData();
			while (!dataQueue.isEmpty()) {
				onRead.onRead(bean.getConnection(), dataQueue.poll());
			}
		} else {
			// read end
			closeRead(channel);
			if (onClose != null)
				onClose.onClose(bean.getConnection());
		}
	}

	/**
	 * write data to remote site
	 * 
	 * @param channel
	 * @param onWrite
	 * @throws IOException
	 */
	public void writeDataToRemoteSite(SocketChannel channel, OnWrite onWrite) throws IOException {
		ContextBean bean = this.context.getChanToContextBean().get(channel);
		Connection connection = bean.getConnection();
		// call write function when user define such function and haven't call
		// it yet!
		if (onWrite != null && !connection.isOnWriteCalled()) {
			connection.setOnWriteCalled(true);
			onWrite.onWrite(connection);
		}

		ByteBuffer buffer = connection.getReadyToWrite().peek();
		if (buffer != null) {
			if (buffer.hasRemaining()) {
				channel.write(buffer);
			}
			// if this buffer finish write to buffer, delete it from queue
			if (!buffer.hasRemaining()) {
				connection.getReadyToWrite().poll();
			}
		}

		// nothing to write
		if (connection.getReadyToWrite().isEmpty()) {
			closeWrite(channel);
			if (connection.isReadyToClose()) {
				connection.doClose();
			}
			return;
		}
	}

	/**
	 * close write event
	 * 
	 * @param channel
	 * @throws ClosedChannelException
	 */
	private void closeWrite(SocketChannel channel) throws ClosedChannelException {
		closeOps(channel, SelectionKey.OP_WRITE);
	}

	/**
	 * close read event
	 * 
	 * @param channel
	 * @throws ClosedChannelException
	 */
	private void closeRead(SocketChannel channel) throws ClosedChannelException {
		closeOps(channel, SelectionKey.OP_READ);
	}

	/**
	 * close some operations
	 * 
	 * @param channel
	 * @param opsToClose
	 * @throws ClosedChannelException
	 */
	private void closeOps(SocketChannel channel, int opsToClose) throws ClosedChannelException {
		ContextBean bean = this.context.getChanToContextBean().get(channel);
		int ops = bean.getOps();
		ops = (~opsToClose) & ops;
		bean.setOps(ops);
		channel.register(this.selector, ops);
	}
}
