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

public class Accepter extends Worker implements Runnable {
	private ServerSocketChannel channel;
	private Selector selector;
	private ServerParam param;
	private List<IoWorker> workers = new ArrayList<>();
	private int workerIndex = 0;

	public Accepter(ServerParam param) {
		this.selector = openSelector("[Accepter] open selector");
		this.channel = openServerSocketChannelNonBlocking("[Accepter] open server socket channel");
		this.param = param;
		bindAddress(this.channel, this.param);
	}

	/**
	 * add worker thread
	 * 
	 * @param worker
	 * worker
	 */
	public void addIoWorker(IoWorker worker) {
		workers.add(worker);
	}

	public void accept() {
		registerChannel();
		// select
		while (true) {
			try {
				this.selector.select();
				Set<SelectionKey> keys = this.selector.selectedKeys();
				Iterator<SelectionKey> iterator = keys.iterator();
				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					handle(key);
					iterator.remove();
				}
			} catch (IOException e) {
				this.logger.warning("[Accepter] select : " + e.toString());
				this.selector = openSelector("[Accepter] select : ");
				registerChannel();
			}
		}
	}

	private void handle(SelectionKey key) {
		if (key.isAcceptable()) {
			/*
			 * accept
			 */
			ServerSocketChannel server = (ServerSocketChannel) key.channel();
			try {
				SocketChannel channel = server.accept();
				this.logger.info("[Accept] accept : " + channel.getRemoteAddress().toString());
				IoWorker worker = workers.get(workerIndex);
				worker.dispatch(new JobBean(channel, this.param));
				workerIndex = (workerIndex + 1) % workers.size();
			} catch (IOException e) {
				// accepter error
				this.logger.warning("[Accept] accept : " + e.toString());
				if (param.getOnAcceptError() != null) {
					param.getOnAcceptError().onAcceptError(e);
				}
				registerChannel();
			}
		}
	}

	@Override
	public void run() {
		accept();
	}

	/**
	 * bind address
	 * 
	 * @param serverSocketChannel
	 * serverSocketChannel
	 * @param serverParam
	 * serverParam
	 */
	private void bindAddress(ServerSocketChannel serverSocketChannel, ServerParam serverParam) {
		do {
			try {
				serverSocketChannel.socket().bind(new InetSocketAddress(serverParam.getHost(), serverParam.getPort()),
						serverParam.getBacklog());
				break;
			} catch (IOException e) {
				this.logger.warning("[Accepter] bind address : " + e.toString());
			}
		} while (true);
	}

	/**
	 * register
	 */
	private void registerChannel() {
		// register
		do {
			try {
				this.channel.register(this.selector, SelectionKey.OP_ACCEPT);
				break;
			} catch (ClosedChannelException e) {
				this.logger.warning("[Accepter] register : " + e.toString());
				this.channel = openServerSocketChannelNonBlocking("[Accepter] open server socket channel ");
				bindAddress(this.channel, this.param);
			}
		} while (true);
	}
}
