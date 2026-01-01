# Tomcat 9 / WebSphere tWAS 9 + Angular SPA Sample (no Node.js on server)

This project demonstrates the standard pattern for hosting an Angular (or any SPA) on a Java servlet container:

- Node.js is used only to **build** Angular on your laptop/CI.
- Tomcat serves only the **built static files** (HTML/CSS/JS).
- Deep links (client-side routes) work because the server returns the SPA entry (`index.html`).

This repo includes a real Angular app (`angular-web`) and a WAR (`tomcat-spa-sample`) that can be built for:

- Tomcat 9.x (bundles Jersey for JAX-RS)
- WebSphere traditional (tWAS) 9.0.5.x (uses container-provided JAX-RS; does not bundle Jersey)

## Software requirements

**Build machine (dev/CI):**

- Node.js + npm (recommended: Node LTS 20/22)
- Java 11+
- Maven 3.9+

**Server (Tomcat machine):**

- Java 11+
- Tomcat 9.x
- No Node.js needed on the server

> This WAR is configured for Tomcat 9 (`javax.servlet`).
> If you want Tomcat 10.1+ instead, the code/dependencies must use `jakarta.servlet`.

## Build targets (Tomcat vs tWAS)

This repo produces two WARs via Maven profiles:

- **Tomcat WAR (default)**: bundles Jersey and wires JAX-RS in `web.xml`.
- **Stable WAR name**: `target/tomcat-spa-sample.war` (keeps the Tomcat context path as `/tomcat-spa-sample`)
	- Also copied to: `target/tomcat-spa-sample-tomcat.war`
- **tWAS WAR**: does not bundle Jersey; relies on container JAX-RS activated by `@ApplicationPath("/api-jaxrs")`.
	- Built as: `target/tomcat-spa-sample.war`
	- Also copied to: `target/tomcat-spa-sample-twas.war`

If you deploy the Tomcat WAR to tWAS, you may hit JAX-RS provider/classloader conflicts (because tWAS already provides JAX-RS).

## End-to-end: build Angular → package WAR → deploy to Tomcat 9

### 0) Set `CATALINA_HOME`

For Homebrew Tomcat 9, the most reliable way is:

```bash
export CATALINA_HOME="$(brew --prefix tomcat@9)/libexec"
```

Verify:

```bash
"$CATALINA_HOME/bin/catalina.sh" version
```

### 1) Build the Angular app

```bash
cd tomcat-spa-sample/angular-web
npm install

# Build for a Tomcat context path + servlet mapping (/tomcat-spa-sample/app/)
npx ng build --configuration production --base-href /tomcat-spa-sample/app/
```

Angular output folder:

- `angular-web/dist/angular-web/browser/`

### 2) Copy the Angular build into the WAR (under WEB-INF)

You asked to place the Angular build under `WEB-INF`. Files under `WEB-INF` are not publicly accessible by default, so this WAR includes a servlet mapping that serves the Angular build at:

- `/tomcat-spa-sample/app/*`

Copy the files using the helper script:

```bash
cd tomcat-spa-sample
./tools/deploy-angular-dist-webinf.sh
```

Manual copy (equivalent):

- From: `angular-web/dist/angular-web/browser/`
- To: `src/main/webapp/WEB-INF/app/`

Note: `src/main/webapp/WEB-INF/app/` is **generated output**. In a source-only checkout it will be empty (kept via a `.gitkeep`), and the deploy script will populate it.

### 3) Build the WAR

```bash
cd tomcat-spa-sample
mvn clean package
```

WAR output:

- `target/tomcat-spa-sample.war`
- `target/tomcat-spa-sample-tomcat.war`

To build the **tWAS** variant instead:

```bash
cd tomcat-spa-sample
mvn -Ptwas clean package
```

tWAS WAR output:

- `target/tomcat-spa-sample.war`
- `target/tomcat-spa-sample-twas.war`

### 4) Deploy to Tomcat 9

```bash
# Stop Tomcat (recommended)
"$CATALINA_HOME/bin/catalina.sh" stop

# Remove old deployment (prevents stale files)
rm -rf "$CATALINA_HOME/webapps/tomcat-spa-sample" \
	"$CATALINA_HOME/webapps/tomcat-spa-sample.war"

# Deploy new WAR
cp target/tomcat-spa-sample-tomcat.war "$CATALINA_HOME/webapps/tomcat-spa-sample.war"

# Start Tomcat
"$CATALINA_HOME/bin/catalina.sh" start
```

### 5) Run / verify

Open:

- `http://localhost:8080/tomcat-spa-sample/app/`

Deep links should also work:

- `http://localhost:8080/tomcat-spa-sample/app/employees`
- `http://localhost:8080/tomcat-spa-sample/app/summary`

API endpoints (same on Tomcat and tWAS):

- Employees (servlet JSON API): `/api/employees`
- Students (JAX-RS API): `/api-jaxrs/students`

## Deploy to WebSphere tWAS 9.0.5.x

- Build the tWAS WAR: `mvn -Ptwas clean package`
- Deploy: `target/tomcat-spa-sample-twas.war`
- Ensure your Angular `base-href` matches your configured context root + `/app/`.

Note on persistence: the embedded H2 database is stored under `${catalina.base}/h2/` when `catalina.base` is present; otherwise it uses `${java.io.tmpdir}/tomcat-spa-sample/h2/`.

### Angular app source

The Angular source lives here:

- [angular-web/README.md](angular-web/README.md)

## Troubleshooting

- **Blank page / 404 for JS/CSS**: your Angular `base-href` is wrong.
	- If you deploy under `/tomcat-spa-sample/app/`, build with:
		- `npx ng build --configuration production --base-href /tomcat-spa-sample/app/`
- **Deep-link refresh returns 404**: make sure you are using the `/app/*` URL.
	- Example: `http://localhost:8080/tomcat-spa-sample/app/summary`
- **Tomcat 10+ errors (`ClassNotFoundException: javax.servlet...`)**: you’re on Tomcat 10.1+.
	- Use a `jakarta.servlet` version of the WAR instead.

- **Tomcat 9 won’t start (ports 8080/8005 already in use)**: run an isolated Tomcat 9 on different ports.
	- Build + deploy as usual (steps 1–3), then run:
		- `./tools/run-tomcat9-isolated.sh`
	- Open:
		- `http://localhost:9080/tomcat-spa-sample/app/`

## Why Node.js is not needed on the server

Tomcat only needs to serve static files and provide an `index.html` fallback for client-side routes.
Node.js is only required to *compile/bundle* Angular source code into those static files.



commands:
cd /Users/sureshselvaraj/SourceCode/myref/tomcat-spa-sample/angular-web && npm run -s build
cd /Users/sureshselvaraj/SourceCode/myref/tomcat-spa-sample && ./tools/deploy-angular-dist-webinf.sh
./tools/mvn -U -DskipTests clean package
./tools/stop-tomcat9-isolated.sh || true
./tools/run-tomcat9-isolated.sh