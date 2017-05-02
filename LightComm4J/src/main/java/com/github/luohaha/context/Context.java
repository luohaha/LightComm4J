package com.github.luohaha.context;

import java.beans.beancontext.BeanContext;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import com.github.luohaha.connection.Connection;
import com.github.luohaha.connection.DataBag;
import com.github.luohaha.inter.OnClose;
import com.github.luohaha.inter.OnRead;
import com.github.luohaha.param.Param;

public class Context {
	
	private Map<SocketChannel, ContextBean> chanToContextBean = new HashMap<>();

	public Context() throws IOException {
		// TODO Auto-generated constructor stub
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
