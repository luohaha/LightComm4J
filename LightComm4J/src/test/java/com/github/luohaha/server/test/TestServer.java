package com.github.luohaha.server.test;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import com.github.luohaha.param.ServerParam;
import com.github.luohaha.server.LightCommServer;

public class TestServer {

	public static void main(String[] args) {
		try {
			AtomicInteger count = new AtomicInteger(0);
			ServerParam param = new ServerParam("localhost", 8888);
			param.setLogLevel(Level.WARNING);
			param.setBacklog(128);
			param.setOnRead((conn, data) -> {
				try {
					conn.write(String.valueOf(count.incrementAndGet()).getBytes());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			param.setOnClose(conn -> {
				try {
					//System.out.println(System.currentTimeMillis());
					conn.doClose();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			param.setOnReadError((conn, err) -> {
				System.out.println(err.getMessage());
			});
			param.setOnWriteError((conn, err) -> {
				System.out.println(err.getMessage());
			});
			param.setOnAcceptError(err -> {
				System.out.println(err.getMessage());
			});
			LightCommServer server = new LightCommServer(param, 4);
			server.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
