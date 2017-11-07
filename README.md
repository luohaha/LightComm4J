# LightComm4J

Yet another asynchronous network library for java

## Install

>Maven

```java
<dependency>
  <groupId>com.github.luohaha</groupId>
  <artifactId>LightComm4J</artifactId>
  <version>1.0.0</version>
</dependency>
```

>Download jar

[download](https://oss.sonatype.org/service/local/repositories/releases/content/com/github/luohaha/LightComm4J/1.0.0/LightComm4J-1.0.0.jar)

## How to use

* server-side

First, we need to creates a socket address from a hostname and a port number. And then, we should define some callback function when some event happens. We use `ServerParam` to store these informations.

```java
ServerParam param = new ServerParam("localhost", 8888);
// set backlog
param.setBacklog(128);
param.setOnAccept(conn -> {
	System.out.println("accept!");
});
param.setOnRead((conn, data) -> {
	System.out.println("read!");
});
param.setOnWrite(conn -> {
	System.out.println("write!");
});
param.setOnClose(conn -> {
	System.out.println("close!");
});
param.setOnReadError((conn, err) -> {
	System.out.println(err.getMessage());
});
param.setOnWriteError((conn, err) -> {
	System.out.println(err.getMessage());
});
param.setOnAcceptError(err -> {
	System.out.println(err.getMessage());
});
```

`OnAccept` will be called when accepter return a new connection. `OnRead` will be called when socket's recv buffer isn't empty. `OnWrite` will be called at first time when socket's send buffer isn't empty and it will be call just once. When remote side close socket, then `OnClose` be called, but if we close server side socket first, this function will not be called forever.

Finally, we can start our server using 'ServerParam' and need to decide the size of io thread pool.

```java
// 4 is the io thread pool's size
LightCommServer server = new LightCommServer(param, 4);
server.start();
```

* client-side

The usage of client side is similar to server side. First, we also need to create a `ClientParam` which is just like `ServerParam`, and define some callback function in it.

```java
ClientParam param = new ClientParam();

param.setOnConnection((conn) -> {
	System.out.println("connect!");
});
param.setOnWrite((conn) -> {
	System.out.println("write");
});
param.setOnRead((conn, data) -> {
	System.out.println("read");
});
param.setOnClose((conn) -> {
	System.out.println("close");
});
param.setOnReadError((conn, err) -> {
	System.out.println(err.getMessage());
});
param.setOnWriteError((conn, err) -> {
	System.out.println(err.getMessage());
});
param.setOnConnError(err -> {
	System.out.println(err.getMessage());
});
```

`OnConnection` will be called when connection is built. `OnWrite`, `OnRead` and `OnClose` are as same as server side.

Finally, we can use client to send message.

```java
// 4 is the io thread pool's size 
LightCommClient client = new LightCommClient(4);
client.connect("localhost", 8888, param);
client.connect("localhost", 8889, param);
```

## Interface

* `OnAccept`

```java
public interface OnAccept {
	public void onAccept(Conn connection);
}
```

* `OnClose`

```java
public interface OnClose {
	public void onClose(Conn connection);
}
```

* `OnConnection`

```java
public interface OnConnection {
	public void onConnection(Conn connection);
}
```

* `OnRead`

```java
public interface OnRead {
	public void onRead(Conn connection, byte[] data);
}
```

* `OnWrite`

```java
public interface OnWrite {
	public void onWrite(Conn connection);
}
```

* `OnConnError`

```java
public interface OnConnError {
	public void onConnError(Exception e);
}
```

* `OnAcceptError`

```java
public interface OnAcceptError {
	public void onAcceptError(Exception e);
}
```

* `OnReadError`

```java
public interface OnReadError {
	public void onReadError(Conn conn, Exception e);
}
```

* `OnWriteError`

```java
public interface OnWriteError {
	public void onWriteError(Conn conn, Exception e);
}
```

* `Conn`

We can send message, close connection and set tcp options by `Conn`.

```java
public interface Conn {
    // send data to remote side
	public void write(byte[] data) throws ConnectionCloseException, ClosedChannelException;
    // close connection when nothing to send
	public void close() throws IOException;
    // close connection immediately
	public void doClose() throws IOException;
    // get local address
	public SocketAddress getLocalAddress() throws IOException;
    // get remote address
	public SocketAddress getRemoteAddress() throws IOException;
    // set socket's send buffer' size
	public void setSendBuffer(int size) throws IOException;
    // set socket's recv buffer' size
	public void setRecvBuffer(int size) throws IOException;
    // open keep-alive mode
	public void setKeepAlive(boolean flag) throws IOException;
    // open reuse address mode
	public void setReUseAddr(boolean flag) throws IOException;
    // no use nagle algorithm
	public void setNoDelay(boolean flag) throws IOException;
     // get socket's send buffer' size
	public int getSendBuffer() throws IOException;
    // get socket's recv buffer' size
	public int getRecvBuffer() throws IOException;
	public boolean getKeepAlive() throws IOException;
	public boolean getReUseAddr() throws IOException;
	public boolean getNoDelay() throws IOException;
}
```

## How to use rpc

* server-side  

First, we need to create `RpcServer`, set host address and port. Then we need to add remote call function's name and `IRpcFunction`. Finally, start server.

```java
RpcServer rpcServer = new RpcServer("localhost", 8888);
rpcServer.add("add", new AddRpc());
rpcServer.start();
```

```java
public class AddRpc implements IRpcFunction{
    @Override
    public Object rpcCall(String function, List<Object> params) {
        int res = (int)params.get(0) + (int)params.get(1);
        return res;
    }
}
```

* client-side

First, we need to create `RpcClient`, then start it. Then we can use `remote` and `call` to finish remote process call.

```java
RpcClient client = new RpcClient();
// need to start client, before remote call.
client.start();
client.remote("localhost", 8888).call("add", 1, 2);
```

## Example

we use `LightComm4J` to build a simple chatroom.

* server-side

```java
public class ChatRoomServer {
	public static void main(String[] args) {
		ServerParam param = new ServerParam("localhost", 8888);
		Set<Conn> conns = new HashSet<>();
		param.setBacklog(128);
		param.setOnAccept(conn -> {
			try {
				String m = conn.getRemoteAddress().toString() + " " + "is online!";
				conns.add(conn);
				conns.forEach(c -> {
					try {
						c.write(m.getBytes());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		param.setOnRead((conn, msg) -> {
			try {
				String m = conn.getRemoteAddress().toString() + " : " + new String(msg);
				conns.forEach(c -> {
					try {
						c.write(m.getBytes());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		param.setOnClose(conn -> {
			try {
				conns.remove(conn);
				String m = conn.getRemoteAddress().toString() + " " + "is offline!";
				conns.forEach(c -> {
					try {
						c.write(m.getBytes());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		param.setOnReadError((conn, err) -> {
			System.out.println(err.getMessage());
		});
		param.setOnWriteError((conn, err) -> {
			System.out.println(err.getMessage());
		});
		param.setOnAcceptError(err -> {
			System.out.println(err.getMessage());
		});
		
		LightCommServer server = new LightCommServer(param, 4);
		try {
			server.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

```

* client-side

```java
public class ChatRoomClient {
	public static void main(String[] args) {
		ClientParam param = new ClientParam();
		param.setOnConnection(conn -> {
			new Thread(() -> {
				Scanner scanner = new Scanner(System.in);
				while (scanner.hasNext()) {
					String msg = scanner.nextLine();
					try {
						conn.write(msg.getBytes());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
		});
		param.setOnRead((conn, msg) -> {
			System.out.println("[chatroom] " + new String(msg));
		});
		param.setOnClose(conn -> {
			System.out.println("[chatroom] " + "chatroom close!");
		});
		try {
			LightCommClient client = new LightCommClient(4);
			client.connect("localhost", 8888, param);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

```

## Example for RPC

* server

```java
public class RpcServerTest {
    public static void main(String[] args) {
        RpcServer rpcServer = new RpcServer("localhost", 8888);
        rpcServer.add("add", new AddRpc());
        rpcServer.start();
    }
}
```

```java
public class AddRpc implements IRpcFunction{
    @Override
    public Object rpcCall(String function, List<Object> params) {
        int res = (int)params.get(0) + (int)params.get(1);
        return res;
    }
}
```

* client

```java
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
        System.out.println("1 + 2 = " + add(1, 2));
        System.out.println("1 + 2 = " + addRpc(1, 2));
    }

}
```