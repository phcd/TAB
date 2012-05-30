package com.archermind.txtbl.dal.orm;

import com.archermind.txtbl.domain.Contact;

import java.sql.SQLException;
import java.util.List;
import java.util.HashMap;

public class ContactORMap extends BaseORMap {

    public ContactORMap(BaseORMap another) {
        super(another);
    }

    public ContactORMap(boolean useMaster) {
        super(useMaster);
    }

    public ContactORMap() {}

	public int addContact(Contact contact) throws SQLException {
		return this.sqlMapClient.update("Contact.addContact", contact);
	}

    @SuppressWarnings("unchecked")
	public List<Contact> getPagedContacts(HashMap param) throws SQLException {
        String contactListType = (String) param.get("type");
        if (contactListType != null && contactListType.toUpperCase().contains("TWITTER")) {
            return this.sqlMapClient.queryForList("Contact.getPagedContactsTwitter", param);
        }
        return this.sqlMapClient.queryForList("Contact.getPagedContacts", param);
	}

	public int modifyContact(Contact contact) throws SQLException {
		return this.sqlMapClient.update("Contact.modifyContact", contact);
	}

	public int removeContact(HashMap param) throws SQLException {
		return this.sqlMapClient.update("Contact.removeContact", param);
	}
}