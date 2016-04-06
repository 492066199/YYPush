package com.sailing.model;

import java.util.List;
import java.util.Map;

public class ChangeNode {
	
	private boolean useMap;
	private Map<String, String> map;
	private List<String> childs;
	
	public boolean isUseMap() {
		return useMap;
	}
	public void setUseMap(boolean useMap) {
		this.useMap = useMap;
	}
	public List<String> getChilds() {
		return childs;
	}
	public void setChilds(List<String> childs) {
		this.childs = childs;
	}
	public Map<String, String> getMap() {
		return map;
	}
	public void setMap(Map<String, String> map) {
		this.map = map;
	}
}
