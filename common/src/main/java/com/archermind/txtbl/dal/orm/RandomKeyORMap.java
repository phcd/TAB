package com.archermind.txtbl.dal.orm;

import com.archermind.txtbl.domain.RandomKey;
import com.ibatis.sqlmap.client.SqlMapClient;

import java.sql.SQLException;

public class RandomKeyORMap {

	public RandomKey getRandomKey(int iKey, SqlMapClient sqlMapClient) throws SQLException {
		return (RandomKey) sqlMapClient.queryForObject("RandomKey.getRandomKey", iKey);
	}

	public RandomKey getMaxRandomKey(SqlMapClient sqlMapClient) throws SQLException {
		return (RandomKey) sqlMapClient.queryForObject("RandomKey.getMaxRandomKey");
	}
}
