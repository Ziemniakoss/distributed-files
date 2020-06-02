package com.ziemniakoss.distributed.server;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.Optional;

@Repository
public class FileRepository implements IFileRepository {
	private final JdbcTemplate jdbcTemplate;
	private final Logger log = LoggerFactory.getLogger(FileRepository.class);

	public FileRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public boolean existsAndRegistered(int id) {
		return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT f.id as id, f.name as name " +
				" FROM servers_files sf " +
				" JOIN files f ON sf.file_id = f.id " +
				" WHERE sf.file_id = ? AND sf.server_id = ?)", Boolean.class, id, ServerApplication.getServerId());
	}

	@Override
	public Optional<File> get(int id) {
		try {
			return Optional.ofNullable(jdbcTemplate.queryForObject(
					"SELECT f.id as id, f.name as name " +
							" FROM servers_files sf " +
							" JOIN files f ON sf.file_id = f.id " +
							" WHERE sf.server_id = ? AND sf.file_id = ?",
					(rs, rn) -> {
						File f = new File();
						f.setId(rs.getInt("id"));
						f.setName(rs.getString("name"));
						return f;
					}, ServerApplication.getServerId(), id));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	@Override
	public boolean exists(int id) {
		return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT id FROM files WHERE id = ?)", Boolean.class, id);
	}


	@Override
	public void unregister(File f) {
		Assert.notNull(f, "File can't be null");
		if (jdbcTemplate.update("DELETE FROM servers_files WHERE server_id = ? AND file_id = ?",
				ServerApplication.getServerId(), f.getId()) > 0) {
			log.info("File{} unregistered from server", f.getId());
		}
		log.warn("Tried to unregister file with id={} but file was not registered on this server", f.getId());
	}

	@Override
	public void register(int fileId) throws FileDoesNotExist {
		int result = jdbcTemplate.queryForObject("SELECT * FROM add_to_server(?, ?)",
				Integer.class, fileId, ServerApplication.getServerId());
		switch (result) {
			case -1:
				log.error("Tried to register file that does not exists");
				throw new FileDoesNotExist(fileId);
			case -2:
				log.error("This server does not exist in database!");
				throw new IllegalArgumentException("This server does not exists. Check database and serverId");
		}
	}
}
