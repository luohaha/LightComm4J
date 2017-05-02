package com.github.luohaha.server.test;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

import com.github.luohaha.client.LightCommClient;
import com.github.luohaha.param.ClientParam;

public class TestClient {

	public static void main(String[] args) throws IOException {
		ClientParam param = new ClientParam();
		param.setOnConnection((conn) -> {
			System.out.println("connect!");
		});
		param.setOnWrite((conn) -> {
			System.out.println("write");
		});
		param.setOnRead((conn, data) -> {
			System.out.println("read");
			System.out.println(new String(data));
		});
		param.setOnClose((conn) -> {
			System.out.println("close");
			try {
				conn.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		LightCommClient client = new LightCommClient(4);
		client.connect("localhost", 8888, param);
		client.connect("localhost", 8888, param);
		client.connect("localhost", 8888, param);
		client.connect("localhost", 8888, param);
	}
}
