package io.uve.yypush.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author yangyang21@staff.weibo.com(yangyang)
 * 
 */
public class JsonReader {
	private static final ObjectMapper mapper = new ObjectMapper();
	static{
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES  , false);
	}
	
	public static ObjectMapper getObjectMapper(){
		return mapper;
	}
}
