package com.ziemniakoss.distributed.client.repositories;

import com.ziemniakoss.distributed.client.models.File;
import com.ziemniakoss.distributed.client.models.Server;

import java.util.List;

public interface IServerFilesRepository {
	List<Server> getAllWith(File f);

	void addToServer(File file, Server server) throws FileDoesNotExistsException, ServerDoesNotExistsException;

	void removeFromServer(File file, Server server) throws FileDoesNotExistsException, ServerDoesNotExistsException;

	List<File> getAllFilesOnServer(Server s) throws ServerDoesNotExistsException;

	boolean exists(Server s);
}
