package com.archermind.txtbl.authenticator;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import java.util.List;


/**
 * Class that 'web scrapes' gmail to enable IMAP for a particular user. Uses HtmlUnit to
 * invoke, process, and submit pages from gmail. If IMAP is already enalbed then this class
 * does nothing. Loads the 'basic html' version of gmail to make scraping easier.
 *
 * Author: Romeo Francis
 * Date: Apr 23, 2009
 * Time: 7:20:37 PM
 */
public class GmailImapEnabler {
    private static final String GMAIL_URL = "http://mail.google.com";
    private static final String USER_NAME_INPUT_ID = "Email";
    private static final String PASSWORD_INPUT_ID = "Passwd";
    private static final String SIGNIN_BUTTON_ID = "signIn";
    private static final String BASIC_HTML_HREF = "?ui=html";
    private static final String SETTINGS_TEXT = "Settings";
    private static final String POP_IMAP_TEXT = "Forwarding and POP/IMAP";
    private static final String IMAP_ENABLED_RADIO_ID = "bx_ie_1";
    private static final String SETTINGS_SAVE_BUTTON_NAME = "nvp_a_prefs";

    /**
     * Web scrape gmail application and enable IMAP for the user specified if it is
     * disabled.
     *
     * @param userId gmail userid
     * @param password gmail password
     */
    public void enableImap(String userId, String password) throws GmailImapEnablerException {
        WebClient webClient = new WebClient();
        webClient.setJavaScriptEnabled(false);

        try {
            //
            // Get the gmail login page
            //
            HtmlPage loginPage = webClient.getPage(GMAIL_URL);

            //
            // Get the username and password input fields and enter
            // the values passed in
            //
            HtmlTextInput userNameInput = loginPage.getHtmlElementById(USER_NAME_INPUT_ID);
            HtmlPasswordInput passwordInput = loginPage.getHtmlElementById(PASSWORD_INPUT_ID);

            userNameInput.setValueAttribute(userId);
            passwordInput.setValueAttribute(password);

            //
            // Get the enclosing form and submit it to login
            //
            HtmlForm loginForm = userNameInput.getEnclosingForm();
            HtmlSubmitInput signInButton = loginForm.getInputByName(SIGNIN_BUTTON_ID);
            HtmlPage gmailPage = signInButton.click();

            //
            // Really hard to process standard version of gmail because its all ajax based with javascript and
            // dynamically loaded data. So, find the use 'basic html' link and click it.  If "basic html" link is
            // not found then assume its already in "basic html" mode.
            //
            HtmlPage basicGmailPage;
            HtmlAnchor basicHtmlLink = this.getAnchorByPartialHref(gmailPage, BASIC_HTML_HREF);
            if (basicHtmlLink != null) {
                basicGmailPage = basicHtmlLink.click();
            } else {
                //
                // Check to see if we are back to the login page, most likely caused by an invalid userid/password
                //
                userNameInput = null;
                try {
                    userNameInput = gmailPage.getHtmlElementById(USER_NAME_INPUT_ID);
                } catch (Exception ex) {
                    // html unit throws a runitime exception when it can't find an element
                    // means all is good
                }

                if (userNameInput != null) {
                    throw new GmailImapEnablerException("Gmail account information invalid, cannot enable IMAP.");
                }

                //
                // Did not find the user id input on the page so assume user set gmail to basic version.
                //
                basicGmailPage = gmailPage;
            }

            //
            // Once the basic version of gmail is loaded find the "Settings' anchor and
            // click it.
            //
            HtmlAnchor settingsLink = this.getAnchorByInnerHTML(basicGmailPage, SETTINGS_TEXT);
            if (settingsLink != null) {
                HtmlPage settingsPage = settingsLink.click();

                //
                // Find the POP/IMAP link on the settings page and click it
                //
                HtmlAnchor imapLink = this.getAnchorByInnerHTML(settingsPage, POP_IMAP_TEXT);
                if (imapLink != null) {
                    HtmlPage imapPage = imapLink.click();

                    //
                    // Check to see if IMAP is enabled, if it is not then enable it.
                    //
                    HtmlRadioButtonInput imapEnabledRadio = (HtmlRadioButtonInput) imapPage.getElementById(IMAP_ENABLED_RADIO_ID);
                    if (! imapEnabledRadio.isChecked()) {
                        imapEnabledRadio.click();

                        HtmlSubmitInput saveButton = imapPage.getElementByName(SETTINGS_SAVE_BUTTON_NAME);
                        saveButton.click();
                    }
                } else {
                    throw new GmailImapEnablerException("Unable to find the 'IMAP' link, cannot enable IMAP.");
                }
            } else {
                throw new GmailImapEnablerException("Unable to find the 'Settings' link, cannot enable IMAP.");
            }
        } catch (GmailImapEnablerException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new GmailImapEnablerException(ex);
        } finally {
            webClient.closeAllWindows();
        }
    }

    /**
     * Find the anchor/link with the href that partially matches the pattern
     * specified.
     *
     * @param page The html page
     * @param matchingAnchorHref The href that should be matched
     *
     * @return The anchor that has the matching href, will return null if not found
     */
    private HtmlAnchor getAnchorByPartialHref(HtmlPage page, String matchingAnchorHref) {
        HtmlAnchor foundAnchor = null;

        List<HtmlAnchor> anchors = page.getAnchors();
        for (HtmlAnchor anchor : anchors) {
            String anchorHref = anchor.getHrefAttribute();
            if (anchorHref != null && anchor.getHrefAttribute().indexOf(matchingAnchorHref) != -1) {
                foundAnchor = anchor;
                break;
            }
        }

        return foundAnchor;
    }

    /**
     * Find the anchor/link with the specified inner html in the specified
     * page.
     *
     * @param page The html page
     * @param anchorInnerHtml The anchor inner html to search by
     *
     * @return The anchor that has the matching text, will return null if not found
     */
    private HtmlAnchor getAnchorByInnerHTML(HtmlPage page, String anchorInnerHtml) {
        HtmlAnchor foundAnchor = null;

        List<HtmlAnchor> anchors = page.getAnchors();
        for (HtmlAnchor anchor : anchors) {
            String anchorText = anchor.getTextContent();
            if (anchorText != null && anchorText.indexOf(anchorInnerHtml) != -1) {
                foundAnchor = anchor;
                break;
            }
        }

        return foundAnchor;
    }
}
