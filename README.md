# LightComm4J
Yet another asynchronous network library for java

## Install


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
```

`OnAccept` will be called when accepter return a new connection. `OnRead` will be called when socket's recv buffer isn't empty. `OnWrite` will be called at first time when socket's send buffer isn't empty and it will be call just once. When remote side client close socket, then `OnClose` be called, but if we close server side socket first, this function will not be called forever.

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