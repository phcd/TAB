package com.archermind.txtbl.attachment.format.inte;

import java.util.List;

import com.archermind.txtbl.domain.Attachment;

public interface AttachmentFormat {
	public List<Attachment> format(String mailbox, String msgID, String filename, byte dataByte[], int targetHeight, int targetWidth);
}
