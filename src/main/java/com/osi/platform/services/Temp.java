package com.osi.platform.services;

public class Temp {

	public static void main(String[] args) {
		String tenderId = "${tenderId} ---------abcdefgh ${tenderId}";
		int tenderValue = 1642;
		System.out.println(tenderId.replaceAll("${tenderId}", String.valueOf(tenderValue)));
		tenderId = tenderId.replace("${tenderId}", String.valueOf(tenderValue));
		System.out.println(tenderId);
		
	}

}
