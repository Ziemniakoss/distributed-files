package com.ziemniakoss.distributed.client.repositories;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ziemniakoss.distributed.client.models.Directory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class DirectoryRepository implements IDirectoryRepository {
	private final JdbcTemplate jdbcTemplate;
	private final String BASE_QUERY =
			"SELECT d.id as id, d.name as name, dd.id as p_id, dd.name as p_name " +
					" FROM directories d " +
					" LEFT JOIN directories dd on d.parent = dd.id ";

	public DirectoryRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Optional<Directory> get(int id) {
		try {
			return Optional.ofNullable(jdbcTemplate.queryForObject(BASE_QUERY + " WHERE d.id = ?",
					new Object[]{id}, (rs, rn) -> map(rs)));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	@Override
	public void add(Directory directory) throws DirectoryDoesNotExistsException {
		Assert.notNull(directory, "Directory can't be null");
		int result;
		if (directory.getParent() == null) {
			result = jdbcTemplate.queryForObject("SELECT * FROM create_directory(?, null)",
					new Object[]{directory.getName()}, Integer.class);
		} else {
			result = jdbcTemplate.queryForObject("SELECT * FROM create_directory(?, ?)",
					new Object[]{directory.getName(), directory.getParent().getId()}, Integer.class);
		}
		switch (result) {
			case -1:
				throw new IllegalArgumentException("Name can't be empty");
			case -2:
				throw new DirectoryDoesNotExistsException();
			case -3:
				throw new IllegalArgumentException("Directory with same name in this directory already exists");
			case -4:
				throw new IllegalArgumentException("Directory name must can only have letters, numbers, spaces and _ - in name");
		}
	}

	@Override
	public void remove(Directory directory) throws DirectoryDoesNotExistsException {
		if (jdbcTemplate.update("DELETE FROM directories WHERE d.id = ?", directory.getId()) == 0) {
			throw new DirectoryDoesNotExistsException();
		}
	}

	@Override
	public Optional<List<Directory>> getAllSubdirectories(Directory directory) {
		if (directory == null) {
			return Optional.of(jdbcTemplate.query(BASE_QUERY + " WHERE d.parent is null ", (rs, rn) -> map(rs)));
		}
		if (!exists(directory)) {
			return Optional.empty();
		}
		return Optional.of(jdbcTemplate.query(BASE_QUERY + "WHERE d.parent = ?", (rs, rn) -> map(rs), directory.getId()));
	}

	@Override
	public Optional<Directory> getParent(Directory d) throws DirectoryDoesNotExistsException {
		Optional<Directory> opt = get(d.getId());
		if (opt.isPresent()) {
			return Optional.ofNullable(opt.get().getParent());
		} else {
			throw new DirectoryDoesNotExistsException();
		}
	}

	@Override
	public Optional<List<Directory>> getPathTo(Directory d) {
		Assert.notNull(d, "Directory can't be null");
		if (!exists(d)) {
			return Optional.empty();
		}
		return Optional.of(jdbcTemplate.query("SELECT * FROM get_path_to_directory(?)",
				(rs, rn) -> map(rs), d.getId()));
	}

	private Directory map(ResultSet rs) throws SQLException {
		Directory result = new Directory();
		result.setId(rs.getInt("id"));
		result.setName(rs.getString("name"));
		int parentId = rs.getInt("p_id");
		if (!rs.wasNull()) {
			Directory parent = new Directory();
			parent.setName(rs.getString("p_name"));
			parent.setId(parentId);
			result.setParent(parent);
		}
		return result;
	}

	@Override
	public boolean exists(int id) {
		return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT id FROM directories WHERE id = ?)", Boolean.class, id);
	}

	@Override
	public boolean exists(Directory d) {
		Assert.notNull(d, "Directory can't be null");
		return exists(d.getId());
	}
}
