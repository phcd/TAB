package com.archermind.txtbl.utils;

import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslClientFactory;
import java.util.Map;

public class XoauthSaslClientFactory implements SaslClientFactory
{
  public static final String OAUTH_TOKEN_PROP =
      "mail.imaps.sasl.mechanisms.xoauth.oauthToken";
  public static final String OAUTH_TOKEN_SECRET_PROP =
      "mail.imaps.sasl.mechanisms.xoauth.oauthTokenSecret";
  public static final String CONSUMER_KEY_PROP =
      "mail.imaps.sasl.mechanisms.xoauth.consumerKey";
  public static final String CONSUMER_SECRET_PROP =
      "mail.imaps.sasl.mechanisms.xoauth.consumerSecret";

  public SaslClient createSaslClient(String[] mechanisms,
                                     String authorizationId,
                                     String protocol,
                                     String serverName,
                                     Map<String, ?> props,
                                     CallbackHandler callbackHandler) {
    boolean matchedMechanism = false;
      for (String mechanism : mechanisms) {
          if ("XOAUTH".equalsIgnoreCase(mechanism)) {
              matchedMechanism = true;
              break;
          }
      }
    if (!matchedMechanism) {
      return null;
    }
    XoauthProtocol xoauthProtocol = null;
    if ("imaps".equalsIgnoreCase(protocol)) {
      xoauthProtocol = XoauthProtocol.IMAP;
    }
    // TODO: support smtp
    if (xoauthProtocol == null) {
      return null;
    }
    return new XoauthSaslClient(xoauthProtocol,
                                (String) props.get(OAUTH_TOKEN_PROP),
                                (String) props.get(OAUTH_TOKEN_SECRET_PROP),
                                (String) props.get(CONSUMER_KEY_PROP),
                                (String) props.get(CONSUMER_SECRET_PROP),
                                callbackHandler);
  }

  public String[] getMechanismNames(Map<String, ?> props) {
    return new String[] {"XOAUTH"};
  }
}
