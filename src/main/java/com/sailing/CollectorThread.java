package com.sailing;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.sailing.config.Config;

public class CollectorThread implements Runnable{
	private static Logger log = Logger.getLogger(CollectorThread.class);
	private final CountDownLatch countDownLatch = new CountDownLatch(1); 
	private Thread curThread = null;
	private Config config;
	
	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	public CollectorThread(Config config) {
		this.config = config;
	}
	
	@Override
	public void run() {
		curThread = Thread.currentThread();
		LogCollector lc = null;
		try {
			lc = LogCollector.build(config);
			log.info("build thread collector success: " + config.name);
			lc.process();
		} catch (ExecutionException | TimeoutException | IOException e) {
			e.printStackTrace();
		} finally {
			if (lc != null) {
				lc.destroy();
			}
			countDownLatch.countDown();
		}
	}
	
	public void stop(){
		try {
			curThread.interrupt();
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
