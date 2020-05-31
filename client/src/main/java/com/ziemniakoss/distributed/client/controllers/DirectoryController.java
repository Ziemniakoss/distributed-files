package com.ziemniakoss.distributed.client.controllers;

import com.ziemniakoss.distributed.client.models.Directory;
import com.ziemniakoss.distributed.client.repositories.DirectoryDoesNotExistsException;
import com.ziemniakoss.distributed.client.services.DirectoriesManager;
import com.ziemniakoss.distributed.client.services.FilesManager;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Controller
public class DirectoryController {
	private final DirectoriesManager directoriesManager;
	private final FilesManager filesManager;

	public DirectoryController(DirectoriesManager directoriesManager, FilesManager filesManager) {
		this.directoriesManager = directoriesManager;
		this.filesManager = filesManager;
	}

	@GetMapping("/")
	public String showRoot(Model model) {
		model.addAttribute("subdirectories", directoriesManager.getRootSubdirectories());
		try {
			model.addAttribute("files", filesManager.getAllInDirectory(null));
		} catch (DirectoryDoesNotExistsException ignored) {
			//nie zostanie rzucone bo folder jest nullem
		}
		return "directory_view";
	}

	@GetMapping("/{directoryId}")
	public String showDirectoryInfo(@PathVariable int directoryId, Model model) {
		try {
			model.addAttribute("directory", directoriesManager.getDirectory(directoryId));
			model.addAttribute("subdirectories", directoriesManager.getAllSubdirectories(directoryId));
			model.addAttribute("files", filesManager.getAllInDirectory(directoryId));
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
	public String addNewDirectory(@Valid @ModelAttribute Directory newDir, Model model, Errors errors) {
		if(errors.hasErrors()){
			model.addAttribute("errorMessages", errors.getAllErrors().stream().
					map(DefaultMessageSourceResolvable::getDefaultMessage).
					collect(Collectors.toList()));
			return "add-directory";		}
		try{
			directoriesManager.addDirectory(newDir.getName(),null);
			System.out.println("added new folder "+newDir.getName());
			return "redirect:/";
		} catch (Exception e) {
			model.addAttribute("errorMessages", Arrays.asList(e.getMessage()));
			System.err.println(e);
			return "add-directory";
		}
	}

	@PostMapping("/{parentId}/add-directory")
	public String addNewDirectory(@PathVariable int parentId,@Valid @ModelAttribute Directory newDir, Model model, Errors errors) {
		if(errors.hasErrors()){
			model.addAttribute("errorMessages", errors.getAllErrors().stream().
					map(e ->e.getDefaultMessage()).
					collect(Collectors.toList()));
			return "add-directory";
		}
		try{
			directoriesManager.addDirectory(newDir.getName(), parentId);
			System.out.println("Dodano nowy foler " + newDir.getName());
			return "redirect:/"+parentId;
		}catch (Exception e){
			model.addAttribute("errorMessages", Arrays.asList(e.getMessage()));
			return "add-directory";
		}
	}

	@GetMapping("/add-directory")
	public String showNewDirectoryForm(Model model) {
		model.addAttribute("newDirectory", new Directory());
		model.addAttribute("fullPath", "~/");
		model.addAttribute("shortPath", "~/");
		model.addAttribute("parentIsRoot", true);
		return "add-directory";
	}

	@GetMapping("/{parentId}/add-directory")
	public String showNewDirectoryForm(@PathVariable int parentId, Model model) {
		try {
			directoriesManager.getDirectory(parentId);
			List<Directory> pathToDirectory = directoriesManager.getPathToDirectory(parentId);
			String pathAsString = "~/" + pathToDirectory.stream().map(Directory::getName).collect(Collectors.joining("/")) + "/";
			model.addAttribute("fullPath", pathAsString);
			model.addAttribute("shortPath", ".../" + pathToDirectory.get(pathToDirectory.size() - 1).getName() + "/");
			model.addAttribute("parentIsRoot", false);
		} catch (DirectoryDoesNotExistsException e) {
			return "error_404";
		}
		model.addAttribute("newDirectory", new Directory());
		return "add-directory";
	}
}
