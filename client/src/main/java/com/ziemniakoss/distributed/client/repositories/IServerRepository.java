package com.ziemniakoss.distributed.client.repositories;

import com.ziemniakoss.distributed.client.models.Server;

import java.util.List;
import java.util.Optional;

public interface IServerRepository {
	Optional<Server> get(int id);

	List<Server> getAll();
}
