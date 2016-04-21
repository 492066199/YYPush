package com.sailing.config;

import java.util.Properties;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sailing.collect.FileType;

public class Config {
	@JsonIgnore
	public String name;
	///data0/nginx/logs/uve_core/stats
	@JsonProperty
	public String basePath;
	
	@JsonProperty
    public String suffix;
    
    @JsonProperty
    public String startTime;
    
    @JsonProperty(defaultValue="false")
    public boolean useStartTime;
    
    @JsonProperty(defaultValue="false")
    public boolean useFileCurrent;
    
    @JsonProperty
    public String kafkaName;
    
    @JsonProperty
    public Properties kafkaProducerProps;
    
    @JsonProperty
    public String feed;
    
    @JsonProperty
    public int delimiter;
    
    @JsonProperty
    public FileType fileType;
    
    @JsonProperty
    public String ips;
    
	public boolean notsame(Config c) {
		try {			
			
			if(c == null){
				return false;
			}
			
			if (this.name.equals(c.name) && this.basePath.equals(c.basePath)
					&& this.suffix.equals(c.suffix)
					&& this.startTime.equals(c.startTime)
					&& this.kafkaName.equals(c.kafkaName)
					&& this.feed.equals(c.feed)
					&& this.delimiter == c.delimiter
					&& this.fileType.equals(c.fileType)
					&& this.useStartTime == c.useStartTime
					&& this.useFileCurrent == c.useFileCurrent
					&& this.ips.equals(c.ips)) {
				return false;
			}
			return true;
		} catch (NullPointerException e) {
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}