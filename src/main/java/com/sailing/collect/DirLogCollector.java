package com.sailing.collect;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.sailing.collect.FileType.FindJavaVisitor;
import com.sailing.model.FileNode;

public class DirLogCollector extends Collector{
	private static Logger log = Logger.getLogger(DirLogCollector.class);
	private final Map<AsynchronousFileChannel, FileNode> map = new HashMap<AsynchronousFileChannel, FileNode>();
	private final long hour = 3600 * 1000;

	@Override
	public void load(DateTime dateTime) throws IOException {
		this.map.clear();
	 	int hour = dateTime.getHourOfDay();
	 	String date = dateTime.toString("yyyy-MM-dd");
	 	Path startingDir = Paths.get(config.basePath + "/" + date + "/" + String.format("%02d", hour));
	 	List<Path> result = new LinkedList<Path>();
	 	Files.walkFileTree(startingDir, new FindJavaVisitor(result, config.suffix));
		for (Path p : result) {
			AsynchronousFileChannel channel = AsynchronousFileChannel.open(p, StandardOpenOption.READ);
			FileNode node = new FileNode();
			node.setBf(ByteBuffer.allocate(100000));
			node.getBf().clear();
			node.setCnt(null);
			node.setOffset(0);
			node.setCurTime(dateTime.getMillis());
			this.map.put(channel, node);
			log.info("load file successs:" + p.toAbsolutePath());
		}
		log.info("init successs!");
	}
	
	@Override
	public void process() throws IOException, ExecutionException, TimeoutException {
		while(true){
			int count = 0;
			while (true) {
				if(Thread.interrupted()){
					log.info("thread " + this.config.name + "has stop by main interupt");
					return;
				}
				
				for (Entry<AsynchronousFileChannel, FileNode> entry : this.map.entrySet()) {
					Future<Integer> f = entry.getKey().read(entry.getValue().getBf(), entry.getValue().getOffset());
					entry.getValue().setCnt(f);
				}
	
				for (FileNode node : this.map.values()) {
					try {
						Integer cnt = 0;
						cnt = node.getCnt().get();					
						if (cnt > 0) {
							handle(node);
						}else if(!node.isHasReadEOF()){
							if(config.fileType.check(node.getCurTime(), config)){
								count = count + 1;
								node.setHasReadEOF(true);
							}
						}
					} catch (InterruptedException e) {
						log.info("recv interrunpt");
						Thread.currentThread().interrupt();
					}
				}
				
				if(count == 10){
					for(AsynchronousFileChannel channel : this.map.keySet()){
						 channel.close();
					}
					break;
				}
			}
			
			if(!loadNext()){
				log.info("load next dir error!");
				break;
			}
		}
		log.info("exit the thread!");
	}

	private boolean loadNext() {
		long curTime = 0; 
		for(FileNode node : map.values()){
			 curTime = node.getCurTime();
			 break;
		}
		curTime = curTime + hour;
		try {
			load(new DateTime(curTime));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}