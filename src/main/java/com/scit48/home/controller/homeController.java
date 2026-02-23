package com.scit48.home.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class homeController {
	
	@GetMapping({"","/"})
	public String home(){
		return "home/home";
	}
	
	
	@GetMapping("/service")
	public String servicePage() {
		return "service"; // templates/service.html
	}
	
	@GetMapping("/guide")
	public String guidePage() {
		return "guide";
		// templates/guide/guide.html
	}
	
	@GetMapping("/usage-guide")
	public String userGuidePage() {
		return "usage-guide";
	}
}
