package com.ziemniakoss.distributed.client.repositories;

import com.ziemniakoss.distributed.client.models.Directory;
import com.ziemniakoss.distributed.client.models.File;

import java.util.List;
import java.util.Optional;

public interface IFileRepository {

	List<File> getAllInDirectory(Directory directory) throws DirectoryDoesNotExistsException;

	int add(File file, Directory directory) throws DirectoryDoesNotExistsException;

	void remove(File file) throws FileDoesNotExistsException;

	Optional<File> get(int fileId);
}
