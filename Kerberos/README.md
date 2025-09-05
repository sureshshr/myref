# Kerberos Sample Java Project

This repository contains a minimal Java (Maven) sample that demonstrates a JAAS-based Kerberos login flow and a small JUnit test. It does not contact any specific service by default; it shows how to configure the JVM and JAAS to obtain Kerberos credentials and run code as the authenticated Subject. The project targets Java 8.

Contents

- `pom.xml` — Maven project file
- `src/main/java/com/example/kerberos/KerberosClient.java` — simple client that uses JAAS LoginContext
- `src/main/java/com/example/kerberos/HttpKerberosClient.java` — minimal HTTP SPNEGO client using JGSS and HttpURLConnection
- `src/test/java/com/example/kerberos/KerberosClientTest.java` — JUnit test that will run only when Kerberos is configured
- `src/main/resources/krb5.conf.example` — example krb5.conf
- `src/main/resources/jaas.conf.example` — example JAAS login config

Quick contract

- Input: JVM system properties and environment similar to a Kerberos-enabled host (see sections below)
- Output: Successful JAAS login and execution of a trivial privileged action
- Success criteria: `KerberosClient.loginAndRun()` returns `"authenticated"`

Edge cases

- No krb5/JAAS configured -> test skipped (safe)
- Incorrect credentials or missing ticket cache -> LoginException
- Keytab vs ticket cache differences covered in examples

Setup for another environment

1.  Install Kerberos client tools (MIT Kerberos or Heimdal) on the host.

2.  Prepare `krb5.conf` for your realm. Copy `src/main/resources/krb5.conf.example` to a system path and edit to match your KDC and realm. Typical locations:

    - Linux/macOS: `/etc/krb5.conf` or `$KRB5_CONFIG`
    - Windows: `%WINDIR%\krb5.ini`

3.  Prepare JAAS config. Copy `src/main/resources/jaas.conf.example` to a file, update the `principal` entry if using keytab or change options accordingly. Example file: `~/jaas.conf`.

4.  Obtain credentials on the host. Two common options:

    a) Use kinit to populate the ticket cache:

        kinit user@EXAMPLE.COM

    b) Use a keytab and set JAAS to use the keytab by editing `jaas.conf` (set `useKeyTab=true` and `keyTab="/path/to/keytab"`).

Running the sample locally (no Kerberos)

You can build the project without a Kerberos environment. The unit test is skipped unless you enable the property.

mvn clean package -DskipTests

Running the Kerberos test in an environment with Kerberos

1. Ensure `krb5.conf` is available and points to your KDC.
2. Ensure you have a valid ticket (e.g., `kinit user@REALM`) or a keytab.
3. Provide the JAAS config file and set JVM properties when running tests.

Example command (uses ticket cache):

```bash
mvn test -Dkerberos.test=true \
   -Djava.security.auth.login.config=/full/path/to/jaas.conf \
   -Djava.security.krb5.conf=/full/path/to/krb5.conf
```

If using a keytab, update `jaas.conf` with `useKeyTab=true` and `keyTab="/path/to/keytab"`, and ensure `doNotPrompt=true`.

HTTP SPNEGO example

If you have an HTTP endpoint protected with SPNEGO (Negotiate), you can run the `HttpKerberosClient` test. Requirements:

- Valid Kerberos ticket or keytab
- Proper `krb5.conf` and `jaas.conf`
- The service principal for the HTTP endpoint (typically `HTTP/host@REALM`)

Example command to run the HTTP test (replace values):

```bash
mvn test -Dkerberos.http.test=true \
   -Dkerberos.http.target=https://kerberized.example.com/protected/resource \
   -Dkerberos.http.service=HTTP/kerberized.example.com@EXAMPLE.COM \
   -Djava.security.auth.login.config=/full/path/to/jaas.conf \
   -Djava.security.krb5.conf=/full/path/to/krb5.conf \
   -Dsun.security.krb5.debug=true
```

Notes and troubleshooting

- If you get "Login failed" check the test log for stack traces. Typical issues are incorrect realm, wrong principal, no ticket cache, or missing permissions to read keytab.
- To get verbose Kerberos/JAAS logs, enable `debug=true` in the JAAS config and add `-Dsun.security.krb5.debug=true` to the JVM.
- macOS: system keychain or credentials may interfere; prefer using the MIT Kerberos distribution.

Next steps (optional enhancements)

- Add an integration example calling an actual Kerberized service (HTTP service with SPNEGO, HDFS, or Kafka).
- Add a Dockerfile that sets up a small KDC (for CI) and runs the test against it.

SQL Server Kerberos connectivity

This project now includes a `SqlServerKerberosClient` that demonstrates connecting to Microsoft SQL Server using Kerberos authentication.

Windows (Integrated Authentication)

- On Windows, the Microsoft JDBC driver supports integrated authentication with `integratedSecurity=true` in the JDBC URL. You'll need the appropriate native library (e.g., `sqljdbc_auth.dll`) on the Java library path.

Example JDBC URL on Windows:

```
jdbc:sqlserver://dbserver.example.com:1433;databaseName=master;integratedSecurity=true
```

Run the test (Windows):

```bash
mvn test -Dkerberos.sql.test=true \
   -Dkerberos.sql.url="jdbc:sqlserver://dbserver.example.com:1433;databaseName=master;integratedSecurity=true" \
   -Djava.security.auth.login.config=/full/path/to/jaas.conf
```

Linux (Kerberos via JAAS/keytab)

- On Linux, configure `/etc/krb5.conf`, create a keytab for the service principal, and configure `jaas.conf` to use the keytab (useKeyTab=true and keyTab="/path/to/keytab"). Ensure the JDBC driver is the version that supports Kerberos (the included `mssql-jdbc` JAR supports it for Java 8).

Example JDBC URL on Linux (KERBEROS):

```
jdbc:sqlserver://dbserver.example.com:1433;databaseName=master;authentication=JavaKerberos
```

Run the test (Linux):

```bash
mvn test -Dkerberos.sql.test=true \
   -Dkerberos.sql.url="jdbc:sqlserver://dbserver.example.com:1433;databaseName=master;authentication=JavaKerberos" \
   -Djava.security.auth.login.config=/full/path/to/jaas.conf \
   -Djava.security.krb5.conf=/full/path/to/krb5.conf
```

Notes

- You may need to set `-Djava.library.path` (Windows) or provide the Kerberos native libs if required by the driver.
- Enable `-Dsun.security.krb5.debug=true` for verbose Kerberos debug output.
- The tests are gated with `-Dkerberos.sql.test=true` to avoid failing on machines without Kerberos.

JavaKerberos (recommended cross-platform approach)

If you want to avoid native DLLs and use Java GSS (recommended for Linux and CI), use the `authentication=JavaKerberos` JDBC mode and a JAAS keytab entry. Steps:

1. Create a keytab for the principal that will authenticate to SQL Server (your AD admin will provide this). The SQL Server service principal should be created and mapped appropriately.

2. Update `jaas.conf` with a keytab entry (see `src/main/resources/jaas.conf.example`, the `KrbLoginKeytab` section).

3. Run tests or application with the following JVM properties:

```bash
mvn test -Dkerberos.sql.test=true \
   -Dkerberos.sql.url="jdbc:sqlserver://dbserver.example.com:1433;databaseName=master;authentication=JavaKerberos" \
   -Djava.security.auth.login.config=/full/path/to/jaas.conf \
   -Djava.security.krb5.conf=/full/path/to/krb5.conf \
   -Dsun.security.krb5.debug=true
```

4. If your application runs standalone (not via Maven test), provide the same JVM system properties and ensure the keytab file is readable by the process user.
