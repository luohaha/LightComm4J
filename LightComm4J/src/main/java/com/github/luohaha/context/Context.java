package com.github.luohaha.context;

import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

import com.github.luohaha.connection.Connection;
import com.github.luohaha.connection.DataBag;
import com.github.luohaha.param.Param;

public class Context {
	
	private Map<SocketChannel, ContextBean> chanToContextBean = new HashMap<>();

	public Context() {
	
	}

	public Map<SocketChannel, ContextBean> getChanToContextBean() {
		return chanToContextBean;
	}

	public void setChanToContextBean(Map<SocketChannel, ContextBean> chanToContextBean) {
		this.chanToContextBean = chanToContextBean;
	}

	/**
	 * init this channel's context
	 * @param channel
	 * @param connection
	 * @param ops
	 */
	public void initContext(SocketChannel channel, Connection connection, int ops, Param param) {
		ContextBean bean = new ContextBean(connection, new ArrayDeque<>(), new DataBag(), ops, param);
		this.chanToContextBean.put(channel, bean);
	}
	
	/**
	 * remove this channel's context
	 * @param channel
	 */
	public void removeContextByChan(SocketChannel channel) {
		this.chanToContextBean.remove(channel);
	}
}
