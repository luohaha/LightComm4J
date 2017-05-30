package com.github.luohaha.connection;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

public class DataBag {

	private byte[] tmpBuffer;
	private int size;
	private int pos;
	private int remainToRead = -1;
	private boolean isFinish = false;
	private static final int CHUNK_SIZE = 1024;
	
	public DataBag() {
		this.size = CHUNK_SIZE;
		this.pos = 0;
		tmpBuffer = new byte[this.size];
	}
	
	/**
	 * read data from byte buffer
	 * @param buffer
	 * read from this buffer
	 * @return
	 * buffer's current position
	 */
	public int readFrom(ByteBuffer buffer) {
		int start = buffer.position();
		byte[] data = new byte[buffer.remaining()];
		buffer.get(data);
		int pos = readFromBytes(data);
		buffer.position(start + pos);
		return pos;
	}
	
	private int readFromBytes(byte[] data) {
		// if buffer's size isn't enough
		while (this.pos + data.length > this.size) {
			extendMemory();
		}
		if (remainToRead == -1) {
			// begin to get data
			if (this.pos + data.length >= 4) {
				// data's position
				int i = 0;
				for (; this.pos < 4; i++) {
					tmpBuffer[this.pos++] = data[i];
				}
				remainToRead = byteToInteger(tmpBuffer);
				for (; i < data.length && remainToRead > 0; i++) {
					tmpBuffer[this.pos++] = data[i];
					remainToRead--;
				}
				if (remainToRead == 0) {
					this.isFinish = true;
				}
				return i;
			} else {
				for (int i = 0; i < data.length; i++) {
					tmpBuffer[this.pos++] = data[i];
				}
				return data.length;
			}
		} else {
			// continue to get data
			if (isFinish)
				return 0;
			// data's position
			int i = 0;
			for (; i < data.length && remainToRead > 0; i++) {
				tmpBuffer[this.pos++] = data[i];
				remainToRead--;
			}
			if (remainToRead == 0) {
				this.isFinish = true;
			}
			return i;
		}
	}
	
	public boolean isFinish() {
		return isFinish;
	}
	
	/**
	 * get bytebuffer from tmpBuffer, when it finish
	 * @return
	 * buffer
	 */
	public ByteBuffer getByteBuffer() {
		if (isFinish()) {
			int length = byteToInteger(tmpBuffer);
			ByteBuffer buffer = ByteBuffer.allocate(length);
			buffer.put(tmpBuffer, 4, length);
			return buffer;
		} else {
			return null;
		}
	}
	
	/**
	 * get bytes from tmpBuffer, when it finish
	 * @return
	 * return byte array from tmpBuffer
	 */
	public byte[] getBytes() {
		if (!isFinish())
			return null;
		byte[] res = new byte[this.pos - 4];
		for (int i = 0; i < res.length; i++) {
			res[i] = this.tmpBuffer[i + 4];
		}
		return res;
	}

	/**
	 * byte转int，采用小端字节序
	 * @param data
	 * byte
	 * @return
	 * int
	 */
	private int byteToInteger(byte[] data) {
		return byteToIntegerFromPos(data, 0);
	}
	
	/**
	 * byte转int，采用小端字节序，从start位置开始
	 * @param data
	 * byte
	 * @param start
	 * start position
	 * @return
	 * int
	 */
	private int byteToIntegerFromPos(byte[] data, int start) {
		return data[start + 3] & 0xff |
			   (data[start + 2] & 0xff) << 8 |
			   (data[start + 1] & 0xff) << 16 |
			   (data[start + 0] & 0xff) << 24;
	}
	
	/**
	 * extend tmpBuffer's size
	 */
	private void extendMemory() {
		this.size += CHUNK_SIZE;
		byte[] newBuffer = new byte[this.size];
		for (int i = 0; i < this.pos; i++) {
			newBuffer[i] = tmpBuffer[i];
		}
		this.tmpBuffer = newBuffer;
	}
	
	/*
	public static void main(String[] args) {
		ByteBuffer buffer = ByteBuffer.allocate(16);
		buffer.putInt(45);
		buffer.putInt(34);
		buffer.putInt(4);
		buffer.flip();
		DataBag bag = new DataBag();
		byte[] res = new byte[12];
		buffer.get(res);
		
	}*/
}
