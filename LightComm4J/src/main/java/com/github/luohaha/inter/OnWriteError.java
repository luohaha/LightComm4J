package com.github.luohaha.inter;

import com.github.luohaha.connection.Conn;

public interface OnWriteError {
	public void onWriteError(Conn conn, Exception e);
}
