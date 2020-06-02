package com.ziemniakoss.distributed.client.controllers;

import com.ziemniakoss.distributed.client.models.Directory;
import com.ziemniakoss.distributed.client.repositories.IDirectoryRepository;
import com.ziemniakoss.distributed.client.repositories.IServerRepository;
import com.ziemniakoss.distributed.client.services.FilesManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
public class FilesController {
	private final IServerRepository serverRepository;
	private final IDirectoryRepository directoryRepository;
	private final FilesManager filesManager;

	public FilesController(IServerRepository serverRepository, IDirectoryRepository directoryRepository, FilesManager filesManager) {
		this.serverRepository = serverRepository;
		this.directoryRepository = directoryRepository;
		this.filesManager = filesManager;
	}

	@GetMapping("/add-file")
	public String showAddingFilesPanel(Model model) {
		model.addAttribute("servers", serverRepository.getAll());
		return "add-file";
	}

	@GetMapping("/{dirId}/add-file")
	public String showAddingFilesPanel(@PathVariable int dirId, Model model) {
		Optional<Directory> dir = directoryRepository.get(dirId);
		if (dir.isEmpty()) {
			return "error_404";
		}
		model.addAttribute("parentDir", dir.get());
		model.addAttribute("servers", serverRepository.getAll());
		return "add-file";
	}

	@PostMapping("/add-file")
	public String addFile(@RequestParam("file") MultipartFile file, Model model, @RequestParam(name = "serverId", required = false) List<Integer> serversIds) {
		if (serversIds == null || serversIds.isEmpty()) {
			model.addAttribute("errorMessages", Arrays.asList("Wybierz pryznajmniej jeden serwer"));
			return "add-file";
		}
		try {
			filesManager.add(file, serversIds, null);
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("errorMessages", Arrays.asList(e.getMessage()));
			return "add-file";
		}
		return "redirect:/";
	}
}
