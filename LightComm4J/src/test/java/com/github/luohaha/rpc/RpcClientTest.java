package com.github.luohaha.rpc;

import java.util.Random;

public class RpcClientTest {

    private static RpcClient client = new RpcClient();

    public static int add(int a, int b) {
        return a + b;
    }

    public static int addRpc(int a, int b) {
        return (int)client.remote("localhost", 8888).call("add", a, b);
    }

    public static void main(String[] args) {
        client.start();
        int succCount = 0;
        int totalCount = 1000;
        Random random = new Random();
        for (int i = 0; i < totalCount; i++) {
            int a = random.nextInt(100);
            int b = random.nextInt(100);
            if (add(a, b) == addRpc(a, b)) {
                succCount++;
            }
        }
        if (succCount == totalCount) {
            System.out.println("Success");
        } else {
            System.out.println("fail");
        }
    }

}
