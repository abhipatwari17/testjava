package com.example.thoughtclan.conversion.service;

public interface ExtractionService {
	
	public Object parcingTheXmlFile(byte[] inputstream, String fileName);
	
	public Object parcingTheXmlFileForSCA(byte[] inputstream, String fileName);

}
