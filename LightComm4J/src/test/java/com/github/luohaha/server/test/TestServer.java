package com.github.luohaha.server.test;

import java.io.IOException;
import java.net.SocketAddress;

import com.github.luohaha.param.ServerParam;
import com.github.luohaha.server.LightCommServer;

public class TestServer {

	public static void main(String[] args) {
		try {
			ServerParam param = new ServerParam("localhost", 8888);
			param.setBacklog(128);
			param.setOnAccept(conn -> {
				System.out.println("accept!");
				try {
					SocketAddress address = conn.getRemoteAddress();
					System.out.println(address.toString());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			param.setOnRead((conn, data) -> {
				System.out.println("read!");
				System.out.println(new String(data));
				try {
					conn.write(String.valueOf(System.currentTimeMillis()).getBytes());
					conn.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			param.setOnWrite(conn -> {
				System.out.println("write!");
			});
			param.setOnClose(conn -> {
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
