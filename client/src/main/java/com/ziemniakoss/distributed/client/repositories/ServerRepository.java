package com.ziemniakoss.distributed.client.repositories;

import com.ziemniakoss.distributed.client.models.Server;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class ServerRepository implements IServerRepository{
	private final String BASE_QUERY = "SELECT * FROM servers ";
	private final JdbcTemplate jdbcTemplate;

	public ServerRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Optional<Server> get(int id) {
		try{
			return Optional.ofNullable(jdbcTemplate.queryForObject(BASE_QUERY + " WHERE id = ?", (rs, rn)->map(rs), id));
		}catch (EmptyResultDataAccessException e){
			return Optional.empty();
		}
	}

	@Override
	public List<Server> getAll() {
		return jdbcTemplate.query(BASE_QUERY, (rs,rn)->map(rs));
	}

	private Server map(ResultSet rs) throws SQLException {
		Server server = new Server();
		server.setId(rs.getInt("id"));
		server.setName(rs.getString("name"));
		server.setUrl(rs.getString("url"));
		return server;
	}
}
