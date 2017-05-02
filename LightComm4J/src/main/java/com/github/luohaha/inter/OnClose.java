package com.github.luohaha.inter;

import com.github.luohaha.connection.Connection;
import com.github.luohaha.context.Context;

public interface OnClose {
	public void onClose(Connection connection);
}
