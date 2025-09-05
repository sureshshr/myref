package com.example.kerberos;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpKerberosClientTest {

    @Test
    public void testHttpSpnego_whenConfigured() throws Exception {
        // Requires external Kerberos-enabled HTTP endpoint and proper JVM properties.
        Assumptions.assumeTrue("true".equalsIgnoreCase(System.getProperty("kerberos.http.test")),
                "HTTP Kerberos test skipped - set -Dkerberos.http.test=true and provide krb5/jaas configs to run");

        String target = System.getProperty("kerberos.http.target");
        String servicePrincipal = System.getProperty("kerberos.http.service");
        org.junit.jupiter.api.Assumptions.assumeTrue(target != null && servicePrincipal != null,
                "Set -Dkerberos.http.target and -Dkerberos.http.service to run the HTTP Kerberos test");

        HttpKerberosClient client = new HttpKerberosClient("KrbLogin");
        String res = client.getWithSpnego(target, servicePrincipal);
        assertTrue(res.length() > 0, "Expected non-empty response body");
    }
}
