package com.example.thoughtclan.conversion.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.thoughtclan.conversion.service.ExtractionService;

@RestController
public class ExtractionController {
	
	@Autowired
	private ExtractionService extractionService;
	
	@GetMapping("/xmlparsing")
	public ResponseEntity<?> home(@RequestParam MultipartFile file) throws IOException {
		return ResponseEntity.ok(extractionService.parcingTheXmlFileForSCA(file.getBytes(), file.getOriginalFilename()));
	}

}


