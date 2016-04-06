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
	
	public static void main(String[] args){
		Thread.currentThread().interrupt();
		try {
			System.out.println(Thread.currentThread().isInterrupted());
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println(Thread.currentThread().isInterrupted());
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
