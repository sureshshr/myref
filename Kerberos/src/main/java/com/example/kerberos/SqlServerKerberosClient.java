package com.example.kerberos;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * SQL Server client demonstrating Kerberos authentication using the Microsoft
 * JDBC driver.
 * For Windows integrated auth, ensure the driver is configured correctly. For
 * Linux, ensure JAAS/krb5 setup and a keytab/ticket cache.
 */
public class SqlServerKerberosClient {

    private final String loginContextName;

    public SqlServerKerberosClient(String loginContextName) {
        this.loginContextName = loginContextName;
    }

    /**
     * Run a simple query against SQL Server and return the first column of the
     * first row as string.
     * The jdbcUrl should include integratedSecurity=true for Windows native auth or
     * use Kerberos configuration for Linux.
     */
    public String queryFirstCell(final String jdbcUrl, final String query) throws Exception {
        LoginContext lc = new LoginContext(loginContextName);
        lc.login();
        Subject subject = lc.getSubject();

        try {
            return Subject.doAs(subject, (PrivilegedExceptionAction<String>) () -> {
                try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
                    try (Statement st = conn.createStatement()) {
                        try (ResultSet rs = st.executeQuery(query)) {
                            if (rs.next()) {
                                return rs.getString(1);
                            } else {
                                return null;
                            }
                        }
                    }
                }
            });
        } finally {
            lc.logout();
        }
    }
}
