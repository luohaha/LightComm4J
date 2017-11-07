package com.github.luohaha.rpc;

import java.util.List;

public interface IRpcFunction {
    public Object rpcCall(String function, List<Object> params);
}
