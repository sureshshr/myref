# Java Kerberos Authentication Test on Windows

This guide explains how to test Kerberos authentication in a simple Java program on a local Windows machine.

---

## 0) Prerequisites
1. Machine is joined to Active Directory:
   ```cmd
   systeminfo | findstr /B /C:"Domain"
   ```
2. Time is synchronized with the domain controller (≤5 min skew)
3. DNS resolves the domain controller (DC)
4. Java installed (JDK 8+):
   ```cmd
   java -version
   javac -version
   ```

---

## 1) Kerberos Configuration
### Default Path:
Create `C:\Windows\krb5.ini`:
```ini
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
```

### Custom Path:
If not using `C:\Windows`, store `krb5.ini` elsewhere and set JVM property:
```
-Djava.security.krb5.conf=C:\path\to\krb5.ini
```

---

## 2) Authentication Method
### A) Using Keytab (service principal)
- Keytab: `C:\dev\kerberos\was.keytab`
- Principal: `HTTP/waswin01.corp.example.com@CORP.EXAMPLE.COM`

### B) Using User Ticket (no keytab)
1. Ensure you have a ticket:
   ```cmd
   klist
   ```
2. If no ticket, install MIT Kerberos and run:
   ```cmd
   kinit YOURUSER@CORP.EXAMPLE.COM
   ```

---

## 3) JAAS Configuration
Create `C:\dev\kerberos\jaas.conf`:
```ini
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

KerberosUser {
  com.sun.security.auth.module.Krb5LoginModule required
  useTicketCache=true
  renewTGT=false
  doNotPrompt=true
  isInitiator=true
  debug=true;
}
```

---

## 4) Java Test Program
`KerberosTest.java`:
```java
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.security.PrivilegedAction;
import org.ietf.jgss.*;

public class KerberosTest {
    public static void main(String[] args) {
        String profile = (args.length > 0) ? args[0] : "KerberosUser";
        String targetSpn = (args.length > 1) ? args[1] : null;
        try {
            LoginContext lc = new LoginContext(profile);
            lc.login();
            System.out.println("✅ JAAS login successful with profile: " + profile);
            Subject subject = lc.getSubject();
            System.out.println("Subject: " + subject);

            if (targetSpn != null) {
                Subject.doAs(subject, (PrivilegedAction<Void>) () -> {
                    try {
                        GSSManager mgr = GSSManager.getInstance();
                        GSSName serverName = mgr.createName(targetSpn, GSSName.NT_HOSTBASED_SERVICE);
                        Oid krb5Oid = new Oid("1.2.840.113554.1.2.2");
                        GSSContext ctx = mgr.createContext(serverName, krb5Oid, null, GSSContext.DEFAULT_LIFETIME);
                        ctx.requestMutualAuth(true);
                        ctx.requestConf(true);
                        ctx.requestInteg(true);
                        byte[] token = ctx.initSecContext(new byte[0], 0, 0);
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
```

---

## 5) Compile & Run
```cmd
javac KerberosTest.java
java ^
 -Djava.security.auth.login.config=C:\dev\kerberos\jaas.conf ^
 -Djava.security.krb5.conf=C:\dev\kerberos\krb5.ini ^
 -Djavax.security.auth.useSubjectCredsOnly=false ^
 -Dsun.security.krb5.debug=true ^
 KerberosTest KerberosUser HTTP/waswin01.corp.example.com@CORP.EXAMPLE.COM
```

- Use `KerberosKeytab` profile if using a keytab.  
- Success output: `✅ JAAS login successful` and optionally service ticket info.

---

## 6) Troubleshooting
- **Clock skew** → sync time with DC
- **Cannot contact KDC** → check DNS/firewall
- **Pre-authentication failed** → wrong password or keytab
- **No valid credentials** → run `kinit` or check `klist`

✅ Minimal Kerberos test in Java on Windows complete.
