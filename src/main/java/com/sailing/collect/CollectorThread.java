package com.sailing.collect;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.sailing.config.Config;

public class CollectorThread implements Runnable{
	private static Logger log = Logger.getLogger(CollectorThread.class);
	private final CountDownLatch countDownLatch = new CountDownLatch(1); 
	private Future<?> future = null;

	public void setFuture(Future<?> future) {
		this.future = future;
	}

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
		Collector lc = null;
		try {
			lc = DirLogCollector.build(config);
			if(lc == null){
				log.info("build thread collector fail: " + config.name);				
			}else{
				log.info("build thread collector success: " + config.name);
				lc.process();
			}
		} catch (ExecutionException | TimeoutException | IOException e) {
			e.printStackTrace();
		} finally {
			countDownLatch.countDown();
		}
	}
	
	public void stop(){
		try {
			future.cancel(true);
			log.info("send interrupte to thread:" + config.name + ", waiting for stop");
			this.countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
