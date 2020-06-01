package com.ziemniakoss.distributed.client.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class FilesController {

	@GetMapping("/add-file")
	public String showAddingFilesPanel(Model model){
		return null;
	}

	@GetMapping("/{dirId}/add-file")
	public String showAddingFilesPanel(@PathVariable int dirId, Model model){
		return null;
	}

}
