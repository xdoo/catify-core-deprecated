package com.catify.core;

import org.apache.camel.spring.Main;

public class Start {

	/**
	 * starts catify based on the spring context.
	 * 
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		Main main = new Main();
		
		main.enableHangupSupport();
		main.run();

	}

}
