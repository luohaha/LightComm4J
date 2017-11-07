package com.github.luohaha.rpc;

import com.github.luohaha.client.LightCommClient;
import com.github.luohaha.connection.Conn;
import com.github.luohaha.exception.ConnectionCloseException;
import com.github.luohaha.inter.OnRead;
import com.github.luohaha.param.ClientParam;
import com.github.luohaha.tools.ObjectAndByteArray;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RpcClient {

    private int ioThreadPoolSize = 1;
    private ClientParam param;
    private LightCommClient client;
    private String host;
    private int port;
    private Logger logger = Logger.getLogger("LightComm4J");

    public RpcClient() {
        this.param = new ClientParam();
    }

    public void start() {
        this.client = new LightCommClient(ioThreadPoolSize);
    }

    public void setLogLevel(Level level) {
        this.param.setLogLevel(level);
    }

    public void setIoThreadPoolSize(int ioThreadPoolSize) {
        this.ioThreadPoolSize = ioThreadPoolSize;
    }

    public void addLogHandler(Handler handler) {
        this.param.addLogHandler(handler);
    }

    public RpcClient remote(String host, int port) {
        this.host = host;
        this.port = port;
        return this;
    }

    /**
     * 远程调用，同步模式
     * @param function
     * 函数名
     * @param fparamList
     * 参数
     * @return
     * 返回结果
     */
    public Object call(String function, Object... fparamList) {
        List<Object> fparams = new ArrayList<>();
        for (Object each : fparamList) {
            fparams.add(each);
        }
        FunctionAndParam functionAndParam = new FunctionAndParam(function, fparams);
        this.param.setOnConnect(conn -> {
            try {
                conn.write(ObjectAndByteArray.toByteArray(functionAndParam));
            } catch (ConnectionCloseException e) {
                this.logger.warning(e.toString());
            } catch (ClosedChannelException e) {
                this.logger.warning(e.toString());
            }
        });
        RpcRead rpcRead = new RpcRead();
        this.param.setOnRead(rpcRead);
        this.client.connect(this.host, this.port, param);
        return rpcRead.getData();
    }

    private class RpcRead implements OnRead {

        private Object data;
        private CountDownLatch countDownLatch = new CountDownLatch(1);

        @Override
        public void onRead(Conn connection, byte[] data) {
            this.data = ObjectAndByteArray.toObject(data);
            try {
                connection.doClose();
            } catch (IOException e) {
                logger.warning(e.toString());
            }
            countDownLatch.countDown();
        }

        public Object getData() {
            do {
                try {
                    countDownLatch.await();
                    break;
                } catch (InterruptedException e) {
                    logger.warning(e.toString());
                }
            } while (true);
            return data;
        }
    }
}
