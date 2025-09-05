package com.example.kerberos;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import javax.security.auth.login.LoginException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KerberosClientTest {

    @Test
    public void testLoginAndRun_whenKerberosConfigured() throws Exception {
        // Skip test unless test environment variable 'kerberos.test' is set to 'true'
        Assumptions.assumeTrue("true".equalsIgnoreCase(System.getProperty("kerberos.test")),
                "Kerberos test skipped - set -Dkerberos.test=true and provide krb5/jaas configs to run");

        KerberosClient client = new KerberosClient("KrbLogin");
        String res;
        try {
            res = client.loginAndRun();
        } catch (LoginException e) {
            // Re-throw with clearer message for test logs
            throw new LoginException("Login failed: " + e.getMessage());
        }
        assertEquals("authenticated", res);
    }
}
