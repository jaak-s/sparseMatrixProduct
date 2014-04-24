package org.fastsparse.util;

import java.util.Date;

public class Timer {
	static long t1;
	static long t2;
	
	public static void tic() {
		t1 = new Date().getTime();
	}
	
	public static long toc() {
		t2 = new Date().getTime();
		System.out.println("It took " + (t2 - t1) + " ms.");
		return t2 - t1;
	}
}
