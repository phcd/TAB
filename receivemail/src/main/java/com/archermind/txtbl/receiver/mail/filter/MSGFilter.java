package com.archermind.txtbl.receiver.mail.filter;

import com.archermind.txtbl.parser.MessageParser;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

public class MSGFilter {

	public MessageParser parser = null;

	protected static BeanFactory attachmentFactory = null;

	private static final Logger log = Logger.getLogger(MSGFilter.class);

	static {
		try {
			attachmentFactory = new XmlBeanFactory(new ClassPathResource("com/archermind/txtbl/attachment/format/attachmentFactory.xml"));
			log.info("[Initialization Attachment Factory Success]");
		} catch (Exception e) {
			log.error("static/MSGFilter/Exception: ", e);
		}
	}

	public void setParser(MessageParser parser) {
		this.parser = parser;
	}

}
