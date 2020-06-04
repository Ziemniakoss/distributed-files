package com.ziemniakoss.distributed.client.controllers;

import com.ziemniakoss.distributed.client.models.Directory;
import com.ziemniakoss.distributed.client.repositories.DirectoryDoesNotExistsException;
import com.ziemniakoss.distributed.client.repositories.FileDoesNotExistsException;
import com.ziemniakoss.distributed.client.repositories.IDirectoryRepository;
import com.ziemniakoss.distributed.client.repositories.IServerRepository;
import com.ziemniakoss.distributed.client.services.FilesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
public class FilesController {
	private final Logger log = LoggerFactory.getLogger(FilesController.class);
	private final IServerRepository serverRepository;
	private final IDirectoryRepository directoryRepository;
	private final FilesManager filesManager;

	public FilesController(IServerRepository serverRepository, IDirectoryRepository directoryRepository, FilesManager filesManager) {
		this.serverRepository = serverRepository;
		this.directoryRepository = directoryRepository;
		this.filesManager = filesManager;
	}

	@GetMapping(value = {"/add-file", "/{dirId}/add-file"})
	public String showAddingFilesPanel(@PathVariable(required = false) Integer dirId,  Model model) {
		if(dirId != null){
			Optional<Directory> optDir = directoryRepository.get(dirId);
			if(optDir.isEmpty()){
				return "error_404";
			}
			model.addAttribute("parentDir", optDir.get());
		}
		model.addAttribute("servers", serverRepository.getAll());
		return "add-file";
	}

	@PostMapping(value = {"/add-file", "/{dirId}/add-file"})
	public String addFile(@RequestParam("file") MultipartFile file, Model model,
						  @RequestParam(name = "serverId", required = false) List<Integer> serversIds, @PathVariable(required = false) Integer dirId) {
		if (serversIds == null || serversIds.isEmpty()) {
			model.addAttribute("errorMessages", Collections.singletonList("Wybierz pryznajmniej jeden serwer"));
			model.addAttribute("servers", serverRepository.getAll());
			return "add-file";
		}
		System.out.println(dirId +" to id folderu");
		try {
			filesManager.add(file, serversIds, dirId);
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("errorMessages", Collections.singletonList(e.getMessage()));
			return "add-file";
		}
		return "redirect:/";
	}


	@GetMapping("file/{fileId}")
	public String showFileDetails(@PathVariable int fileId, Model model){
		try{
			model.addAttribute("file", filesManager.getFile(fileId));
			model.addAttribute("servers", filesManager.getAllWithFile(fileId));
		} catch (FileDoesNotExistsException e) {
			log.warn("Requested file that does not exists");
			return "error_404";
		}
		return "file";
	}
}
