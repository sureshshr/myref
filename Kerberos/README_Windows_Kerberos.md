# Kerberos Setup for WebSphere on Windows

This guide explains how to configure **Kerberos authentication** for
WebSphere Application Server (WAS) on a **Windows host**.

------------------------------------------------------------------------

## 1. Prerequisites

-   WebSphere Application Server installed (e.g.,
    `C:\IBM\WebSphere\AppServer`)
-   Service account created in Active Directory (e.g.,
    `svc_wasapp01@CORP.EXAMPLE.COM`)
-   SPN (Service Principal Name) registered for the WAS host
-   Keytab generated from Active Directory Domain Controller
-   Windows host joined to the domain (or can resolve AD DNS)

------------------------------------------------------------------------

## 2. Generate Keytab on Domain Controller

Run on AD server as Domain Admin:

``` powershell
ktpass -princ HTTP/waswin01.corp.example.com@CORP.EXAMPLE.COM ^
       -mapuser CORP\svc_wasapp01 ^
       -crypto AES256-SHA1 ^
       -ptype KRB5_NT_PRINCIPAL ^
       -pass <StrongPasswordHere> ^
       -out C:\temp\waswin01.keytab
```

> Replace `waswin01.corp.example.com` with your WAS host FQDN.\
> If AES256 is unsupported, try `AES128-SHA1` or `RC4-HMAC-NT`.

------------------------------------------------------------------------

## 3. Transfer Keytab to WAS Host

Copy the file to the WAS host, e.g.:

    C:\IBM\WebSphere\AppServer\etc\waswin01.keytab

Restrict file permissions: - Right-click → **Properties → Security** -
Allow read access only to WAS service account and Administrators

------------------------------------------------------------------------

## 4. Configure Kerberos (krb5.ini)

Create `C:\Windows\krb5.ini` (default) or place it elsewhere if you plan
to override:

``` ini
[libdefaults]
  default_realm = CORP.EXAMPLE.COM
  dns_lookup_realm = true
  dns_lookup_kdc = true
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

------------------------------------------------------------------------

## 5. Override krb5.ini Location (Optional)

If not using `C:\Windows\krb5.ini`, configure WAS to use a custom path:

1.  Log in to **WAS Admin Console** →\
    Servers → Server Types → WebSphere application servers →
    `<server_name>`\
    → Java and Process Management → Process Definition → Java Virtual
    Machine → Custom Properties

2.  Add property:

    -   **Name:**

            java.security.krb5.conf

    -   **Value:**

            C:\IBM\WebSphere\AppServer\etc\krb5.ini

3.  Save → Synchronize nodes → Restart WAS

------------------------------------------------------------------------

## 6. Configure WAS for Kerberos

In **WAS Admin Console**:

-   Go to **Security → Global Security → Kerberos**
-   Fill in:
    -   Kerberos realm: `CORP.EXAMPLE.COM`
    -   KDC host: `dc01.corp.example.com`
    -   Kerberos config file: `C:\Windows\krb5.ini` (or custom path)
    -   Keytab file: `C:\IBM\WebSphere\AppServer\etc\waswin01.keytab`
    -   Service principal:
        `HTTP/waswin01.corp.example.com@CORP.EXAMPLE.COM`

Save → Synchronize → Restart WAS.

------------------------------------------------------------------------

## 7. Test Kerberos

From a command prompt **running as the WAS service account**:

``` cmd
kinit -k -t C:\IBM\WebSphere\AppServer\etc\waswin01.keytab HTTP/waswin01.corp.example.com@CORP.EXAMPLE.COM
klist
```

Check WAS **SystemOut.log** to confirm Kerberos login success.

------------------------------------------------------------------------

## 8. Troubleshooting

-   **Clock skew** → Ensure Windows time syncs with domain
-   **Duplicate SPN** → Use `setspn -X` to find duplicates
-   **Wrong crypto** → Regenerate keytab with AES128 or RC4
-   **NTLM fallback** → Ensure SPN and realm are correctly configured

------------------------------------------------------------------------

✅ Kerberos authentication for WAS on Windows is now set up.
