package com.example.kerberos;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PrivilegedExceptionAction;

/**
 * Minimal HTTP client that uses SPNEGO (Negotiate) via JGSS to get a service
 * ticket and call a protected HTTP endpoint.
 * This class demonstrates the steps but keeps the implementation minimal.
 */
public class HttpKerberosClient {

    private final String loginContextName;

    public HttpKerberosClient(String loginContextName) {
        this.loginContextName = loginContextName;
    }

    public String getWithSpnego(final String targetUrl, final String servicePrincipal) throws Exception {
        LoginContext lc = new LoginContext(loginContextName);
        lc.login();
        Subject subject = lc.getSubject();

        try {
            return Subject.doAs(subject, (PrivilegedExceptionAction<String>) () -> {
                // Establish GSSContext for SPNEGO
                GSSManager manager = GSSManager.getInstance();
                Oid spnegoOid = new Oid("1.3.6.1.5.5.2"); // SPNEGO
                GSSName serverName = manager.createName(servicePrincipal, GSSName.NT_HOSTBASED_SERVICE);
                GSSContext gssContext = manager.createContext(serverName, spnegoOid, null, GSSContext.DEFAULT_LIFETIME);
                gssContext.requestMutualAuth(true);
                gssContext.requestCredDeleg(false);

                byte[] token = new byte[0];
                token = gssContext.initSecContext(token, 0, token.length);

                // Create HTTP request with Authorization: Negotiate <base64token>
                URL url = new URL(targetUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                if (token != null && token.length > 0) {
                    String base64 = java.util.Base64.getEncoder().encodeToString(token);
                    conn.setRequestProperty("Authorization", "Negotiate " + base64);
                }
                conn.connect();

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                in.close();
                return sb.toString();
            });
        } finally {
            lc.logout();
        }
    }
}
