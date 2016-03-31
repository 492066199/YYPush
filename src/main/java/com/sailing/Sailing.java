package com.sailing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.sailing.config.Config;

public class Sailing {
	private static Logger log = Logger.getLogger(Sailing.class);
	public static void main(String[] args) {
		LogCollector lc = null;
		try {
			List<Config> configs = LoadingConfig();
			lc = LogCollector.build(configs.get(0));
			lc.process();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		} finally {
			if(lc != null){
				lc.destroy();
			}
		}
	}

	private static List<Config> LoadingConfig() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper jsonMapper = new ObjectMapper();
		TypeFactory typefactory = jsonMapper.getTypeFactory();
		List<Config> configs= jsonMapper.readValue(new File("config.json"), typefactory.constructCollectionLikeType(ArrayList.class, Config.class));
		log.info("finished init config");
		return configs;
	}
}
