package com.github.luohaha.param;

import com.github.luohaha.inter.OnAccept;
import com.github.luohaha.inter.OnClose;
import com.github.luohaha.inter.OnConnectError;
import com.github.luohaha.inter.OnConnect;
import com.github.luohaha.inter.OnRead;
import com.github.luohaha.inter.OnWrite;

public class ClientParam extends Param {
	private OnConnect onConnect;
	private OnConnectError onConnectError;
	
	public ClientParam() {
		// 
	}

	public OnConnect getOnConnect() {
		return onConnect;
	}

	public void setOnConnect(OnConnect onConnect) {
		this.onConnect = onConnect;
	}

	public OnConnectError getOnConnectError() {
		return onConnectError;
	}

	public void setOnConnectError(OnConnectError onConnectError) {
		this.onConnectError = onConnectError;
	}



	@Override
	public OnAccept getOnAccept() {
		return null;
	}

	@Override
	public OnConnect getOnConnection() {
		return this.onConnect;
	}


	@Override
	public boolean isServerParam() {
		return false;
	}
	
}
