package com.github.luohaha.worker;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

public class Worker {
	public Logger logger = Logger.getLogger("LightComm4J");
	/**
	 * open selector
	 * 
	 * @return
	 */
	public Selector openSelector(String msg) {
		Selector selector;
		do {
			try {
				selector = Selector.open();
				// selector open
				return selector;
			} catch (IOException e) {
				this.logger.warning(msg + e.toString());
			}
		} while (true);
	}
	
	/**
	 * open non-blocking socket channel
	 * 
	 * @return
	 */
	public SocketChannel openSocketChannelNonBlocking(String msg) {
		SocketChannel socketChannel;
		do {
			try {
				socketChannel = SocketChannel.open();
				socketChannel.configureBlocking(false);
				return socketChannel;
			} catch (IOException e) {
				this.logger.warning(msg + e.toString());
			}
		} while (true);
	}
	
	/**
	 * open server socket channel
	 * @param msg
	 * @return
	 */
	public ServerSocketChannel openServerSocketChannelNonBlocking(String msg) {
		ServerSocketChannel serverSocketChannel;
		do {
			try {
				serverSocketChannel = ServerSocketChannel.open();
				serverSocketChannel.configureBlocking(false);
				return serverSocketChannel;
			} catch (IOException e) {
				this.logger.warning(msg + e.toString());
			}
		} while (true);
	}
}
