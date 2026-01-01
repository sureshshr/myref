# Angular Employee CRUD (build then deploy to Tomcat)

This is a small Angular app (standalone + router) that supports:

- Employee list
- Create employee
- Edit employee
- Delete employee
- Summary (totals + department counts)

Data is stored in `localStorage` (so it works as a pure static SPA).

## Run locally (dev)

On your dev machine (Node.js required locally):

```bash
cd angular-web
npm ci
npm start
```

## Production build

For Tomcat deployments, **use a relative base href** so bundles load under any context path:

```bash
cd angular-web
npm ci
npx ng build --configuration production --base-href ./
```

Output folder:

- `angular-web/dist/angular-web/browser/`

## Integrate into the Tomcat WAR (manual)

Copy the build output into the Tomcat WAR web root:

- Copy everything from:
  - `angular-web/dist/angular-web/browser/`
- Into:
  - `tomcat-spa-sample/src/main/webapp/`

Then rebuild and deploy the WAR:

```bash
cd tomcat-spa-sample
./tools/mvn clean package
```

The existing SPA fallback filter in the WAR will make deep-link refresh work for Angular routes.
