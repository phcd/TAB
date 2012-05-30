package com.archermind.txtbl.utils.exchange;

import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.domain.ExchangeDiagnostics;
import com.archermind.txtbl.domain.ExchangeDiagnosticsResult;

import com.archermind.txtbl.utils.MSExchangeUtil;
import com.webalgorithm.exchange.ConnectionPick;
import com.webalgorithm.exchange.ExchangeClient;
import com.webalgorithm.exchange.dto.Attachment;
import com.webalgorithm.exchange.dto.Message;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MSExchangeDiagnosisUtil {
    private static final Logger logger = Logger.getLogger(MSExchangeDiagnosisUtil.class);
    private static final String DIAGNOSE_EMAIL_BODY = "Dear User,\n\n" + "  This is a test email to confirm the send capability of your exchange email account on Peek. This email has been auto-generated from your exchange account '%s' and will reflect in your sent items folder.\n\n" + "Sincerely,\n\n" + "The Peek Team\n" + "www.getpeek.in";
    private static final String DIAGNOSE_EMAIL_SUBJECT = "Peek Test Email";
    
    public static List<ExchangeDiagnosticsResult> diagnose(Account account, boolean sendDiagnostic) {
        Map<ExchangeDiagnostics, ExchangeDiagnosticsResult> resultsMap = setupDiagnosticsResults();
        String context = String.format("Host - %s, Port - %s, useSSL - %s, email - %s, loginName - %s, fbaPath - %s, prefix - %s", account.getReceiveHost(), account.getReceivePort(), account.getReceiveTs(), account.getName(), account.getLoginName(), account.getReceiveHostPrefix(), account.getReceiveHostFbaPath());
        logger.info("Running diagnosis for " + context);
        try {
            ConnectionPick cp = MSExchangeUtil.getExchangeClient(account.getExchangeConnMode(), account.getReceiveHost(), account.getReceivePort(), account.getReceiveTs(), account.getName(), account.getLoginName(), account.getPassword(), account.getReceiveHostPrefix(), account.getReceiveHostFbaPath());
            ExchangeClient exchangeClient = cp.getExchangeClient();
            resultsMap.get(ExchangeDiagnostics.CONNECTION).setResult(ExchangeDiagnosticsResult.DiagnosticsResult.SUCCESS);
            logger.info("Connection diagnosis successful for " + context);

            diagnoseContacts(resultsMap.get(ExchangeDiagnostics.CONTACTS), exchangeClient, context);

            Collection<Message> messages = new ArrayList<Message>();
            try {
                messages = exchangeClient.getMessagesHeaders("Inbox/");
                resultsMap.get(ExchangeDiagnostics.MESSAGES).setResult(ExchangeDiagnosticsResult.DiagnosticsResult.SUCCESS);
                logger.info("Messages diagnosis successful for " + context);
            } catch (Exception e) {
                logger.warn("Messages failed: " + context, e);
                resultsMap.get(ExchangeDiagnostics.MESSAGES).setResult(ExchangeDiagnosticsResult.DiagnosticsResult.FAILURE);
            }

            diagnoseAttachments(resultsMap.get(ExchangeDiagnostics.ATTACHMENTS), exchangeClient, messages, context);
            if(sendDiagnostic) {
                diagnoseSendMessage(resultsMap.get(ExchangeDiagnostics.SEND), exchangeClient, account, context);
            }
        } catch (Exception e) {
            logger.warn("Connection failed: " + context, e);
            resultsMap.get(ExchangeDiagnostics.CONNECTION).setResult(ExchangeDiagnosticsResult.DiagnosticsResult.FAILURE);
        }
        //doing instead of resultsMap.values() to ensure order
        ArrayList<ExchangeDiagnosticsResult> results = new ArrayList<ExchangeDiagnosticsResult>();
        for (ExchangeDiagnostics diagnostics : ExchangeDiagnostics.values()) {
            results.add(resultsMap.get(diagnostics));
        }
        return results;
    }

    private static void diagnoseSendMessage(ExchangeDiagnosticsResult result, ExchangeClient exchangeClient, Account account, String context) {
        try {
            Message message = new Message();
            message.setTo(account.getName());
            message.setBody(String.format(DIAGNOSE_EMAIL_BODY, account.getName()));
            message.setFromName(account.getAlias_name());
            message.setFromEmail(account.getName());
            message.setSubject(DIAGNOSE_EMAIL_SUBJECT);

            exchangeClient.sendMessage(message);
            result.setResult(ExchangeDiagnosticsResult.DiagnosticsResult.SUCCESS);
            logger.info("Send diagnosis successful for " + context);
        } catch(Exception e) {
            logger.warn("Send failed: " + context, e);
            result.setResult(ExchangeDiagnosticsResult.DiagnosticsResult.FAILURE);
        }
    }

    private static void diagnoseAttachments(ExchangeDiagnosticsResult result, ExchangeClient exchangeClient, Collection<Message> messages, String context) {
        boolean attachmentsFailed = false;
        boolean attachmentPassed = false;
        for (Message message : messages) {
            if (message.isHasAttachment()) {
                try {
                    Collection<Attachment> attachmentCollection = exchangeClient.getAttachments(message.getHref());
                    for (Attachment attachment : attachmentCollection) {
                        exchangeClient.getAttachmentData(attachment.getHref());
                        attachmentPassed = true;
                        logger.info("Attahcments diagnosis successful for " + context);
                        break;
                    }
                } catch (Exception e) {
                    logger.warn("Attachments failed: " + context, e);
                    attachmentsFailed = true;
                    break;
                }
            }
            if (attachmentPassed) {
                break;
            }
        }

        if (attachmentPassed) {
            result.setResult(ExchangeDiagnosticsResult.DiagnosticsResult.SUCCESS);
        } else if (attachmentsFailed) {
            result.setResult(ExchangeDiagnosticsResult.DiagnosticsResult.FAILURE);
        }
    }

    private static void diagnoseContacts(ExchangeDiagnosticsResult result, ExchangeClient exchangeClient, String context) {
        try {
            exchangeClient.getContacts();
            result.setResult(ExchangeDiagnosticsResult.DiagnosticsResult.SUCCESS);
            logger.info("Contacts diagnosis successful for " + context);
        } catch (Exception e) {
            logger.warn("Contacts failed: " + context, e);
            result.setResult(ExchangeDiagnosticsResult.DiagnosticsResult.FAILURE);
        }
    }

    private static Map<ExchangeDiagnostics, ExchangeDiagnosticsResult> setupDiagnosticsResults() {
        Map<ExchangeDiagnostics, ExchangeDiagnosticsResult> results = new HashMap<ExchangeDiagnostics, ExchangeDiagnosticsResult>();
        for (ExchangeDiagnostics diagnostics : ExchangeDiagnostics.values()) {
            results.put(diagnostics, new ExchangeDiagnosticsResult(diagnostics, ExchangeDiagnosticsResult.DiagnosticsResult.NOT_TESTED));
        }
        return results;
    }

    public static void main(String[] args) {
        Account account = new Account();
        account.setReceiveTs("ssl");
        account.setReceiveHost("exchange.getpeek.in");
        account.setReceiveHostFbaPath("/exchweb/bin/auth/owaauth.dll");
        account.setReceiveHostPrefix("exchange");
        account.setReceivePort("443");
        account.setLoginName("paul");
        account.setName("paul@exchange.getpeek.in");
        account.setPassword("xxxx");

        MSExchangeDiagnosisUtil.diagnose(account, true);
    }
}
