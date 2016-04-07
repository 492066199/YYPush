package com.sailing.kafka;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * must be thread safe
 * @author yangyang21
 *
 */
public class KafkaSet {
	private final static ConcurrentHashMap<String, KafkaClient> kafkaMap = new ConcurrentHashMap<String, KafkaClient>();
	
	public static KafkaClient getKafkaProducer(String kafkaName, Properties kafkaProducerProps){
		KafkaClient client = kafkaMap.get(kafkaName);
		if(client == null){
			client = new KafkaClient(kafkaProducerProps);
			KafkaClient clientTrue = kafkaMap.putIfAbsent(kafkaName, client);
			
			if(clientTrue != null){
				client.close();
				return clientTrue;
			}else {
				return client;				
			}
		}
		return client;
	}
}
