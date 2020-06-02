package com.ziemniakoss.distributed.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@RestController
public class FilesController {
	private final IFileRepository fileRepository;
	private final Logger log = LoggerFactory.getLogger(FilesController.class);

	public FilesController(IFileRepository fileRepository) {
		this.fileRepository = fileRepository;
	}

	@GetMapping(value = "/file/{fileId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<?> getFile(@PathVariable int fileId, HttpServletResponse response, HttpServletRequest req) {
		var optFile = fileRepository.get(fileId);
		if (optFile.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File with this id does not exists");
		}
		var file = new FileSystemResource(Paths.get("savedFiles", String.valueOf(fileId)));
		if (!file.exists()) {
			log.error("Requested file that does not exist on this server id={}", optFile.get().getId());//todo co zrobić?
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File was registerd on this server, but was not present");
		}
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + optFile.get().getName());
		log.info("Sending file with id {} to", req.getRemoteUser());
		return ResponseEntity.status(HttpStatus.OK).
				contentType(MediaType.APPLICATION_OCTET_STREAM).
				body(file);
	}

	@PostMapping(value = "/files/{fileId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> saveFile(@PathVariable int fileId, @RequestBody MultipartFile attachement, HttpServletRequest req) {
		System.out.println(attachement.getName());
		System.out.println(attachement.getSize());
		if (!fileRepository.exists(fileId)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File with given id does not exists");
		}
		if (fileRepository.existsAndRegistered(fileId)) {
			log.error("User {} tried to register file with id {}, which is already registered on this server",
					req.getRemoteUser(), fileId);
			return ResponseEntity.status(HttpStatus.CONFLICT).body("File is already registered");
		}
		try {
			new File("savedFiles").mkdir();
			attachement.transferTo(Paths.get("savedFiles", String.valueOf(fileId)));
			log.debug("File with id {} saved, registering", fileId);
			fileRepository.register(fileId);
			log.info("File {} registered!", fileId);
			System.out.println(Paths.get("savedFiles","1").toString());
		} catch (IOException e) {
			log.error("Error occurred while saving file {}: {}", fileId, e.getMessage());
			return ResponseEntity.
					status(HttpStatus.INTERNAL_SERVER_ERROR).
					body("Failed to save file, please try again later");
		}catch (FileDoesNotExist e){
			log.error("Saved file with id that does not exists");
			try {
				Files.delete(Paths.get("savedFiles", String.valueOf(fileId)));
			} catch (IOException ioException) {
				log.error("Failed to remove file after attempt to register file with not existing id {}: {}", fileId, e.getMessage());
				ioException.printStackTrace();
			}
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).
					body("File with given id does not exists");
		}
		return ResponseEntity.of(Optional.of("Created"));
	}
}
