package com.archermind.txtbl.dal.orm;

import com.archermind.txtbl.domain.Client;

import java.sql.SQLException;


public class ClientORMap extends BaseORMap {

    public ClientORMap(boolean useMaster) {
        super(useMaster);
    }

    public ClientORMap() {
    }

    public int addClient(Client client) throws SQLException {
        return this.sqlMapClient.update("Client.addClient", client);
    }

    public Client getClientById(String id) throws SQLException {
        return (Client) this.sqlMapClient.queryForObject("Client.getClientById", id);
    }


}