package com.github.luohaha.rpc;

import java.util.List;

public class AddRpc implements IRpcFunction{
    @Override
    public Object rpcCall(String function, List<Object> params) {
        int res = (int)params.get(0) + (int)params.get(1);
        return res;
    }
}
