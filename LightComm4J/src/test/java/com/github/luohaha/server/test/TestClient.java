package com.github.luohaha.server.test;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

import com.github.luohaha.client.LightCommClient;
import com.github.luohaha.param.ClientParam;

public class TestClient {

	public static void main(String[] args) throws IOException {
		AtomicInteger clientCount = new AtomicInteger(0);
		ClientParam param = new ClientParam();
		param.setLogLevel(Level.WARNING);
		FileHandler fileHandler = new FileHandler("./test.log");
		fileHandler.setFormatter(new SimpleFormatter());
		param.addLogHandler(fileHandler);
		param.setOnWrite((conn) -> {
			try {
				conn.write("hello".getBytes());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		param.setOnRead((conn, data) -> {
			//System.out.println(new String(data));
			clientCount.incrementAndGet();
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
		param.setOnConnectError(err -> {
			System.out.println(err.getMessage());
		});
		LightCommClient client = new LightCommClient(4);
		int count = 6000;
		for (int i = 0; i < count; i++) {
			client.connect("localhost", 8888, param);
		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fileHandler.flush();
		System.out.println(count + " -> " + clientCount.get());
		
	}
}
