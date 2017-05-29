package com.github.luohaha.worker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.github.luohaha.connection.Connection;
import com.github.luohaha.context.Context;
import com.github.luohaha.handler.IoHandler;
import com.github.luohaha.param.ClientParam;

public class Connector extends Worker implements Runnable {
	private Selector selector;
	private List<IoWorker> workers = new ArrayList<>();
	private int workersIndex = 0;
	private ConcurrentMap<SocketChannel, ClientParam> chanToParam = new ConcurrentHashMap<>();
	private BlockingQueue<SocketChannel> chanQueue = new LinkedBlockingQueue<>();

	public Connector() {
		this.selector = openSelector("[Connector]" + " selector open : ");
	}

	/**
	 * send msg to remote site
	 * 
	 * @param host
	 * @param port
	 * @param msg
	 * @throws IOException
	 */
	public void connect(String host, int port, ClientParam param) {
		// build socket channel
		SocketChannel socketChannel = openSocketChannelNonBlocking("[Connector]" + " socket channel open : ");
		// build connection
		connectToAddress(socketChannel, host, port, param);
	}

	@Override
	public void run() {
		
		while (true) {
			try {
				this.selector.select();
				SocketChannel newChan = this.chanQueue.poll();
				if (newChan != null) {
					try {
						newChan.register(selector, SelectionKey.OP_CONNECT);
					} catch (ClosedChannelException e) {
						logger.warning("[Connector] channel close : " + e.toString());
					}
				}
				Set<SelectionKey> keys = this.selector.selectedKeys();
				Iterator<SelectionKey> iterator = keys.iterator();
				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					handle(key);
					iterator.remove();
				}
			} catch (IOException e1) {
				this.logger.warning("[Connector] select error : " + e1.toString());
				this.selector = openSelector("[Connector]" + " selector open : ");
			}
		}
	}

	private void handle(SelectionKey key) {
		SocketChannel channel = (SocketChannel) key.channel();
		if (key.isConnectable()) {
			try {
				if (channel.finishConnect()) {
					//connect finish
					this.logger.info("[Connecter] finish connect " + channel.getRemoteAddress().toString());
					IoWorker worker = this.workers.get(workersIndex);
					worker.dispatch(new JobBean(channel, this.chanToParam.get(channel)));
					workersIndex = (workersIndex + 1) % workers.size();
				}
			} catch (IOException e) {
				this.logger.info("[Connecter] finish connect error : " + e.toString());
				ClientParam clientParam = this.chanToParam.get(channel);
				if (clientParam.getOnConnectError() != null) {
					clientParam.getOnConnectError().onConnectError(e);
				}
				this.chanToParam.remove(channel);
				try {
					channel.close();
				} catch (IOException e1) {
					// already close
				}
			}
		}
	}

	/**
	 * add io worker
	 * 
	 * @param worker
	 */
	public void addWorker(IoWorker worker) {
		this.workers.add(worker);
	}

	/**
	 * connect to address
	 * 
	 * @param socketChannel
	 * @param address
	 * @param param
	 */
	private void connectToAddress(SocketChannel socketChannel, String host, int port, ClientParam param) {
		SocketAddress address = new InetSocketAddress(host, port);
		try {
			socketChannel.connect(address);
			// connect success
			this.chanToParam.put(socketChannel, param);
			this.chanQueue.add(socketChannel);
			this.selector.wakeup();
		} catch (IOException e) {
			this.logger.warning("[Connector] connect to " + host + ":" + port + " fail");
			if (param.getOnConnectError() != null)
				param.getOnConnectError().onConnectError(e);
		}
	}

}
