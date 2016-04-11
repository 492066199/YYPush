package com.sailing.collect;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.joda.time.DateTime;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.sailing.Sailing;
import com.sailing.config.Config;
import com.sailing.kafka.KafkaClient;
import com.sailing.kafka.KafkaSet;
import com.sailing.model.FileNode;

public abstract class Collector {
	public Config config;
	public KafkaClient producer;	
	public final int prefixlength = Sailing.acceptIp.get().length() + 1;
	public final byte[] prefix = (Sailing.acceptIp.get() + "|").getBytes();
	
	public void handle(FileNode node ) {		
		ByteBuffer bf = node.getBf();
		bf.flip();
		int limit = bf.limit();
		int count = 0;
		int index = 0;
		for(int i = 0; i < limit; i++){
			byte c = bf.get(i);
			if(c == '\n'){
				int length = (i + 1) - index;
				if(length != 0){
					byte[] dst = new byte[length - 1 + this.prefixlength];
					System.arraycopy(prefix, 0, dst, 0, this.prefixlength);
					bf.get(dst, this.prefixlength, length - 1);
					bf.get();
					producer.send(config.feed, dst);
					count = count + 1;
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
		sendStatusInfo(count);
	}
	
	public abstract void load(DateTime dateTime) throws IOException;
	
	public abstract void process()  throws IOException, ExecutionException, TimeoutException ;
	
	private void sendStatusInfo(int count) {
		Cat.logEvent("logsEvent", "", Event.SUCCESS, this.config.name);
	}
	
	//must be thread safe
	public static Collector build(Config config) throws IOException {
		if(config.fileType == null || config.kafkaName == null){
			return null;
		}
		Collector lc = config.fileType.getNewCollector();
		lc.config = config;			
		lc.producer = KafkaSet.getKafkaProducer(config.kafkaName, config.kafkaProducerProps);
		lc.load(DateTime.parse(config.startTime));
		return lc;
	}
}
