package com.github.luohaha.rpc;

public class RpcServerTest {
    public static void main(String[] args) {
        RpcServer rpcServer = new RpcServer("localhost", 8888);
        rpcServer.add("add", new AddRpc());
        rpcServer.start();
    }
}
