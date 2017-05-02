package com.github.luohaha.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.github.luohaha.connection.Connection;
import com.github.luohaha.context.Context;
import com.github.luohaha.handler.IoHandler;
import com.github.luohaha.param.ClientParam;
import com.github.luohaha.param.Param;
import com.github.luohaha.worker.Connector;
import com.github.luohaha.worker.IoWorker;

public class LightCommClient {
	private Connector connector;
	private int ioThreadPoolSize = 1;
	
	public LightCommClient(int ioThreadPoolSize) throws IOException {
		// TODO Auto-generated constructor stub
		this.ioThreadPoolSize = ioThreadPoolSize;
		this.connector = new Connector();
		for (int i = 0; i < this.ioThreadPoolSize; i++) {
			try {
				IoWorker ioWorker = new IoWorker();
				connector.addWorker(ioWorker);
				new Thread(ioWorker).start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		new Thread(connector).start();
	}

	public void connect(String host, int port, ClientParam param) throws IOException {
		this.connector.connect(host, port, param);
	}
	
}
