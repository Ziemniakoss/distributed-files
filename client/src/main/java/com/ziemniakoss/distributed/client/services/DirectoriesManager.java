package com.ziemniakoss.distributed.client.services;

import com.ziemniakoss.distributed.client.models.Directory;
import com.ziemniakoss.distributed.client.repositories.DirectoryDoesNotExistsException;
import com.ziemniakoss.distributed.client.repositories.IDirectoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.file.DirectoryIteratorException;
import java.util.List;
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
		if(parentId != null){
			Directory parent = new Directory();
			parent.setId(parentId);
			d.setParent(parent);
		}
		directoryRepository.add(d);
	}

	public List<Directory> getRootSubdirectories(){
		return directoryRepository.getAllSubdirectories(null).get();//nie rzuci bo to root
	}

	public List<Directory> getAllSubdirectories(int parentId) throws DirectoryDoesNotExistsException {
		Directory parent = directoryRepository.get(parentId).orElseThrow(()->new DirectoryDoesNotExistsException());
		return directoryRepository.getAllSubdirectories(parent).get();//nie rzuci bo sie juz upewnilismy ze istnieje
	}
}
