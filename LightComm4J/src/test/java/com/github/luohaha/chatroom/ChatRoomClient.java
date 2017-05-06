package com.github.luohaha.chatroom;

import java.io.IOException;
import java.util.Scanner;

import com.github.luohaha.client.LightCommClient;
import com.github.luohaha.param.ClientParam;

public class ChatRoomClient {
	public static void main(String[] args) {
		ClientParam param = new ClientParam();
		param.setOnConnection(conn -> {
			new Thread(() -> {
				Scanner scanner = new Scanner(System.in);
				while (scanner.hasNext()) {
					String msg = scanner.nextLine();
					try {
						conn.write(msg.getBytes());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
		});
		param.setOnRead((conn, msg) -> {
			System.out.println("[chatroom] " + new String(msg));
		});
		param.setOnClose(conn -> {
			System.out.println("[chatroom] " + "chatroom close!");
		});
		try {
			LightCommClient client = new LightCommClient(4);
			client.connect("localhost", 8888, param);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
