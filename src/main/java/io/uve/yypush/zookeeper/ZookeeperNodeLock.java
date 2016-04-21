package io.uve.yypush.zookeeper;

import io.uve.yypush.model.ChangeNode;

import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.collect.Lists;

public enum ZookeeperNodeLock {
	instance;
	private final Lock lock = new ReentrantLock(); 
	private final Condition c = lock.newCondition();
	public final List<ChangeNode> changeStatus = Lists.newArrayList();
	public boolean needReload = false;
	
	public void lock(){
		lock.lock();
	}
	
	public void unlock(){
		lock.unlock();
	}
	
	public void signalAll(){
		c.signalAll();
	}

	public void addchange(ChangeNode node) {
		changeStatus.add(node);
		this.needReload = true;
	}

	public void await() throws InterruptedException {
		c.await();
	}	
}
