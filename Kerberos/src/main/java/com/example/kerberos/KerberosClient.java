package com.example.kerberos;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.security.PrivilegedAction;

/**
 * Minimal Kerberos client that logs in via JAAS and runs an action as the
 * authenticated Subject.
 * This class does not perform any network service calls; it demonstrates the
 * login flow.
 */
public class KerberosClient {

    private final String loginContextName;

    public KerberosClient(String loginContextName) {
        this.loginContextName = loginContextName;
    }

    public String loginAndRun() throws LoginException {
        LoginContext lc = new LoginContext(loginContextName);
        lc.login();
        Subject subject = lc.getSubject();

        // Run a simple privileged action as the authenticated subject
        String result = Subject.doAs(subject, (PrivilegedAction<String>) () -> "authenticated");

        lc.logout();
        return result;
    }
}
