package com.catify.core.rest;

import java.util.ArrayList;
import java.util.List;

public class MyBuilder {

	List<String> cache = new ArrayList<String>();
	String before;
	
	public MyBuilder add(String name){
		cache.add(name);
		System.out.println(before);
		before = name;
		return this;
	}
	
}
