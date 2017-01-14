package com.gary.test;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

public class test {

	@Test
	public void test() {
		System.out.println(TimeZone.getDefault());
		Date date = new Date(); 
		System.out.println(date);
	}

}
