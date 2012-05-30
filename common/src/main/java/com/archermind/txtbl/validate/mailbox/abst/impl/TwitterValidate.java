package com.archermind.txtbl.validate.mailbox.abst.impl;

import com.archermind.txtbl.dal.business.impl.UserService;
import com.archermind.txtbl.domain.Account;
import com.archermind.txtbl.utils.SysConfigManager;
import com.archermind.txtbl.validate.mailbox.abst.Validate;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

public class TwitterValidate extends Validate {

    private static String twitterPeekDomain;

    static {
        twitterPeekDomain = SysConfigManager.instance().getValue("twitterPeekDomain", "twitterpeek");
    }


    /**
     * Validates the account by attempting to login into twitter.
     *
     * @param account The user's account, source of login name and password.
     * @throws Exception If the account cannot be validated.
     */
    public void validate(Account account) throws Exception {
        try{
            new UserService().fetchTwitterToken(account.getName());
        }catch (Throwable t){
            throw new Exception("Twitter account doesn't exist.");
        }
    }

    /**
     * Intializes the Twitter interface using the supplied credentials. If the username ends in "@twitterpeek",
     * then the username is assumed to be the user's twitter ID, otherwise, the username is supplied as is for
     * authentication.
     *
     * @param username The user's twitter ID
     * @param password The user's twitter password
     * @return A reference to the Twitter object which can be used to perform twitter operations.
     *
     */
    public static Twitter init(final String username, final String password)
    {
        //TODO: need to figure out where the setting of the base url when
        Twitter twitter = new TwitterFactory().getInstance();

        /*
        twitter.setUserId(getTwitterId(username));
        twitter.setPassword(password);

        twitter.setSource("peekinc");*/

        return twitter;
    }

    /**
     * Returns the twitter id from an email of the form <twitter_id>@twitterpeek
     *
     * @param emailAddress  Email of the form <twitter_id>@twitterpeek
     * @return  the <twitter_id> portion of the email address, or the email address if an invalid format is specified.
     */
    public static String getTwitterId(String emailAddress) {
        if (emailAddress.endsWith("@" + twitterPeekDomain)) {
            return emailAddress.substring(0, emailAddress.indexOf('@'));
        }
        return emailAddress;
    }

}
