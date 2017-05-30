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
import java.util.logging.Logger;

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
	private Logger logger = Logger.getLogger("LightComm4J");
	
	public LightCommClient(int ioThreadPoolSize) {
		
		this.ioThreadPoolSize = ioThreadPoolSize;
		this.connector = new Connector();
		for (int i = 0; i < this.ioThreadPoolSize; i++) {
			IoWorker ioWorker = new IoWorker(i);
			connector.addWorker(ioWorker);
			new Thread(ioWorker).start();
			this.logger.info("[IoWorker-" + i + "]" + " start...");
		}
		new Thread(connector).start();
		this.logger.info("[Connector]" + " start...");
	}

	public void connect(String host, int port, ClientParam param) {
		this.connector.connect(host, port, param);
	}
	
}
