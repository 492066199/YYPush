package com.sailing.zookeeper;


public class ZkFactory {
	private static volatile boolean expare = false;
	private static volatile ZkConfig zkConfig = null;
	private final static String connectStr = "10.77.96.122:2181"; 
	
	public static ZkConfig getZkConfig(){		
		if(zkConfig == null){
			synchronized(ZkFactory.class){
				if(zkConfig == null){
					ZkConfig tmp = new ZkConfig(connectStr);
					if(expare){
						tmp.handleExpare();
					}
					zkConfig = tmp;
				}
			}
		}
		return zkConfig;
	}
	
	public static void expare(){
		synchronized(ZkFactory.class){
			expare = false;
			zkConfig = null;
		}
	}
}
