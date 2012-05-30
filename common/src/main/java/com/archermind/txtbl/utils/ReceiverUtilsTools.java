package com.archermind.txtbl.utils;


import org.apache.html.dom.HTMLDocumentImpl;
import org.cyberneko.html.parsers.DOMFragmentParser;
import org.jboss.logging.Logger;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class ReceiverUtilsTools {
    private static final String HOSTNAME = System.getenv("hostname");

    private static final Logger log = Logger.getLogger(ReceiverUtilsTools.class);

	private static String[] originalCharacter = null;

	private static String[] replacerCharacter = null;
	
	static {
		Properties prop = new Properties();
		try {
			prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("com/archermind/txtbl/receiver/mail/prop/replaceChar.properties"));
			originalCharacter = prop.getProperty("originalCharacter").split("SPLIT");
			replacerCharacter = prop.getProperty("replacerCharacter").split("SPLIT");
		} catch (Exception e) {
			originalCharacter = null;
			replacerCharacter = null;
			log.error("replaceString/ReceiverUtilsTools/Exception: ", e);
		}
	}

    // TODO - Implement me!!
    public static String getIPAddress() {
        return HOSTNAME;
    }

	public static String replace(String str) throws UnsupportedEncodingException {
		if (str == null) {
			str = "";
		}
		if (!"".equals(str.trim())) {
			if (originalCharacter != null) {
				for (int i = 0; i < originalCharacter.length; i++) {
					str = str.replaceAll(originalCharacter[i], replacerCharacter[i]);
				}
			}
		}
		return str;
	}
	
	public static String subSubject(String subject, int len) {
		if (subject != null) {
			subject = subject.trim();
			if (subject.length() > len - 3) {
				subject = subject.substring(0, len - 3) + "...";
			}
		}
		return subject;
	}

	public static String dateToStr(Date date) {

		String dateTime = "";
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (date != null) {
			dateTime = dateFormat.format(date);
		}
		return dateTime;
	}
	
	public static String htmToTxt(String content) {
		try {
            if (content != null)
            {
                content = content.replaceAll("&nbsp;", " ");
                content = content.replaceAll("&NBSP;", " ");
                DOMFragmentParser parser = new DOMFragmentParser();
                DocumentFragment node = new HTMLDocumentImpl().createDocumentFragment();
                Reader sr = new StringReader(content);
                parser.parse(new InputSource(sr), node);
                StringBuffer newContent = new StringBuffer();
                getText(newContent, node);
                return newContent.toString().replaceAll(" *[\\r|\\n]{1,} *[\\r|\\s]{1,} *", "\n");
            }
            else
            {
                return null;
            }
		} catch (Throwable e) {
			log.error("failed to convert: " + content, e);
			return htmlToText(content).replaceAll(" *[\\r|\\n]{1,} *[\\r|\\s]{1,} *", "\n");
		}
	}

	private static void getText(StringBuffer sb, Node node) {

		if ("TITLE".equalsIgnoreCase(node.getNodeName()) || "META".equalsIgnoreCase(node.getNodeName()) || "SCRIPT".equalsIgnoreCase(node.getNodeName())
				|| "STYLE".equalsIgnoreCase(node.getNodeName()) || node.getNodeType() == Node.COMMENT_NODE) {
			return;
		}
		if (node.getNodeType() == Node.TEXT_NODE) {
			if(node.getNodeValue()!=null&&node.getNodeValue().trim().length()>0){
				if(node.getParentNode()==null){
					sb.append(" ").append(node.getNodeValue().trim().replaceAll("\\r|\\n", " ")).append(" ");
				}else if(node.getParentNode().hasAttributes()){
					Node dir = node.getParentNode().getAttributes().getNamedItem("dir");
					if (dir != null && "rtl".equalsIgnoreCase(dir.getNodeValue())){
                        if ("pre".equalsIgnoreCase(node.getParentNode().getNodeName())) {
                            sb.append(" ").append(reverseStr(node.getNodeValue()).trim()).append(" ");
                        } else {
                            sb.append(" ").append(reverseStr(node.getNodeValue()).trim().replaceAll("\\r|\\n", " ")).append(" ");
                        }
                    }
					else{
						if("pre".equalsIgnoreCase(node.getParentNode().getNodeName()))sb.append(node.getNodeValue());
						else sb.append(node.getNodeValue().replaceAll("\\r|\\n", " "));
					}
				}else{
					if("pre".equalsIgnoreCase(node.getParentNode().getNodeName()))sb.append(node.getNodeValue());
					else sb.append(node.getNodeValue().replaceAll("\\r|\\n", " "));
				}
			}
		}

		if ("br".equalsIgnoreCase(node.getNodeName()) || "P".equalsIgnoreCase(node.getNodeName()) || "span".equalsIgnoreCase(node.getNodeName()) || "li".equalsIgnoreCase(node.getNodeName())
				|| "tr".equalsIgnoreCase(node.getNodeName()) || "hr".equalsIgnoreCase(node.getNodeName()) || "dl".equalsIgnoreCase(node.getNodeName())

		)
			sb.append("\n");

		if (node.getNodeName() != null && node.getNodeName().toLowerCase().matches("^h\\d")) {
			sb.append("\n");
		}

		NodeList children = node.getChildNodes();
		if (children != null) {
			int len = children.getLength();
			for (int i = 0; i < len; i++) {
				getText(sb, children.item(i));
			}
		}

		if ("br".equalsIgnoreCase(node.getNodeName()) || "P".equalsIgnoreCase(node.getNodeName()) || "span".equalsIgnoreCase(node.getNodeName()) || "li".equalsIgnoreCase(node.getNodeName())
				|| "tr".equalsIgnoreCase(node.getNodeName()) || "hr".equalsIgnoreCase(node.getNodeName()) || "dl".equalsIgnoreCase(node.getNodeName())

		)
			sb.append("\n");

		if (node.getNodeName() != null && node.getNodeName().toLowerCase().matches("^h\\d")) {
			sb.append("\n");
		}

		if ("td".equalsIgnoreCase(node.getNodeName())) {
			if (node.getChildNodes() == null || (node.getChildNodes().getLength() == 1 && node.getChildNodes().item(0).getNodeType() == Node.TEXT_NODE))
				sb.append(" | ");
			else
				sb.append("\n");
		}

		if ("dt".equalsIgnoreCase(node.getNodeName())) sb.append(":");

	}

	private static String htmlToText(String orgText) {

		String[] arysplit;
		int i, j, lens;
		String strOutput = "";

		arysplit = orgText.split("<");
		if (arysplit[0].length() > 0)
			j = 1;
		else
			j = 0;

		lens = arysplit.length;
		for (i = j; i < lens; i++) {
			if (arysplit[i].indexOf(">") > 0)
				arysplit[i] = arysplit[i].substring(arysplit[i].indexOf(">") + 1);
			else
				arysplit[i] = "<" + arysplit[i];
		}

		for (i = 0; i < lens; i++)
			strOutput = strOutput + arysplit[i];
		strOutput = strOutput.substring(1 - j);
		strOutput = strOutput.replaceAll(">", ">");
		strOutput = strOutput.replaceAll("<", "<");
		strOutput = strOutput.replaceAll("&gt;", ">");
		strOutput = strOutput.replaceAll("&lt;", "<");
		strOutput = strOutput.replaceAll("&nbsp;", " ");
		strOutput = strOutput.replaceAll("nbsp;", "");
		strOutput = strOutput.replaceAll("&quot;", "");
		return strOutput;
	}

	public static String reverseStr(String str) {
		char[] stack = new char[str.length()];
		for (int i = 0; i < str.length(); i++) {
			stack[i] = str.charAt(i);
		}
		StringBuffer reverseStr = new StringBuffer("");
		for (int j = stack.length - 1; j >= 0; j--) {
			reverseStr.append(stack[j]);
		}
		return reverseStr.toString();
	}

	public static void main(String[] args) throws Exception {
		byte[] content = "adfdsafasfsa</f".getBytes();
		System.out.println(ReceiverUtilsTools.bodyFilter(content));
		}
	
	public static String bodyFilter(byte[] content) {
		String retuContent = "";
		String bodyContentBack = "";
		try {
			if (content != null) {
				String bodyContent = new String(content, "UTF-8").trim();
				bodyContentBack = bodyContent;
				if (bodyContent.length() > 8) {
					String tempBodyContent = bodyContent.substring(0, bodyContent.length() - 8);
					bodyContent = bodyContent.substring(bodyContent.length() - 8, bodyContent.length());
					if (bodyContent.indexOf("</") != -1) {
						bodyContent = bodyContent.substring(0, bodyContent.indexOf("</"));
					}
					retuContent = tempBodyContent + bodyContent;
				}else{
					if (bodyContent.indexOf("</") != -1) {
						bodyContent = bodyContent.substring(0, bodyContent.indexOf("</"));
					}
					retuContent = bodyContent;
				}	
			}
		} catch (Exception e) {
			retuContent = bodyContentBack;
			log.warn("bodyFilter/ReceiverUtilsTools/Exception: ", e);
		}
		return retuContent;
	}
	
	public static String subAddress(String address) {
		if (address != null) {
			if (address.length() > 3000) {
				address = address.substring(0, 3000);
				if (address.indexOf(";") != -1) {
					address = address.substring(0, address.lastIndexOf(";"));
				}
			}
		}
		return address;
	}
	
    public static String printWatch(StopWatch watch, int folderDepth, int newMessages, boolean fullCheck)
    {
        StringBuilder sb = new StringBuilder(watch.shortSummary().replaceAll("': time", String.format(", depth=%d, new=%d, fullcheck=%s': time", folderDepth, newMessages, fullCheck)));
        sb.append('\n');
        StopWatch.TaskInfo[] tasks = watch.getTaskInfo();
        sb.append("-----------------------------------------\n");
        sb.append("ms     %     Task name\n");
        sb.append("-----------------------------------------\n");
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMinimumIntegerDigits(5);
        nf.setGroupingUsed(false);
        NumberFormat pf = NumberFormat.getPercentInstance();
        pf.setMinimumIntegerDigits(3);
        pf.setGroupingUsed(false);
        for (StopWatch.TaskInfo task : tasks) {
            if (task.getTimeMillis() > 0) {
                sb.append(nf.format(task.getTimeMillis())).append("  ");
                sb.append(pf.format(task.getTimeSeconds() / watch.getTotalTimeSeconds())).append("  ");
                sb.append(task.getTaskName()).append("\n");
            }
        }

        return sb.toString();
    }
}
