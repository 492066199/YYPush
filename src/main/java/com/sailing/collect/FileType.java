package com.sailing.collect;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.sailing.config.Config;

public enum FileType {
	ACCESS{
		
	},
	ERROR{
		
	},
	SUBREQ{
		@Override
		public boolean checkFile(long l, Config config) {
			DateTime dateTime = new DateTime(l - hour);
			String date = dateTime.toString("_yyyy-MM-dd-HH");
			Path startingDir = Paths.get(config.basePath + "/" + config.suffix + date);
			boolean exist = Files.exists(startingDir, LinkOption.NOFOLLOW_LINKS);
			if(exist){
				return true;
			}
			return false;
		}
	},
	DIR{
		public boolean checkFile(long l, Config config) {
			DateTime dateTime = new DateTime(l);
			int hour = dateTime.getHourOfDay();
		 	String date = dateTime.toString("yyyy-MM-dd");
		 	Path startingDir = Paths.get(config.basePath + "/" + date + "/" + String.format("%02d", hour));
		 	boolean exist = Files.exists(startingDir, LinkOption.NOFOLLOW_LINKS);
		 	if(exist){
			 	try {
			 		List<Path> result = new LinkedList<Path>();
					Files.walkFileTree(startingDir, new FindJavaVisitor(result, config.suffix));
					if(result.size() == 10){
						return true;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			 	log.info("try next file: not exist!");
		 	}
			return false;
		}
		
		@Override
		public Collector getNewCollector(){
			return new DirLogCollector();
		}
		
	};
	
	public final long hour = 3600 * 1000;
	
	protected static Logger log = Logger.getLogger(FileType.class);
	
	//access
	public boolean check(long curTime, Config config) throws InterruptedException {
		DateTime now = new DateTime();
		DateTime cur = new DateTime(curTime);
		if(now.getHourOfDay() == cur.getHourOfDay()){
			Thread.sleep(2000);
			return false;
		}else {
			return checkFile(curTime + hour, config);
		}
	}

	public boolean checkFile(long l, Config config) {
		DateTime dateTime = new DateTime(l - hour);
	 	String date = dateTime.toString("_yyyy-MM-dd-HH");
	 	Path startingDir = Paths.get(config.basePath + "/" + config.suffix + date + ".gz");
	 	boolean exist = Files.exists(startingDir, LinkOption.NOFOLLOW_LINKS);
	 	if(exist){
	 		return true;
	 	}
		return false;
	}
	
	public static class FindJavaVisitor extends SimpleFileVisitor<Path> {
		private String suffix; 
		private List<Path> result;		
		
		public FindJavaVisitor(List<Path> result, String suffix) {
			this.result = result;
			this.suffix = suffix;
		}
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
			if (file.toString().endsWith(suffix)) {
				result.add(file.toAbsolutePath());
			}
			return FileVisitResult.CONTINUE;
		}
	}
	
	public Collector getNewCollector(){
		return new FileLogColloector();
	}
}
