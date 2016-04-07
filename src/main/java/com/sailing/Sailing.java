package com.sailing;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sailing.config.Config;
import com.sailing.json.JsonReader;
import com.sailing.model.ChangeNode;
import com.sailing.zookeeper.ZkConfig;

public class Sailing {
	private static Logger log = Logger.getLogger(Sailing.class);
	public final Lock lock = new ReentrantLock(); 
	public final Condition c = lock.newCondition();
	public final List<ChangeNode> changeStatus = Lists.newArrayList();
	public final Map<String, Config> configs = Maps.newHashMap();
	public final Map<String, CollectorThread> threadMap = Maps.newHashMap(); 
	public ZkConfig zkConfig = null;
	public boolean needReload = false;
	public final ExecutorService threadpool = Executors.newCachedThreadPool(); 
	
	public static void main(String[] args) {
		Sailing sail = new Sailing();
		ZkConfig zkConfig = new ZkConfig(sail);
		sail.zkConfig = zkConfig;
		try {
			sail.lock.lock();
			sail.loadConfig();
			sail.process();
			while(true){
				sail.c.await();
				sail.reloadConfigs();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally{
			sail.lock.unlock();
		}		
	}
	
	private void loadConfig() {
		try {
			zkConfig.LoadingConfig();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void reloadConfigs() {
		if(this.needReload == true){
			for(ChangeNode node : changeStatus){
				if(node.isUseMap()){
					for(Entry<String, String> entry: node.getMap().entrySet()){
						CollectorThread thread = threadMap.get(entry.getKey());
						Config newConfig = null;
						
						log.info("reload config begin:" + entry.getKey() + "=>" + entry.getValue());
						try {
							newConfig = JsonReader.getObjectMapper().readValue(entry.getValue(), Config.class);
							newConfig.name = entry.getKey();
						} catch (IOException e) {
							log.error("reload config failed and skip:" + entry.getKey() + "=>" + entry.getValue());
							continue;
						}
						
						if(thread == null || thread.getConfig().notsame(newConfig)){
							if(thread != null){
								log.info("begin stop and remove thread :" + entry.getKey());
								thread.stop();
								threadMap.remove(entry.getKey());
								log.info("success stop and remove thread :" + entry.getKey());
								
							}
							loadNewThread(newConfig);
						}
						
						log.info("no data change:" + entry.getKey());
					}
				}else {
					Set<String> names = Sets.newHashSet();
					for(String path : node.getChilds()){
						String son = ZkConfig.zkBase + "/" + path;
						names.add(son);
					}
					Iterator<Entry<String, CollectorThread>> iter = threadMap.entrySet().iterator();
					while (iter.hasNext()) {
						Map.Entry<String, CollectorThread> entry = (Map.Entry<String, CollectorThread>) iter.next();
						if(!names.contains(entry.getKey())){
							entry.getValue().stop();
							iter.remove();
						}
					}
				}
			}		
			this.needReload = false;
			this.changeStatus.clear();
		}
	}

	private void process() {
		for (Config config : configs.values()) {
			loadNewThread(config);
		}
	}
	
	private void loadNewThread(Config config){
		CollectorThread cur = new CollectorThread(config);
		Future<?> future = threadpool.submit(cur);
		log.info("init thread: " + config.name);	
		cur.setFuture(future);
		threadMap.put(config.name, cur);
	}
}
