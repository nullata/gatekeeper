package com.gatekeeper.repos;

import com.gatekeeper.dtos.Gatekey;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 *
 * @author null
 */
@Repository
public class GateKeyRepository {

    private final JdbcTemplate jdbcTemplate;

    public GateKeyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Gatekey findByKey(String table, String column, String key) throws DataAccessException {
        RowMapper<Gatekey> gatekeyRowMapper = (rs, rowNum) -> new Gatekey(rs.getString(column));
        String query = "SELECT " + column + " FROM " + table + " WHERE " + column + " = ?";
        return jdbcTemplate.queryForObject(query, gatekeyRowMapper, key);
    }
}
