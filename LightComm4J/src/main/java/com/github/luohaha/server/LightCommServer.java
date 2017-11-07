package com.github.luohaha.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

import com.github.luohaha.connection.Connection;
import com.github.luohaha.connection.DataBag;
import com.github.luohaha.handler.IoHandler;
import com.github.luohaha.inter.OnAccept;
import com.github.luohaha.inter.OnClose;
import com.github.luohaha.inter.OnRead;
import com.github.luohaha.inter.OnWrite;
import com.github.luohaha.param.ServerParam;
import com.github.luohaha.worker.Accepter;
import com.github.luohaha.worker.IoWorker;

public class LightCommServer {
	private ServerParam param;
	private int ioThreadPoolSize = 1;
	
	public LightCommServer(ServerParam serverParam, int ioThreadPoolSize) {
		this.param = serverParam;
		this.ioThreadPoolSize = ioThreadPoolSize;
	}

	/**
	 * start server
	 */
	public void start() {
		Accepter accepter = new Accepter(this.param);
		for (int i = 0; i < ioThreadPoolSize; i++) {
			IoWorker ioWorker = new IoWorker(i);
			accepter.addIoWorker(ioWorker);
			new Thread(ioWorker).start();
			this.param.getLogger().info("[IoWorker-" + i + "]" + " start...");
		}
		new Thread(accepter).start();
		this.param.getLogger().info("[Accepter] start...");
	}
	
}
