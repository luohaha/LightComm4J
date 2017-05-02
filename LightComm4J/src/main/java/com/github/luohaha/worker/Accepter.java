package com.github.luohaha.worker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.github.luohaha.param.ServerParam;

public class Accepter implements Runnable {
	private ServerSocketChannel channel;
	private Selector selector;
	private ServerParam param;
	private List<IoWorker> workers = new ArrayList<>();
	private int workerIndex = 0;

	public Accepter(ServerParam param) throws IOException {
		this.selector = Selector.open();
		this.channel = ServerSocketChannel.open();
		this.channel.configureBlocking(false);
		this.param = param;
		this.channel.socket().bind(new InetSocketAddress(param.getHost(), param.getPort()),
				this.param.getBacklog());
	}
	
	/**
	 * add worker thread
	 * @param worker
	 */
	public void addIoWorker(IoWorker worker) {
		workers.add(worker);
	}

	public void accept() throws ClosedChannelException {
		this.channel.register(this.selector, SelectionKey.OP_ACCEPT);
		while (true) {
			try {
				this.selector.select();
				Set<SelectionKey> keys = this.selector.selectedKeys();
				Iterator<SelectionKey> iterator = keys.iterator();
				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					try {
						handle(key);
					} catch (Exception e) {
						e.printStackTrace();
					}
					iterator.remove();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private void handle(SelectionKey key) throws IOException, InterruptedException {
		if (key.isAcceptable()) {
			/*
			 * accept
			 */
			ServerSocketChannel server = (ServerSocketChannel) key.channel();
			SocketChannel channel = server.accept();
			IoWorker worker = workers.get(workerIndex);
			worker.dispatch(new JobBean(channel, this.param));
			workerIndex = (workerIndex + 1) % workers.size();
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			accept();
		} catch (ClosedChannelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
