package com.github.luohaha.worker;

import java.nio.channels.SocketChannel;

import com.github.luohaha.param.Param;

public class JobBean {
	private SocketChannel channel;
	private Param param;
	public JobBean(SocketChannel channel, Param param) {
		super();
		this.channel = channel;
		this.param = param;
	}
	public SocketChannel getChannel() {
		return channel;
	}
	public void setChannel(SocketChannel channel) {
		this.channel = channel;
	}
	public Param getParam() {
		return param;
	}
	public void setParam(Param param) {
		this.param = param;
	}
	
	
}
