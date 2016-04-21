package io.uve.yypush.zookeeper;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * @author yangyang21@staff.weibo.com(yangyang)
 * 
 */
public enum ZkMonitorPath {
	instance;
	public final Lock lock = new ReentrantLock(); 
	public final Condition c = lock.newCondition();
	
	public boolean register(String name){
		try {
			lock.lock();
			ZkFactory.getZkConfig().register(name);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			lock.unlock();
		}	
		return false;
	}
	
	public boolean cancel(String name){
		try {
			lock.lock();
			ZkFactory.getZkConfig().cancel(name);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			lock.unlock();
		}
		return false;
	}
	
	public void heart(String name){
		ZkFactory.getZkConfig().heart(name);
	}
}
