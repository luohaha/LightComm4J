package com.github.luohaha.client;

import java.util.logging.Logger;
import com.github.luohaha.param.ClientParam;
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
