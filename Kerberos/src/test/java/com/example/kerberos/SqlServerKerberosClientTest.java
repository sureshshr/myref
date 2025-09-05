package com.example.kerberos;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SqlServerKerberosClientTest {

    @Test
    public void testSqlServerKerberos_whenConfigured() throws Exception {
        Assumptions.assumeTrue("true".equalsIgnoreCase(System.getProperty("kerberos.sql.test")),
                "SQL Server Kerberos test skipped - set -Dkerberos.sql.test=true to run");

        String jdbcUrl = System.getProperty("kerberos.sql.url");
        String query = System.getProperty("kerberos.sql.query", "SELECT 1");
        Assumptions.assumeTrue(jdbcUrl != null, "Set -Dkerberos.sql.url to run the test");

        SqlServerKerberosClient client = new SqlServerKerberosClient("KrbLogin");
        String res = client.queryFirstCell(jdbcUrl, query);
        assertNotNull(res);
    }
}
