package com.ziemniakoss.distributed.client.services;

import com.ziemniakoss.distributed.client.models.Directory;
import com.ziemniakoss.distributed.client.models.File;
import com.ziemniakoss.distributed.client.repositories.DirectoryDoesNotExistsException;
import com.ziemniakoss.distributed.client.repositories.IDirectoryRepository;
import com.ziemniakoss.distributed.client.repositories.IFileRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FilesManager {
	private final IDirectoryRepository directoryRepository;
	private final IFileRepository fileRepository;

	public FilesManager(IDirectoryRepository directoryRepository, IFileRepository fileRepository) {
		this.directoryRepository = directoryRepository;
		this.fileRepository = fileRepository;
	}

	public List<File> getAllInDirectory(Integer directoryId) throws DirectoryDoesNotExistsException {
		Directory d = null;
		if(directoryId != null){
			d = directoryRepository.get(directoryId).orElseThrow(DirectoryDoesNotExistsException::new);
		}
		return fileRepository.getAllInDirectory(d);
	}
}
