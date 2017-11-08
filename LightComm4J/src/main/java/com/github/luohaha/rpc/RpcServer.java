package com.github.luohaha.rpc;

import com.github.luohaha.connection.Conn;
import com.github.luohaha.exception.ConnectionCloseException;
import com.github.luohaha.inter.OnAcceptError;
import com.github.luohaha.inter.OnReadError;
import com.github.luohaha.inter.OnWriteError;
import com.github.luohaha.param.ServerParam;
import com.github.luohaha.server.LightCommServer;
import com.github.luohaha.tools.ObjectAndByteArray;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RpcServer {
    private Map<String, IRpcFunction> functions = new HashMap<>();
    private ServerParam param;
    private Logger logger = Logger.getLogger("LightComm4J");
    private int ioThreadPoolSize = 1;

    public RpcServer(String host, int port) {
        this.param = new ServerParam(host, port);
    }

    public void start() {
        this.param.setOnRead((conn, data) -> {
            FunctionAndParam functionAndParam = (FunctionAndParam) ObjectAndByteArray.toObject(data);
            Object res = get(functionAndParam.getFunction())
                    .rpcCall(functionAndParam.getFunction(), functionAndParam.getParams());
            try {
                conn.write(ObjectAndByteArray.toByteArray(res));
            } catch (ConnectionCloseException e) {
                logger.warning(e.toString());
            } catch (ClosedChannelException e) {
                logger.warning(e.toString());
            }
        });
        this.param.setOnClose(conn -> {
            try {
                conn.doClose();
            } catch (IOException e) {
                this.logger.warning(e.toString());
            }
        });
        ErrorHandle errorHandle = new ErrorHandle();
        this.param.setOnAcceptError(errorHandle);
        this.param.setOnReadError(errorHandle);
        this.param.setOnWriteError(errorHandle);
        LightCommServer server = new LightCommServer(param, ioThreadPoolSize);
        server.start();
    }

    public void setBacklog(int backlog) {
        this.param.setBacklog(backlog);
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


    public void add(String function, IRpcFunction rpcFunction) {
        this.functions.put(function, rpcFunction);
    }

    public void remove(String function) {
        this.functions.remove(function);
    }

    public IRpcFunction get(String function) {
        return this.functions.get(function);
    }

    private class ErrorHandle implements OnAcceptError, OnReadError, OnWriteError {

        @Override
        public void onAcceptError(Exception e) {
            logger.warning(e.toString());
        }

        @Override
        public void onReadError(Conn conn, Exception e) {
            logger.warning(e.toString());
            try {
                conn.doClose();
            } catch (IOException e1) {
                logger.warning(e1.toString());
            }
        }

        @Override
        public void onWriteError(Conn conn, Exception e) {
            logger.warning(e.toString());
            try {
                conn.doClose();
            } catch (IOException e1) {
                logger.warning(e1.toString());
            }
        }
    }
}
