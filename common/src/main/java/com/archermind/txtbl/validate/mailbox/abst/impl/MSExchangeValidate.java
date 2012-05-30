package com.archermind.txtbl.validate.mailbox.abst.impl;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.validate.mailbox.abst.Validate;
import com.archermind.txtbl.utils.MSExchangeUtil;
import com.webalgorithm.exchange.ConnectionPick;
import com.webalgorithm.exchange.ExchangeClient;
import com.webalgorithm.exchange.ExchangeClientFactory;
import org.jboss.logging.Logger;

public class MSExchangeValidate extends Validate
{
    private static final Logger log = Logger.getLogger(MSExchangeValidate.class);

    /**
     * Validates the account by attempting to login into the exchange server using the account credentials. If the login
     * name of the account is not the same as the account name (email address), we simply attempt to login using the
     * login name. If the login name is the same as the account name, we first attempt to login using the account name-
     * if this fails, we fallback to using the username component of the account name. If we are successful, we update
     * the account's login name accordingly.
     *
     * @param account
     * @throws Exception
     */
    public void validate(Account account) throws Exception
    {
        /*
        * Throws an exception if we cannot access the user's account
        */
        log.info("Validating Exchange server " + account.getReceiveHost() + " for account " + account);

        int port = MSExchangeUtil.getPort(account.getReceivePort());
        boolean useSSL = MSExchangeUtil.useSSL(account.getReceiveTs());

        doValidate(account.getReceiveHost(), port, useSSL, account.getName(), account.getLoginName(), account.getPassword(), account.getReceiveHostFbaPath(), account.getReceiveHostPrefix());

    }


    private void doValidate(String exchangeServer, int port, boolean useSSL, String emailAccount, String loginName, String password, String fbaPath, String prefix) throws Exception {
        log.info(String.format("Validating Exchange server host - %s port - %s ssl - %s email - %s login - %s fbapath - %s prefix - %s", exchangeServer, port, useSSL, emailAccount, loginName, fbaPath, prefix));

        /*
        * Throws an exception if we cannot access the user's account
        */
        log.info("Validating Exchange server " + exchangeServer + " for account " + emailAccount);

        try {
            ExchangeClient exchangeClient = null;

            log.info("trying to pick connection ");
            ConnectionPick cp = ExchangeClientFactory.pickConnection(exchangeServer, port, useSSL, emailAccount, loginName, password, prefix, fbaPath);

            if (cp != null) {
                exchangeClient = cp.getExchangeClient();
            }

            if (exchangeClient != null) {
                exchangeClient.close();
            }
        }
        catch (Throwable t) {
            log.error(t);
            t.printStackTrace();
            log.fatal(String.format("Unable to validate MSExchange account %s", emailAccount));
            throw new RuntimeException(String.format("Unable to validate MSExchange account %s", emailAccount), t);
        }
    }

}
