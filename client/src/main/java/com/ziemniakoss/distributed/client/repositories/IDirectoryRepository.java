package com.ziemniakoss.distributed.client.repositories;

import com.ziemniakoss.distributed.client.models.Directory;

import java.util.List;
import java.util.Optional;

public interface IDirectoryRepository {
	Optional<Directory> get(int id);

	void add(Directory directory) throws DirectoryDoesNotExistsException;

	void remove(Directory directory) throws DirectoryDoesNotExistsException;

	Optional<List<Directory>> getAllSubdirectories(Directory directory);

	Optional<Directory> getParent(Directory d) throws DirectoryDoesNotExistsException;

	Optional<List<Directory>> getPathTo(Directory d);

	boolean exists(int id);

	boolean exists(Directory d);
}
