package com.github.luohaha.worker;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.github.luohaha.connection.Connection;
import com.github.luohaha.context.Context;
import com.github.luohaha.context.ContextBean;
import com.github.luohaha.handler.IoHandler;
import com.github.luohaha.param.Param;

public class IoWorker extends Worker implements Runnable {
	private int id;
	private Selector selector;
	private Context context;
	private IoHandler ioHandler;
	private BlockingQueue<JobBean> jobBeans = new LinkedBlockingQueue<>();
	private Logger logger = Logger.getLogger("LightComm4J");

	public IoWorker(int id) {
		this.id = id;
		this.context = new Context();
		this.selector = openSelector("[IoWorker-" + this.id + "]" + " selector open : ");
		this.ioHandler = new IoHandler(this.selector, this.context);
	}

	@Override
	public void run() {
		while (true) {
			try {
				this.selector.select();
				JobBean job = jobBeans.poll();
				if (job != null) {
					this.logger.fine("[IoWorker-" + this.id + "]" + " handle new job");
					initSocketChannel(job);
				}
				Set<SelectionKey> keys = this.selector.selectedKeys();
				Iterator<SelectionKey> iterator = keys.iterator();
				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					handle(key);
					iterator.remove();
				}
			} catch (IOException e) {
				// select error
				this.logger.warning("[IoWorker-" + this.id + "]" + " selector : " + e.toString());
				this.selector = openSelector("[IoWorker-" + this.id + "]" + " selector open : ");
			}
		}
	}

	/**
	 * handle read or write event
	 * 
	 * @param key
	 * @throws IOException
	 */
	private void handle(SelectionKey key) {
		SocketChannel channel = (SocketChannel) key.channel();
		ContextBean bean = this.context.getChanToContextBean().get(channel);
		if (key.isReadable()) {
			if (bean.getParam().getOnRead() == null)
				return;
			try {
				String address = channel.getRemoteAddress().toString();
				ioHandler.readDataFromRemoteSite(channel, bean.getParam().getOnRead(), bean.getParam().getOnClose());
				this.logger.info("[IoWorker-" + this.id + "] read data from remote site " + address);
			} catch (IOException e) {
				this.logger.warning("[IoWorker-" + this.id + "] read data from remote site : " + e.toString());
				if (bean.getParam().getOnReadError() != null)
					bean.getParam().getOnReadError().onReadError(bean.getConnection(), e);
				this.context.removeContextByChan(channel);
				try {
					channel.close();
				} catch (IOException e1) {
					// if channel already close
				}
			}
		} else if (key.isWritable()) {
			try {
				String address = channel.getRemoteAddress().toString();
				ioHandler.writeDataToRemoteSite(channel, bean.getParam().getOnWrite());
				this.logger.info("[IoWorker-" + this.id + "] write data to remote site " + address);
			} catch (IOException e) {
				this.logger.warning("[IoWorker-" + this.id + "] write data to remote site : " + e.toString());
				if (bean.getParam().getOnWriteError() != null)
					bean.getParam().getOnWriteError().onWriteError(bean.getConnection(), e);
				this.context.removeContextByChan(channel);
				try {
					channel.close();
				} catch (IOException e1) {
					// if channel already close
				}
			}
		}
	}

	/**
	 * dispatch job to worker
	 * 
	 * @param job
	 */
	public void dispatch(JobBean job) {
		this.jobBeans.add(job);
		this.selector.wakeup();
	}

	/**
	 * init new job
	 * 
	 * @param jobBean
	 * @throws IOException
	 */
	private void initSocketChannel(JobBean jobBean) {
		SocketChannel channel = jobBean.getChannel();
		Param param = jobBean.getParam();
		try {
			channel.configureBlocking(false);
		} catch (IOException e) {
			// channel error
			this.logger.warning("[IoWorker-" + this.id + "] channel : " + e.toString());
			return;
		}
		int ops = 0;
		if (param == null)
			throw new NullPointerException();
		if (param.getOnRead() != null) {
			ops |= SelectionKey.OP_READ;
		}
		ops |= SelectionKey.OP_WRITE;
		try {
			channel.register(this.selector, ops);
		} catch (ClosedChannelException e) {
			// channel close
			this.logger.warning("[IoWorker-" + this.id + "] channel : " + e.toString());
			return;
		}
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
				param.getOnConnection().onConnect(connection);
			}
		}

	}

}
