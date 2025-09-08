# Kerberos Authentication for SQL Server in WebSphere (WAS)

This document explains how to configure Kerberos authentication so a WebSphere Application Server (WAS) DataSource connects to Microsoft SQL Server using Kerberos (no DB password stored). Place this file in the project directory to download or open from your IDE.

## Defaults used in examples

- AD realm: CORP.EXAMPLE.COM
- SQL host: dbhost.corp.example.com:1433
- SQL service account: SQLsvc@CORP.EXAMPLE.COM
- Keytab on WAS: /opt/IBM/WebSphere/keytabs/sqlsvc.keytab
- JAAS alias (WAS): KerberosUser
- JNDI DataSource: jdbc/YourDb
- WAS server OS: Linux (adjust for AIX/Windows)

## Quick checklist

1. Register SPN for SQL service account and create keytab.
2. Copy keytab to WAS and set secure permissions.
3. Create krb5.conf on WAS.
4. Create jaas.conf and/or JAAS System Login in WAS.
5. Add JVM properties in WAS pointing to jaas.conf and realm/KDC.
6. Create J2C auth alias (optional) and configure DataSource custom properties.
7. Restart WAS and test.

## 1) AD / SQL Server — SPN & keytab (example AD admin commands)

On AD admin host (Windows):

- Register SPN:
  setspn -A MSSQLSvc/dbhost.corp.example.com:1433 SQLsvc
- Create keytab (example, adjust crypto per domain policy):
  ktpass -princ MSSQLSvc/dbhost.corp.example.com@CORP.EXAMPLE.COM -mapuser SQLsvc -pass TemporaryPassword! -out sqlsvc.keytab -ptype KRB5_NT_PRINCIPAL -crypto AES256-SHA1
- Securely copy sqlsvc.keytab to WAS: /opt/IBM/WebSphere/keytabs/sqlsvc.keytab

Verify:

- setspn -L SQLsvc

## 2) krb5.conf (place in /etc/krb5.conf or OS-specific location)

Example:

```
[libdefaults]
  default_realm = CORP.EXAMPLE.COM
  dns_lookup_kdc = false

[realms]
  CORP.EXAMPLE.COM = {
    kdc = kdc.corp.example.com
  }

[domain_realm]
  .corp.example.com = CORP.EXAMPLE.COM
  corp.example.com = CORP.EXAMPLE.COM
```

## 3) jaas.conf (example path: /opt/IBM/WebSphere/jaas.conf)

Example entry:

```
KerberosUser {
  com.ibm.security.auth.module.Krb5LoginModule required
    useKeytab=true
    keyTab="/opt/IBM/WebSphere/keytabs/sqlsvc.keytab"
    principal="SQLsvc@CORP.EXAMPLE.COM"
    storeKey=true
    doNotPrompt=true
    debug=true;
};
```

## 4) WAS Admin Console settings (high level)

- Security → Global security → Authentication mechanisms → Kerberos authentication
  - Enable Kerberos, set realm and KDC.
- Security → Global security → Authentication → JAAS - System logins
  - Create alias `KerberosUser` with module `com.ibm.security.auth.module.Krb5LoginModule` and properties matching jaas.conf.
- Security → Global security → Authentication → J2C authentication data
  - Create alias `SqlServerKerberosAlias` (User ID: SQLsvc@CORP.EXAMPLE.COM).
- Servers → Server Types → WebSphere application servers → server1 → Process definition → Java Virtual Machine → Custom properties
  - java.security.krb5.realm = CORP.EXAMPLE.COM
  - java.security.krb5.kdc = kdc.corp.example.com
  - java.security.auth.login.config = /opt/IBM/WebSphere/jaas.conf
  - (optional) sun.security.krb5.debug = true
- Resources → JDBC → Data sources → [YourDataSource]
  - JNDI name: jdbc/YourDb
  - Data store helper class: com.ibm.websphere.rsadapter.MicrosoftSQLServerDataStoreHelper
  - Security: Component-managed/Container-managed → Authentication alias: SqlServerKerberosAlias
  - Custom properties: serverName=dbhost.corp.example.com, portNumber=1433, databaseName=YourDb, integratedSecurity=true, authenticationScheme=JavaKerberos

Save, restart the server if needed.

## 5) File permissions

- chown to WAS OS user and chmod 600 the keytab:
  sudo chown wasuser:wasgroup /opt/IBM/WebSphere/keytabs/sqlsvc.keytab
  sudo chmod 600 /opt/IBM/WebSphere/keytabs/sqlsvc.keytab

## 6) Test & debug

On WAS host:

- kinit -k -t /opt/IBM/WebSphere/keytabs/sqlsvc.keytab SQLsvc@CORP.EXAMPLE.COM
- klist
  Check WAS logs: SystemOut.log and SystemErr.log for Kerberos messages.

## 7) Java sample (use container DataSource)

Place or update your app to use JNDI DataSource:

```
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class SqlKerbTest {
  public static void main(String[] args) throws Exception {
    InitialContext ctx = new InitialContext();
    DataSource ds = (DataSource) ctx.lookup("jdbc/YourDb");
    try (Connection conn = ds.getConnection();
         Statement st = conn.createStatement();
         ResultSet rs = st.executeQuery("SELECT SYSTEM_USER, SUSER_SNAME()")) {
      while (rs.next()) {
        System.out.println("Connected as: " + rs.getString(1) + " / " + rs.getString(2));
      }
    }
  }
}
```

## 8) Common issues

- SPN mismatch or missing SPN
- Keytab principal differs from SPN
- DNS forward/reverse mismatch
- Clock skew (>5 minutes)
- Wrong JAAS alias or missing java.security.auth.login.config

If you want, confirm values (realm, SQL host, service account, keytab path, JAAS alias, JNDI name, WAS OS/user) and I will generate exact ktpass/setspn commands, krb5.conf, jaas.conf, WAS JVM properties and a ready-to-save
