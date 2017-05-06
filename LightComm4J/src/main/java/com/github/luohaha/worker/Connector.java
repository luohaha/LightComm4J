package com.github.luohaha.worker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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

import com.github.luohaha.connection.Connection;
import com.github.luohaha.context.Context;
import com.github.luohaha.handler.IoHandler;
import com.github.luohaha.param.ClientParam;

public class Connector implements Runnable {
	private Selector selector;
	private List<IoWorker> workers = new ArrayList<>();
	private int workersIndex = 0;
	private ConcurrentMap<SocketChannel, ClientParam> chanToParam = new ConcurrentHashMap<>();
	private BlockingQueue<SocketChannel> chanQueue = new LinkedBlockingQueue<>();
	
	public Connector() throws IOException {
		this.selector = Selector.open();
	}
	
	/**
	 * send msg to remote site
	 * @param host
	 * @param port
	 * @param msg
	 * @throws IOException 
	 */
	public void connect(String host, int port, ClientParam param) throws IOException {
		SocketChannel socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);
		this.chanToParam.put(socketChannel, param);
		this.chanQueue.add(socketChannel);
		// build connection
		SocketAddress address = new InetSocketAddress(host, port);
		socketChannel.connect(address);
		this.selector.wakeup();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			try {
				this.selector.select();
				SocketChannel newChan = this.chanQueue.poll();
				if (newChan != null) {
					newChan.register(selector, SelectionKey.OP_CONNECT);
				}
				Set<SelectionKey> keys = this.selector.selectedKeys();
				Iterator<SelectionKey> iterator = keys.iterator();
				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					handle(key);
					iterator.remove();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void handle(SelectionKey key) {
		SocketChannel channel = (SocketChannel) key.channel();
		if (key.isConnectable()) {
			try {
				if (channel.finishConnect()) {
					IoWorker worker = this.workers.get(workersIndex);
					worker.dispatch(new JobBean(channel, this.chanToParam.get(channel)));
					workersIndex = (workersIndex + 1) % workers.size();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				ClientParam clientParam = this.chanToParam.get(channel);
				if (clientParam.getOnConnError() != null) {
					clientParam.getOnConnError().onConnError(e);
				}
			}
		}
	}
	
	/**
	 * add io worker
	 * @param worker
	 */
	public void addWorker(IoWorker worker) {
		this.workers.add(worker);
	}
}
