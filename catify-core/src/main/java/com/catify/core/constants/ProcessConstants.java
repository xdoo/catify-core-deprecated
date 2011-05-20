package com.catify.core.constants;

public class ProcessConstants {
	
	//node types
	public static final int START				= 1;
	public static final int REQUEST				= 5;
	public static final int RECEIVE				= 10;
	public static final int REPLY				= 15;
	public static final int FORK				= 20;
	public static final int MERGE				= 25;
	public static final int DECISION			= 30;
	public static final int LINE				= 35;
	public static final int LINEEND				= 40;
	public static final int SLEEP				= 45;
	public static final int TIMEREVENT			= 50;
	public static final int EXCEPTIONEVENT		= 55;
	public static final int END					= 100;
	
	//state
	public static final int STATE_READY			= 1;
	public static final int STATE_WORKING		= 2;
	public static final int STATE_WAITING		= 3;
	public static final int STATE_DONE			= 4;
	
	//routing slip key
	public static final String ROUTINGSLIP		= "process_routing";
	public static final String RECIPIENTLIST	= "process_recipients";
	
	//message header key
	public static final String STATE 			= "catify_state";

}
