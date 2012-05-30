package com.archermind.txtbl.taskfactory.web;

import com.archermind.txtbl.dal.DALException;
import com.archermind.txtbl.dal.business.impl.UserService;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.taskfactory.TaskFactoryEngineImp;
import com.archermind.txtbl.taskfactory.common.AccountSenderIMP;
import com.archermind.txtbl.taskfactory.common.IsMSPAccountSenderFilter;
import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author youlianjie
 * @author vveksler - reworked best i could :)
 */

public class TFSubscribeServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(TFSubscribeServlet.class);
    private static IsMSPAccountSenderFilter isMSPAccountSenderFilter = new IsMSPAccountSenderFilter(AccountSenderIMP.getInstance());

    /**
     *
     */
    public TFSubscribeServlet()
    {
        super();
    }

    /**
     *
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        logger.info("processing possible notification request...");

        Notification notification = parseRequest(request);

        if (notification != null)
        {
            logger.info(String.format("received notification for transaction=%s, customer=%s", notification.getTransaction(), notification.getCustomer()));
            sendMessage(notification);
        }
    }
                                                     
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        if ("yes".equals(request.getParameter("loadparam")))
        {
            logger.info("processing loading parameter request...");

            AccountSenderIMP.getInstance().loadParam();
        }
    }

    /**
     * send message to the receiver
     */
    private void sendMessage(Notification notification)
    {
        List<Account> accounts = getAccount(notification);
        
        if (accounts == null)
            return;

        for (Account account : accounts)
        {
            logger.info(String.format("sending receive email request to provider for account=%s, uid=%s", account.getName(), account.getUser_id()));
            sendAccount(account);
            TaskFactoryEngineImp.getInstance().subscribeReceivedMail(account);
        }
    }

    private void sendAccount(Account account)
    {
        isMSPAccountSenderFilter.SendAccount(account);
    }

    /**
     * get account by the paraMap from database
     *
     * @return
     */
    private List<Account> getAccount(Notification notification)
    {
        List<Account> accounts;

        try
        {
            accounts = new UserService().getReceiveAccount(notification.getTransaction() + "," + notification.getCustomer());
        }
        catch (DALException e)
        {
            logger.error(String.format("error when get account by  transactionID [%s], customerID=[%s]", notification.getTransaction(), notification.getCustomer()));
            return null;
        }

        if (accounts == null || accounts.size() == 0)
        {
            logger.error(String.format("there is no account in the database with transactionID [%s], customerID=[%s]", notification.getTransaction(), notification.getCustomer()));
            return null;
        }

        return accounts;
    }

    /**
     * parse the parameters from the request
     *
     * @param request
     * @return
     */
    private Notification parseRequest(HttpServletRequest request) throws IOException
    {
        String notification = IOUtils.toString(request.getInputStream());

        //logger.info(String.format("processing msp notification %s", notification));
        
        String transaction = getTransactionID(notification);

        if (transaction == null)
        {
            logger.warn(String.format("unexpected request, missing transaction id %s", notification));
            return null;
        }

        String customerID = getWebServiceCustomerID(notification);

        return new Notification(transaction, customerID);
    }

    /**
     * get TransactionID from the request inputstream
     *
     * @param soap
     * @return
     */
    private String getTransactionID(String soap)
    {
        String[] temp = soap.split("<TransactionID>");
        if (temp.length > 1)
        {
            return temp[1].split("</TransactionID>")[0];
        }
        else
        {
            logger.error("this is bad request!! request [" + soap + "]");
            return null;
        }
    }

    class Notification
    {
        private String transaction;
        private String customer;

        Notification(String transaction, String customer)
        {
            this.transaction = transaction;
            this.customer = customer;
        }

        public String getTransaction()
        {
            return transaction;
        }

        public String getCustomer()
        {
            return customer;
        }
    }

    public static String getWebServiceCustomerID(String s)
    {
        String[] temp = s.split("<WebServiceCustomerID>");
        if (temp.length > 1)
        {
            return temp[1].split("</WebServiceCustomerID>")[0];
        }
        else
            return "";
    }
}
