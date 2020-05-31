package com.ziemniakoss.distributed.client.services;

import com.ziemniakoss.distributed.client.models.Directory;
import com.ziemniakoss.distributed.client.repositories.DirectoryDoesNotExistsException;
import com.ziemniakoss.distributed.client.repositories.IDirectoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class DirectoriesManager {
	private final IDirectoryRepository directoryRepository;
	private final Pattern namePattern = Pattern.compile("[a-zA-Z0-9_\\- ]+");

	public DirectoriesManager(IDirectoryRepository directoryRepository) {
		this.directoryRepository = directoryRepository;
	}

	public Directory getDirectory(int id) throws DirectoryDoesNotExistsException {
		return directoryRepository.get(id).orElseThrow(DirectoryDoesNotExistsException::new);
	}

	public void addDirectory(String name, Integer parentId) throws DirectoryDoesNotExistsException {
		Directory d = new Directory();
		d.setName(name);
		if (parentId != null) {
			Directory parent = new Directory();
			parent.setId(parentId);
			d.setParent(parent);
		}
		directoryRepository.add(d);
	}

	public List<Directory> getRootSubdirectories() {
		return directoryRepository.getAllSubdirectories(null).get();//nie rzuci bo to root
	}

	public List<Directory> getAllSubdirectories(int parentId) throws DirectoryDoesNotExistsException {
		Directory parent = directoryRepository.get(parentId).orElseThrow(() -> new DirectoryDoesNotExistsException());
		return directoryRepository.getAllSubdirectories(parent).get();//nie rzuci bo sie juz upewnilismy ze istnieje
	}

	public List<Directory> getPathToDirectory(int dirId) throws DirectoryDoesNotExistsException {
		Optional<Directory> optDir = directoryRepository.get(dirId);
		if(optDir.isEmpty()){
			throw new DirectoryDoesNotExistsException();
		}
		return directoryRepository.getPathTo(optDir.get()).get();
	}

	public List<Directory> getPathToDirectory(Directory dir) throws DirectoryDoesNotExistsException {
		if (dir == null) {
			return null;
		}
		return getPathToDirectory(dir.getId());
	}

}
