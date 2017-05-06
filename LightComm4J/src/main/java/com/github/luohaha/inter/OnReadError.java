package com.github.luohaha.inter;

import com.github.luohaha.connection.Conn;

public interface OnReadError {
	public void onReadError(Conn conn, Exception e);
}
