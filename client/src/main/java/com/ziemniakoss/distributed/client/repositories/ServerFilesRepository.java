package com.ziemniakoss.distributed.client.repositories;

import com.ziemniakoss.distributed.client.models.File;
import com.ziemniakoss.distributed.client.models.Server;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;

@Repository
public class ServerFilesRepository implements IServerFilesRepository {
	private final JdbcTemplate jdbcTemplate;
	private final IFileRepository fileRepository;

	public ServerFilesRepository(JdbcTemplate jdbcTemplate, IFileRepository fileRepository) {
		this.jdbcTemplate = jdbcTemplate;
		this.fileRepository = fileRepository;
	}

	@Override
	public List<Server> getAllWith(File f) {
		return jdbcTemplate.query("SELECT s.id as id, s.name as name, s.url as url " +
						" FROM servers_files sf " +
						" JOIN servers s ON sf.server_id = s.id " +
						" WHERE file_id = ?",
				(rs, rn) -> {
					Server s = new Server();
					s.setId(rs.getInt("id"));
					s.setName(rs.getString("name"));
					s.setUrl(rs.getString("url"));
					return s;
				}, f.getId());
	}

	@Override
	public void addToServer(File file, Server server) throws FileDoesNotExistsException, ServerDoesNotExistsException {
		int result = jdbcTemplate.queryForObject("SELECT * FROM add_to_server(?,?)",
				Integer.class, file.getId(), server.getId());
		switch (result) {
			case -1:
				throw new FileDoesNotExistsException();
			case -2:
				throw new ServerDoesNotExistsException();
		}
	}

	@Override
	public void removeFromServer(File file, Server server) throws FileDoesNotExistsException, ServerDoesNotExistsException {
		int result = jdbcTemplate.queryForObject("SELECT * FROM remove_from_server(?,?)",
				Integer.class, file.getId(), server.getId());
		switch (result) {
			case -1:
				throw new FileDoesNotExistsException();
			case -2:
				throw new ServerDoesNotExistsException();

		}
	}

	@Override
	public List<File> getAllFilesOnServer(Server s) throws ServerDoesNotExistsException {
		if (!exists(s)) {
			throw new ServerDoesNotExistsException();
		}
		//todo bez użycia filerepo
		return jdbcTemplate.query("SELECT file_id as file_id FROM servers_files WHERE server_id = ?",
				(rs, rn) -> fileRepository.get(rs.getInt("file_id")).get(), s.getId());
	}

	@Override
	public boolean exists(Server s) {
		Assert.notNull(s, "Server can't be null");
		return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT id " +
				"FROM servers where id = ?)", Boolean.class, s.getId());
	}
}
