package com.sailing;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sailing.collect.CollectorThread;
import com.sailing.config.Config;
import com.sailing.json.JsonReader;
import com.sailing.model.ChangeNode;
import com.sailing.zookeeper.ZkConfig;
import com.sailing.zookeeper.ZkFactory;
import com.sailing.zookeeper.ZookeeperNodeLock;

public class Sailing {
	private static Logger log = Logger.getLogger(Sailing.class);
	public static final AtomicReference<String> acceptIp = new AtomicReference<String>();
	public static final Map<String, CollectorThread> threadMap = Maps.newHashMap(); 
	public static final ExecutorService threadpool = Executors.newCachedThreadPool();	
	public static final Set<String> ips = getHostIps();
	
	public static void main(String[] args) {
		Sailing sail = new Sailing();
		try {
			ZookeeperNodeLock.instance.lock();
			Map<String, Config> configs = sail.loadConfig();
			if(configs != null){
				for (Config config : configs.values()) {
					sail.loadNewThread(config);
				}
			}
			while(true){
				ZookeeperNodeLock.instance.await();
				sail.reloadConfigs();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally{
			ZookeeperNodeLock.instance.unlock();
		}		
	}
	
	private Map<String, Config> loadConfig() {
		try {
			return ZkFactory.getZkConfig().LoadingConfig();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void reloadConfigs() {
		if(ZookeeperNodeLock.instance.needReload == true){
			for(ChangeNode node : ZookeeperNodeLock.instance.changeStatus){
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
						}else {
							log.info("no data change:" + entry.getKey());
						}
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
			ZookeeperNodeLock.instance.needReload = false;
			ZookeeperNodeLock.instance.changeStatus.clear();
		}
	}
	
	private void loadNewThread(Config config){
		if(!checkip(config.ips)){
			log.info("not contains this ip");
			return;
		}
		CollectorThread cur = new CollectorThread(config);
		Future<?> future = threadpool.submit(cur);
		log.info("init thread: " + config.name);	
		cur.setFuture(future);
		threadMap.put(config.name, cur);
	}
	
	private boolean checkip(String originIps) {
		for(String ip : Splitter.on(',').split(originIps)){
			if(ips.contains(ip.trim())){
				acceptIp.compareAndSet(null, ip.trim());
				return true;
			}
		}
		return false;
	}

	public static Set<String> getHostIps(){
		Set<String> ips = Sets.newHashSet();
		try {
			Enumeration<?> e = NetworkInterface.getNetworkInterfaces();
			while(e.hasMoreElements()){
			    NetworkInterface n = (NetworkInterface) e.nextElement();
			    Enumeration<?> ee = n.getInetAddresses();
			    while (ee.hasMoreElements()){
			        InetAddress i = (InetAddress) ee.nextElement();
			        if(i instanceof Inet4Address){
			        	ips.add(i.getHostAddress());
			        }
			    }
			}
		} catch (SocketException e1) {
			ips.add("127.0.0.1");
		}
		return ips;
	}
}
