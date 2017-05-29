package com.github.luohaha.param;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.luohaha.inter.OnAccept;
import com.github.luohaha.inter.OnClose;
import com.github.luohaha.inter.OnConnectError;
import com.github.luohaha.inter.OnConnect;
import com.github.luohaha.inter.OnRead;
import com.github.luohaha.inter.OnReadError;
import com.github.luohaha.inter.OnWrite;
import com.github.luohaha.inter.OnWriteError;

public abstract class Param {

	// logger
	private Logger logger = Logger.getLogger("LightComm4J");

	private OnRead onRead;
	private OnReadError onReadError;
	private OnWrite onWrite;
	private OnWriteError onWriteError;
	private OnClose onClose;

	public OnReadError getOnReadError() {
		return onReadError;
	}
	public void setOnReadError(OnReadError onReadError) {
		this.onReadError = onReadError;
	}
	public OnWriteError getOnWriteError() {
		return onWriteError;
	}
	public void setOnWriteError(OnWriteError onWriteError) {
		this.onWriteError = onWriteError;
	}
	public OnRead getOnRead() {
		return onRead;
	}
	public void setOnRead(OnRead onRead) {
		this.onRead = onRead;
	}
	public OnWrite getOnWrite() {
		return onWrite;
	}
	public void setOnWrite(OnWrite onWrite) {
		this.onWrite = onWrite;
	}
	public OnClose getOnClose() {
		return onClose;
	}
	public void setOnClose(OnClose onClose) {
		this.onClose = onClose;
	}
	
	public void addLogHandler(Handler handler) {
		this.logger.addHandler(handler);
	}
	
	public void setLogLevel(Level level) {
		this.logger.setLevel(level);
	}
	
	public Logger getLogger() {
		return logger;
	}
	public abstract OnAccept getOnAccept();
	public abstract OnConnect getOnConnection();
	public abstract boolean isServerParam();
}
