package com.github.luohaha.param;

import com.github.luohaha.inter.OnAccept;
import com.github.luohaha.inter.OnClose;
import com.github.luohaha.inter.OnConnection;
import com.github.luohaha.inter.OnRead;
import com.github.luohaha.inter.OnWrite;

public abstract class Param {

	private OnRead onRead;
	private OnWrite onWrite;
	private OnClose onClose;
	
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
	
	public abstract OnAccept getOnAccept();
	public abstract OnConnection getOnConnection();
	public abstract boolean isServerParam();
}
