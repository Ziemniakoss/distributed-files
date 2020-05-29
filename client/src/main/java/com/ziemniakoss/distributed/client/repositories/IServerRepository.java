package com.ziemniakoss.distributed.client.repositories;

import com.ziemniakoss.distributed.client.models.Server;

public interface IServerRepository {
	Server get(int id);
}
