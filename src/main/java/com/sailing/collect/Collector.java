package com.sailing.collect;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.joda.time.DateTime;

import com.sailing.Sailing;
import com.sailing.config.Config;
import com.sailing.kafka.KafkaClient;
import com.sailing.kafka.KafkaSet;
import com.sailing.model.FileNode;
import com.sailing.zookeeper.ZkMonitorPath;

public abstract class Collector {
	public Config config;
	public KafkaClient producer;	
	public final int prefixlength = Sailing.acceptIp.get().length() + 1;
	public final byte[] prefix = (Sailing.acceptIp.get() + "|").getBytes();
	private int count = 0;
	
	public void handle(FileNode node ) {		
		ByteBuffer bf = node.getBf();
		bf.flip();
		int limit = bf.limit();
		int index = 0;
		for(int i = 0; i < limit; i++){
			byte c = bf.get(i);
			if(c == config.delimiter){
				int length = (i + 1) - index;
				if(length != 0){
					byte[] dst = new byte[length - 1 + this.prefixlength];
					System.arraycopy(prefix, 0, dst, 0, this.prefixlength);
					bf.get(dst, this.prefixlength, length - 1);
					bf.get();
					producer.send(config.feed, dst);
					dst = null;
				}
				index = i + 1;
			}
		}
		
		int finallength = limit - index;
		byte[] fdst = new byte[finallength];
		bf.get(fdst, 0, finallength);
		bf.clear();
		bf.put(fdst);
		node.setOffset(node.getOffset() + limit - node.getLastFinalLength());
		node.setLastFinalLength(finallength);
		if(count == 20){
			ZkMonitorPath.instance.heart(config.name);
			count = 0;
		}
		count ++;
	}
	
	public abstract boolean load(DateTime dateTime, boolean first) throws IOException;
	
	public abstract void process()  throws IOException, ExecutionException, TimeoutException ;
	
	//must be thread safe
	public static Collector build(Config config) throws IOException {
		if(config.fileType == null || config.kafkaName == null){
			return null;
		}
		Collector lc = config.fileType.getNewCollector();
		lc.config = config;			
		lc.producer = KafkaSet.getKafkaProducer(config.kafkaName, config.kafkaProducerProps);
		if(lc.load(DateTime.parse(config.startTime), true)){
			return lc;
		}
		return null;
	}
}
