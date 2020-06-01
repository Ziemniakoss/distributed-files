package com.ziemniakoss.distributed.client.repositories;

import com.ziemniakoss.distributed.client.models.Directory;
import com.ziemniakoss.distributed.client.models.File;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class FileRepository implements IFileRepository {
	private final IDirectoryRepository directoryRepository;
	private final JdbcTemplate jdbcTemplate;

	public FileRepository(IDirectoryRepository directoryRepository, JdbcTemplate jdbcTemplate) {
		this.directoryRepository = directoryRepository;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public List<File> getAllInDirectory(Directory directory) throws DirectoryDoesNotExistsException {
		if (directory != null && !directoryRepository.exists(directory)) {
			throw new DirectoryDoesNotExistsException();
		}
		List<File> queryResult;
		if (directory == null) {
			String sql =
					"SELECT f.id as id, f.name as name, f.creation_date as creation_date,f.hash as hash, f.size as size," +
							"d.id as d_id, d.name as d_name" +
							" FROM files f " +
							" LEFT JOIN directories d ON f.directory = d.id" +
							" WHERE f.directory IS NULL";
			queryResult = jdbcTemplate.query(sql, (rs, rn) -> map(rs));
		} else {
			String sql = "SELECT f.id as id, f.name as name, f.creation_date as creation_date,f.hash as hash, f.size as size, " +
					" d.id as d_id, d.name as d_name " +
					" FROM files f " +
					" LEFT JOIN directories d on f.directory = d.id" +
					" WHERE d.id = ?";
			queryResult = jdbcTemplate.query(sql, (rs, rn) -> map(rs), directory.getId());
		}
		return queryResult.parallelStream().
				peek(e -> e.setDirectory(directory)).
				collect(Collectors.toList());
	}

	private File map(ResultSet rs) throws SQLException {
		File result = new File();
		result.setId(rs.getInt("id"));
		result.setName(rs.getString("name"));
		result.setSize(rs.getInt("size"));
		result.setHash(rs.getString("hash"));
		result.setCreationDate(rs.getTimestamp("creation_date"));
		final int dirId = rs.getInt("d_id");
		if (rs.wasNull()) {
			Directory d = new Directory();
			d.setId(dirId);
			d.setName(rs.getString("d_name"));
			result.setDirectory(d);
		}
		return result;

	}

	@Override
	public int add(File file, Directory directory) throws DirectoryDoesNotExistsException {
		Assert.notNull(file, "File can't be null");
		Assert.notNull(file.getHash(), "Hash must be calculated");
		Assert.isTrue(file.getHash().length() == 32, "MD5 hash must have 32 characters");
		Assert.isTrue(file.getSize() > 0, "File size must be greater than 0");
		int result = jdbcTemplate.queryForObject("SELECT * FROM create_file(?,?,?);", Integer.class,
				file.getName(), file.getHash(), file.getSize(),
				directory == null ? null : directory.getId());
		switch (result) {
			case -1:
				throw new IllegalArgumentException("Name can't be null");
			case -2:
				throw new IllegalArgumentException("MD5 hash with illegal length");
			case -3:
				throw new IllegalArgumentException("Size must be grater than 0");
			case -4:
				throw new DirectoryDoesNotExistsException();
		}
		return result;
	}

	@Override
	public void remove(File file) throws FileDoesNotExistsException {
		Assert.notNull(file, "File can't be null");
		if (jdbcTemplate.update("DELETE FROM files WHERE id = ?", file.getId()) == 0) {
			throw new FileDoesNotExistsException();
		}
	}

	@Override
	public Optional<File> get(int fileId) {
		try {
			final String sql =
					"SELECT f.id AS id, f.name AS name, f.size AS size, f.hash AS hash, f.creation_date as creation_date, " +
							" d.id AS d_id, d.name AS d_name " +
							" FROM files f " +
							"   LEFT JOIN directories d ON f.directory = d.id " +
							" WHERE f.id = ?;";
			return Optional.ofNullable(
					jdbcTemplate.query(sql, rs -> {
						File result = new File();
						result.setId(rs.getInt("id"));
						result.setName(rs.getString("name"));
						result.setCreationDate(rs.getTimestamp("creation_date"));
						result.setHash(rs.getString("hash"));
						result.setSize(rs.getInt("size"));
						int dirId = rs.getInt("d_id");
						if (!rs.wasNull()) {
							Directory d = new Directory();
							d.setId(dirId);
							d.setName(rs.getString("d_name"));
							result.setDirectory(d);
						}
						return result;
					}, fileId));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}
}
