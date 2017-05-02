package com.github.luohaha.param;

import com.github.luohaha.inter.OnAccept;
import com.github.luohaha.inter.OnClose;
import com.github.luohaha.inter.OnConnection;
import com.github.luohaha.inter.OnRead;
import com.github.luohaha.inter.OnWrite;

public class ServerParam extends Param {
	private String host;
	private int port;
	private int backlog = 32;
	private OnAccept onAccept;
	
	public ServerParam(String host, int port) {
		// TODO Auto-generated constructor stub
		this.host = host;
		this.port = port;
	}

	public int getBacklog() {
		return backlog;
	}

	public void setBacklog(int backlog) {
		this.backlog = backlog;
	}


	public void setOnAccept(OnAccept onAccept) {
		this.onAccept = onAccept;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public OnAccept getOnAccept() {
		// TODO Auto-generated method stub
		return this.onAccept;
	}

	@Override
	public OnConnection getOnConnection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isServerParam() {
		// TODO Auto-generated method stub
		return true;
	}
	
}
