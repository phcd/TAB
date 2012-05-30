package com.archermind.txtbl.authenticator;

import com.archermind.txtbl.utils.StringUtils;
import com.archermind.txtbl.utils.SysConfigManager;

import javax.mail.AuthenticationFailedException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LoginUtil {
    public static LoginUtil INSTANCE = new LoginUtil();

    private Set<String> loginFailureMessages = new HashSet<String>();

    public LoginUtil() {
        loginFailureMessages.add("invalid username or password");
        loginFailureMessages.add("invalid credentials");
        loginFailureMessages.add("incorrect name and/or password");
        loginFailureMessages.add("password");
        loginFailureMessages.add("credentials");
        loginFailureMessages.add("AUTHENTICATE failed");
        loginFailureMessages.add("Authentication failed");
        loginFailureMessages.add("AUTHENTICATE GroupWise login failed");
        loginFailureMessages.add("Login failed.");
        loginFailureMessages.add("login failed");
        loginFailureMessages.add("authorization failed");
        loginFailureMessages.add("Authorization failed");
        loginFailureMessages.add("authentication failed");
        loginFailureMessages.add("user invalid");
        loginFailureMessages.add("User disabled");
        loginFailureMessages.add("Invalid login");
        loginFailureMessages.add("not a valid user");
        loginFailureMessages.add("account is currently inactive");
        loginFailureMessages.add("Invalid user");

        String additionalLoginFailureMessages = SysConfigManager.instance().getValue("loginFailureMessages");

        if (StringUtils.isNotEmpty(additionalLoginFailureMessages)) {
            String[] messages = additionalLoginFailureMessages.split("###");
            loginFailureMessages.addAll(Arrays.asList(messages));
        }
    }

    public boolean isLoginFailure(AuthenticationFailedException exception) {
        if (exception != null && StringUtils.isNotEmpty(exception.getMessage())) {
            String error = exception.getMessage().toLowerCase();
            for (String message : loginFailureMessages) {
                if (error.contains(message)) {
                    return true;
                }
            }
        }
        return false;
    }

}
