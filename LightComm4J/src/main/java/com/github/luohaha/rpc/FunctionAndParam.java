package com.github.luohaha.rpc;

import java.io.Serializable;
import java.util.List;

public class FunctionAndParam implements Serializable {
    private String function;
    private List<Object> params;

    public FunctionAndParam(String function, List<Object> params) {
        this.function = function;
        this.params = params;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public List<Object> getParams() {
        return params;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }
}
