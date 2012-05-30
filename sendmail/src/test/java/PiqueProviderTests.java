import com.archermind.txtbl.authenticator.Authenticator;
import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.Body;
import com.archermind.txtbl.domain.Email;
import com.archermind.txtbl.domain.EmailPojo;
import com.archermind.txtbl.mail.store.ApacheS3MessageStore;
import com.archermind.txtbl.mail.store.MessageStoreException;
import com.archermind.txtbl.sender.mail.abst.impl.PiqueOperator;
import com.archermind.txtbl.validate.mailbox.abst.impl.ActiveSyncValidate;
import com.ibatis.sqlmap.engine.type.SimpleDateFormatter;
import com.zynku.sync.activesync.context.ActiveSyncContext;
import com.zynku.sync.activesync.control.ActiveSyncController;
import com.zynku.sync.activesync.model.ApplicationData;
import com.zynku.sync.activesync.model.Folder;
import junit.framework.Assert;
import org.apache.log4j.BasicConfigurator;
import org.junit.Test;

import javax.mail.MessagingException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class PiqueProviderTests extends AbstractProviderTest
{

    @Test
    public void testSendPiq() throws DALException, MessagingException, InterruptedException, MessageStoreException , Exception
    {
        Account account = getAccount("newimap", "dan.morel@gmail.com");
        System.out.println("test account = " + account);
        EmailPojo emailPojo = new EmailPojo();
        Email email = new Email();
        email.setTo("dan.morel@gmail.com");
        email.setSubject("testing a piq over the pique service");
        email.setEmail_type("PIQ");
        email.setFrom_alias("Dudey Dude");
        email.setFrom("dan@getpeek.com");
        email.setMessage_type("PIQ");

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String sDate = fmt.format(new Date());

        email.setSentTime(sDate);
        email.setMaildate(sDate);
        email.setModify_time(sDate);
        email.setOriginal_account("dan@getpeek.com");
        email.setReply("dan@getpeek.com");
        email.setSent(false);
        email.setUserId(account.getUser_id());
        System.out.println(email);

        Body body = new Body();
        body.setData("testing sending a piq".getBytes());

        emailPojo.setEmail(email);
        emailPojo.setAccount(account);
        emailPojo.setBody(body);

        List<EmailPojo> emailPojoList = new ArrayList<EmailPojo>();
        emailPojoList.add(emailPojo);

        String result = new PiqueOperator().sendMail(emailPojoList);




    }

}