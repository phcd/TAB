package com.archermind.txtbl.dal.orm;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.mode.SqlMapFactory;
import com.ibatis.sqlmap.client.SqlMapClient;

import java.sql.SQLException;

class BaseORMap {
    protected boolean useTransaction = true;
    protected SqlMapClient sqlMapClient;

    public BaseORMap(BaseORMap another) {
        this.useTransaction = another.useTransaction;
        this.sqlMapClient = another.sqlMapClient;
    }

    public BaseORMap(boolean useMaster) {
        if (useMaster) {
            sqlMapClient = SqlMapFactory.getMasterSqlMapClient();
        } else {
            sqlMapClient = SqlMapFactory.getSlaveSqlMapClient();
        }
    }

    public BaseORMap(boolean useMaster, boolean useTransaction) {
        this(useMaster);
        this.useTransaction = useTransaction;
    }

    public BaseORMap() {
        this(true, true);
    }

    public SqlMapClient getSqlMapClient() {
        return sqlMapClient;
    }
    
    public void startTransaction() throws SQLException {
        if (this.useTransaction) {
            this.sqlMapClient.startTransaction();
        }
    }

    public void commitTransaction() throws SQLException {
        if (this.useTransaction) {
            this.sqlMapClient.commitTransaction();
        }
    }

    public void endTransaction() throws SQLException {
        if (this.useTransaction) {
            this.sqlMapClient.endTransaction();
        }
    }

    public void endTransaction(SqlMapClient sqlMapClient) throws DALException {
        try {
            sqlMapClient.endTransaction();
        } catch (SQLException e) {
            throw new DALException("Unable to end transaction", e);
        }
    }
}