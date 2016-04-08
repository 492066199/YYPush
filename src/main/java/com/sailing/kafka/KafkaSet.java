package com.sailing.kafka;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

/**
 * must be thread safe
 * @author yangyang21
 *
 */
public class KafkaSet {
	private final static Logger log = Logger.getLogger(KafkaSet.class);
	private final static ConcurrentHashMap<String, KafkaClient> kafkaMap = new ConcurrentHashMap<String, KafkaClient>();
	private final static ReentrantLock lock = new ReentrantLock(); 
	public static KafkaClient getKafkaProducer(String kafkaName, Properties kafkaProducerProps){
		KafkaClient client = kafkaMap.get(kafkaName);
		if(client == null){
			try {
				lock.lock();
				if(client == null){
					client = new KafkaClient(kafkaProducerProps);
					kafkaMap.put(kafkaName, client);
					return client;
				}
			} catch (Exception e) {
				log.error("create producer error!");
				return null;
			} finally {
				lock.unlock();
			}
		}
		return client;
	}
}
