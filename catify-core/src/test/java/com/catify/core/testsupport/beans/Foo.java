package com.catify.core.testsupport.beans;

import java.util.HashMap;
import java.util.Map;

public class Foo {

	public Foo(){}
	public Foo(String foo, int bar){
		this.foo = foo;
		this.bar = bar;
	}
	
	String foo;
	int bar;
	Map<String,Object> map = new HashMap<String, Object>();
	
	public String getFoo() {
		return foo;
	}
	
	public void setFoo(String foo) {
		this.foo = foo;
	}
	
	public int getBar() {
		return bar;
	}
	
	public void setBar(int bar) {
		this.bar = bar;
	}
	
    public Map<String, Object> getMap() {
        return map;
    }
    
    public void setMap(Map<String, Object> map) {
        this.map = map;
    }
    
    public void putMap(String key, Object value){
        this.map.put(key, value);
    }
}
