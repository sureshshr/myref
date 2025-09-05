0. Prerequisites (quick checks)

Joined to AD
Command Prompt →

systeminfo | findstr /B /C:"Domain"

Should show Domain: YOUR.DOMAIN.

Time is in sync with the domain controller (Kerberos is picky: ≤5 min skew).

DNS resolves your DC (e.g., dc01.corp.example.com).

Java installed

java -version
javac -version

(Use JDK 8+)

1. Create/confirm Kerberos config
   Option 1 (default path)

Create C:\Windows\krb5.ini with your details:

[libdefaults]
default_realm = CORP.EXAMPLE.COM
dns_lookup_kdc = true
dns_lookup_realm = true
ticket_lifetime = 24h
forwardable = true

[realms]
CORP.EXAMPLE.COM = {
kdc = dc01.corp.example.com
admin_server = dc01.corp.example.com
}

[domain_realm]
.corp.example.com = CORP.EXAMPLE.COM
corp.example.com = CORP.EXAMPLE.COM

Option 2 (custom path)

Put krb5.ini anywhere (e.g., C:\dev\kerberos\krb5.ini) and pass:

-Djava.security.krb5.conf=C:\dev\kerberos\krb5.ini

2. Choose how you’ll authenticate
   A) Using a keytab (service principal)

You have a file like C:\dev\kerberos\was.keytab and a principal such as
HTTP/waswin01.corp.example.com@CORP.EXAMPLE.COM.

B) Using your AD user ticket (no keytab)

Make sure you have a ticket (either from domain logon or run kinit from MIT Kerberos for Windows):

klist

You should see a TGT (ticket-granting ticket).
If not, install MIT Kerberos for Windows and run:

kinit YOURUSER@CORP.EXAMPLE.COM

3. Create a JAAS file

Create C:\dev\kerberos\jaas.conf with both entries and use the one you need:

// === A) Keytab-based login ===
KerberosKeytab {
com.sun.security.auth.module.Krb5LoginModule required
useKeyTab=true
keyTab="C:/dev/kerberos/was.keytab"
principal="HTTP/waswin01.corp.example.com@CORP.EXAMPLE.COM"
storeKey=true
doNotPrompt=true
isInitiator=true
debug=true;
};

// === B) User-ticket (no keytab) ===
KerberosUser {
com.sun.security.auth.module.Krb5LoginModule required
useTicketCache=true
renewTGT=false
doNotPrompt=true
isInitiator=true
debug=true;
}

Adjust paths, hostnames, and realm names.

4. Write the minimal Java test

Create C:\dev\kerberos\KerberosTest.java:

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.security.PrivilegedAction;

// Optional GSS to fetch a service ticket and prove end-to-end
import org.ietf.jgss.\*;

public class KerberosTest {
public static void main(String[] args) {
// Profile name from jaas.conf: KerberosKeytab OR KerberosUser
String profile = (args.length > 0) ? args[0] : "KerberosUser";

        // Optional target SPN to request a service ticket for (e.g., HTTP or MSSQLSvc)
        String targetSpn = (args.length > 1) ? args[1] : null;

        try {
            LoginContext lc = new LoginContext(profile);
            lc.login();
            System.out.println("✅ JAAS login successful with profile: " + profile);

            Subject subject = lc.getSubject();
            System.out.println("Subject: " + subject);

            if (targetSpn != null) {
                // Request a service ticket via GSS-API (no need to contact the actual service)
                Subject.doAs(subject, (PrivilegedAction<Void>) () -> {
                    try {
                        GSSManager mgr = GSSManager.getInstance();
                        GSSName serverName = mgr.createName(targetSpn, GSSName.NT_HOSTBASED_SERVICE);
                        Oid krb5Oid = new Oid("1.2.840.113554.1.2.2"); // Kerberos V5

                        GSSContext ctx = mgr.createContext(
                                serverName,
                                krb5Oid,
                                null,
                                GSSContext.DEFAULT_LIFETIME
                        );
                        ctx.requestMutualAuth(true);
                        ctx.requestConf(true);
                        ctx.requestInteg(true);

                        byte[] token = new byte[0];
                        token = ctx.initSecContext(token, 0, token.length);
                        System.out.println("🎟  Acquired service ticket for: " + targetSpn +
                                " (token length: " + (token == null ? 0 : token.length) + ")");

                        ctx.dispose();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                });
            } else {
                System.out.println("No target SPN provided; JAAS login only.");
            }

        } catch (LoginException e) {
            System.err.println("❌ Kerberos authentication failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

}

5. Compile

In Command Prompt at C:\dev\kerberos:

javac KerberosTest.java

If you get missing GSS classes (rare on modern JDKs), ensure you’re using a standard Oracle/OpenJDK JDK (not a JRE-only install).

6. Run
   A) Using keytab
   java ^
   -Djava.security.auth.login.config=C:\dev\kerberos\jaas.conf ^
   -Djava.security.krb5.conf=C:\dev\kerberos\krb5.ini ^
   -Djavax.security.auth.useSubjectCredsOnly=false ^
   -Dsun.security.krb5.debug=true ^
   KerberosTest KerberosKeytab HTTP/waswin01.corp.example.com@CORP.EXAMPLE.COM

B) Using your user ticket (no keytab)

(Ensure klist shows a valid TGT)

java ^
-Djava.security.auth.login.config=C:\dev\kerberos\jaas.conf ^
-Djava.security.krb5.conf=C:\dev\kerberos\krb5.ini ^
-Djavax.security.auth.useSubjectCredsOnly=false ^
-Dsun.security.krb5.debug=true ^
KerberosTest KerberosUser HTTP/waswin01.corp.example.com@CORP.EXAMPLE.COM

What you should see

✅ JAAS login successful …

If you passed a target SPN, a line like
🎟 Acquired service ticket for: HTTP/… (token length: …)
which proves you got a service ticket from the KDC.

You can also target other services, e.g. SQL Server:
MSSQLSvc/dbhost.corp.example.com:1433@CORP.EXAMPLE.COM

7. (Optional) Real JDBC test to SQL Server with Kerberos

If you want to go one step further and actually connect:

Download the Microsoft JDBC Driver for SQL Server and put its JAR on your classpath.

Create SqlKerbTest.java:

import javax.security.auth.login.LoginContext;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class SqlKerbTest {
public static void main(String[] args) throws Exception {
String jaasProfile = (args.length > 0) ? args[0] : "KerberosUser";
String url = "jdbc:sqlserver://dbhost.corp.example.com:1433;" + "databaseName=YourDb;" + "integratedSecurity=true;" + "authenticationScheme=JavaKerberos";

        LoginContext lc = new LoginContext(jaasProfile);
        lc.login();
        System.out.println("✅ JAAS login successful for JDBC");

        try (Connection conn = DriverManager.getConnection(url);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT SYSTEM_USER, SUSER_SNAME()")) {
            while (rs.next()) {
                System.out.println("Connected as: " + rs.getString(1) + " / " + rs.getString(2));
            }
        }
    }

}

Run (adjust paths and add the JDBC jar to classpath):

javac -cp .;mssql-jdbc-\*.jar SqlKerbTest.java

java ^
-cp .;mssql-jdbc-\*.jar ^
-Djava.security.auth.login.config=C:\dev\kerberos\jaas.conf ^
-Djava.security.krb5.conf=C:\dev\kerberos\krb5.ini ^
-Djavax.security.auth.useSubjectCredsOnly=false ^
-Dsun.security.krb5.debug=true ^
SqlKerbTest KerberosUser

8. Troubleshooting cheatsheet

KDC has no support for encryption type
Keytab/realm uses ciphers your KDC doesn’t allow → regenerate keytab (AES256 or AES128), enable matching enctypes in AD.

Clock skew too great
Sync Windows time with the DC.

Pre-authentication failed / Integrity check failed
Wrong password in keytab or wrong principal → regenerate keytab or fix principal.

Cannot find default realm / Cannot locate KDC
Bad krb5.ini or DNS. Verify dc01 resolves and is reachable on UDP/TCP 88.

No valid credentials provided (Mechanism level: Failed to find any Kerberos tgt)
Using KerberosUser without a TGT. Run kinit or ensure you’re logged into the domain and klist shows a TGT.

Still failing? Turn on debug
You already have -Dsun.security.krb5.debug=true. Read the trace—look for the realm, KDC, and chosen enctypes.
