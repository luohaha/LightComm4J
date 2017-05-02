package com.github.luohaha.worker;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.github.luohaha.connection.Connection;
import com.github.luohaha.context.Context;
import com.github.luohaha.context.ContextBean;
import com.github.luohaha.handler.IoHandler;
import com.github.luohaha.param.ClientParam;
import com.github.luohaha.param.Param;
import com.github.luohaha.param.ServerParam;

public class IoWorker implements Runnable {
	private Selector selector;
	private Context context;
	private IoHandler ioHandler;
	private BlockingQueue<JobBean> jobBeans = new LinkedBlockingQueue<>();
	
	public IoWorker() throws IOException {
		this.context = new Context();
		this.selector = Selector.open();
		this.ioHandler = new IoHandler(this.selector, this.context);
	}

	@Override
	public void run() {
		while (true) {
			try {
				this.selector.select();
				JobBean job = jobBeans.poll();
				if (job != null) {
					initSocketChannel(job);
				}
				Set<SelectionKey> keys = this.selector.selectedKeys();
				Iterator<SelectionKey> iterator = keys.iterator();
				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					try {
						handle(key);
					} catch (IOException e) {
						e.printStackTrace();
					}
					iterator.remove();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * handle read or write event
	 * @param key
	 * @throws IOException
	 */
	private void handle(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		ContextBean bean = this.context.getChanToContextBean().get(channel);
		if (key.isReadable()) {
			if (bean.getParam().getOnRead() == null)
				return;
			ioHandler.readDataFromRemoteSite(channel, bean.getParam().getOnRead(), bean.getParam().getOnClose());
		} else if (key.isWritable()) {
			ioHandler.writeDataToRemoteSite(channel, bean.getParam().getOnWrite());
		}
	}
	
	/**
	 * dispatch job to worker
	 * @param job
	 * @throws InterruptedException
	 */
	public void dispatch(JobBean job) throws InterruptedException {
		this.jobBeans.put(job);
		this.selector.wakeup();
	}
	
	/**
	 * init new job
	 * @param jobBean
	 * @throws IOException
	 */
	private void initSocketChannel(JobBean jobBean) throws IOException {
		SocketChannel channel = jobBean.getChannel();
		Param param = jobBean.getParam();
		channel.configureBlocking(false);
		int ops = 0;
		if (param.getOnRead() != null) {
			ops |= SelectionKey.OP_READ;
		}
		if (param.getOnWrite() != null) {
			ops |= SelectionKey.OP_WRITE;
		}
		channel.register(this.selector, ops);
		// new connection
		Connection connection = new Connection(this.context, channel, this.selector);
		// init context
		this.context.initContext(channel, connection, ops, param);
		// call on accept or on connection
		if (param.isServerParam()) {
			if (param.getOnAccept() != null) {
				param.getOnAccept().onAccept(connection);
			}
		} else {
			if (param.getOnConnection() != null) {
				param.getOnConnection().onConnection(connection);
			}
		}
		
	}

}
