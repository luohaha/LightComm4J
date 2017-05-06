package com.github.luohaha.param;

import com.github.luohaha.inter.OnAccept;
import com.github.luohaha.inter.OnClose;
import com.github.luohaha.inter.OnConnError;
import com.github.luohaha.inter.OnConnection;
import com.github.luohaha.inter.OnRead;
import com.github.luohaha.inter.OnWrite;

public class ClientParam extends Param {
	private OnConnection onConnection;
	private OnConnError onConnError;
	
	public ClientParam() {
		// TODO Auto-generated constructor stub
	}

	public OnConnError getOnConnError() {
		return onConnError;
	}


	public void setOnConnError(OnConnError onConnError) {
		this.onConnError = onConnError;
	}


	public void setOnConnection(OnConnection onConnection) {
		this.onConnection = onConnection;
	}

	@Override
	public OnAccept getOnAccept() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OnConnection getOnConnection() {
		// TODO Auto-generated method stub
		return this.onConnection;
	}


	@Override
	public boolean isServerParam() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
