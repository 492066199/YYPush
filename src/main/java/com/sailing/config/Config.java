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

	public boolean notsame(Config c) {
		if(c == null){
			return false;
		}
		if (this.name.equals(c.name) && this.basePath.equals(c.basePath)
				&& this.suffix.equals(c.suffix)
				&& this.startTime.equals(c.startTime)
				&& this.feed.equals(c.feed)
				&& this.delimiter == c.delimiter
				&& this.fileType.equals(c.fileType)) {
			return false;
		}
		return true;
	}
}