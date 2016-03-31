package com.sailing.config;

import java.util.Properties;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Config {
	///data0/nginx/logs/uve_core/stats
	@JsonProperty
	public String basePath;
	
	@JsonProperty
    public String suffix;
    
    @JsonProperty
    public String startTime;
    
    @JsonProperty
    public int[] fileoffset;
    
    @JsonProperty
    public Properties kafkaProducerProps;
    
    @JsonProperty
    public String feed;
    
    @JsonProperty
    public String zkPath;
    
    @JsonProperty
    public String zkAddr;
}