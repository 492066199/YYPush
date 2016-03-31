package com.sailing.model;

import java.nio.ByteBuffer;
import java.util.concurrent.Future;

public class FileNode {
	private ByteBuffer bf;
	private long offset;
	private Future<Integer> cnt;
	private long lastFinalLength;
	private boolean hasReadEOF = false; 
	private long curTime;
	
	public ByteBuffer getBf() {
		return bf;
	}
	public void setBf(ByteBuffer bf) {
		this.bf = bf;
	}
	public long getOffset() {
		return offset;
	}
	public void setOffset(long offset) {
		this.offset = offset;
	}
	public Future<Integer> getCnt() {
		return cnt;
	}
	public void setCnt(Future<Integer> cnt) {
		this.cnt = cnt;
	}
	
	public static void main(String[] args) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(100000);
		buffer.clear();
		byte[] src = new byte[]{1, 3, 5, 6, 9};
		buffer.put(src);
		buffer.flip();
		for (int i = 0; i < buffer.limit(); i++) {
			System.out.println(buffer.get());
		}
	}
	public long getLastFinalLength() {
		return lastFinalLength;
	}
	public void setLastFinalLength(long lastFinalLength) {
		this.lastFinalLength = lastFinalLength;
	}
	public boolean isHasReadEOF() {
		return hasReadEOF;
	}
	public void setHasReadEOF(boolean hasReadEOF) {
		this.hasReadEOF = hasReadEOF;
	}
	public long getCurTime() {
		return curTime;
	}
	public void setCurTime(long curTime) {
		this.curTime = curTime;
	}
}
