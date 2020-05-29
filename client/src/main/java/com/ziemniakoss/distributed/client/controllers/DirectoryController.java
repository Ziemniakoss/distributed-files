package com.ziemniakoss.distributed.client.controllers;

import com.ziemniakoss.distributed.client.repositories.DirectoryDoesNotExistsException;
import com.ziemniakoss.distributed.client.services.DirectoriesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class DirectoryController {
	private final DirectoriesManager directoriesManager;

	public DirectoryController(DirectoriesManager directoriesManager) {
		this.directoriesManager = directoriesManager;
	}

	@GetMapping("/")
	public String showRoot(Model model) {
		model.addAttribute("subdirectories", directoriesManager.getRootSubdirectories());
		return "directory_view";
	}

	@GetMapping("/{directoryId}")
	public String showDirectoryInfo(@PathVariable int directoryId, Model model) {
		try{
			model.addAttribute("directory",directoriesManager.getDirectory(directoryId));
			model.addAttribute("subdirectories", directoriesManager.getAllSubdirectories(directoryId));
			return "directory_view";
		} catch (DirectoryDoesNotExistsException e) {
			return "error_404";
		}
	}

	@PostMapping("/{directoryId}/delete")
	public String deleteDirectory(@PathVariable int directoryId) {
		return null;//todo
	}

	@PostMapping("/add-directory")
	public String addNewDirectory() {
		return null;//todo
	}

	@PostMapping("/{parentId}/add-directory")
	public String addNewDirectory(@PathVariable int parentId) {
		return null;//todo
	}

	@GetMapping("/add-directory")
	public String showNewDirectoryForm() {
		return null;//todo
	}

	@GetMapping("/{parentId}/add-directory")
	public String showNewDirectoryForm(@PathVariable int parentId) {
		return null;//todo
	}
}
