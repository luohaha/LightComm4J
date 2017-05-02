package com.github.luohaha.server.test;

import java.io.IOException;

import com.github.luohaha.param.ServerParam;
import com.github.luohaha.server.LightCommServer;

public class TestServer {

	public static void main(String[] args) {
		try {
			ServerParam param = new ServerParam("localhost", 8888);
			param.setBacklog(128);
			param.setOnAccept((conn) -> {
				System.out.println("accept!");
			});
			param.setOnRead((conn, data) -> {
				System.out.println("read!");
			});
			param.setOnWrite((conn) -> {
				try {
					conn.write(String.valueOf(System.currentTimeMillis()).getBytes());
					conn.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			param.setOnClose((conn) -> {
				System.out.println("close!");
				try {
					conn.doClose();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			LightCommServer server = new LightCommServer(param, 4);
			server.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
