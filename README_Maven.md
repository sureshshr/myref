# Eclipse to Maven Project Conversion

This guide helps convert an Eclipse-based Java project into a Maven-based project, validate the setup, and refactor the codebase using GitHub Copilot in Agent Mode.

---

## 🚀 Objective

Convert an existing Eclipse Java project to a Maven project using GitHub Copilot Agent Mode, validate the conversion, and refactor code as needed.

---

## 🧰 Prerequisites

- Eclipse-based Java project
- [VS Code](https://code.visualstudio.com/) installed
- GitHub Copilot and GitHub Copilot Chat extensions installed
- GitHub Copilot subscription
- Java 8 or 17 installed
- Maven installed

---

## 🧠 Prompt to Use in Copilot Chat

Step 1: Convert Eclipse Project to Maven
Create a pom.xml file in the root directory.

Move source code to Maven-standard folder structure:

src/main/java/

src/main/resources/

src/test/java/ (if applicable)

Add appropriate groupId, artifactId, and version to pom.xml.

Add required dependencies (convert from .jar references in .classpath).

If there is a lib folder with jars, suggest Maven equivalents or use system scope (temporary).

Step 2: Validate Project Build
Run: mvn clean compile

Run: mvn test (if there are unit tests)

Fix any errors due to dependency issues or misplaced packages.

Step 3: Refactor Code (if required)
Check for hardcoded paths or imports relying on Eclipse-specific settings.

Clean up or remove .classpath, .project, and other Eclipse-specific configs (after migration).

Update code to use Maven plugins (e.g., for builds, testing, packaging).

Suggest replacing deprecated libraries with updated Maven-central-supported versions.

Step 4: IDE Integration
Add .mvn wrapper (optional): mvn -N io.takari:maven:wrapper

Re-import the project in Eclipse or IntelliJ as a Maven project.

Ensure the project builds correctly in the IDE and command line.

Step 5: Final Verification
Confirm target/ directory is created with compiled classes after build.

Verify JAR or WAR packaging as expected.

Test full functionality after migration.

Validate if mvn install works successfully without IDE help.

## ✅ Final Checklist

- [x] `pom.xml` created and configured
- [x] Source code moved to Maven layout
- [x] Dependencies added to `pom.xml`
- [x] Eclipse configs removed
- [x] Build validated using `mvn compile`
- [x] Code refactored for compatibility

---

## 📂 Recommended .gitignore

```
target/
*.iml
.classpath
.project
.settings/
```

---

## 🧪 Bonus

- Add `mvn wrapper`: `mvn -N io.takari:maven:wrapper`
- Use `maven-compiler-plugin` to set Java version
- Use `maven-shade-plugin` for creating fat JARs

---

## 💬 Need Help?

Ask Copilot:

> "Why is my Maven build failing with this error?"

Or

> "Suggest Maven plugin for JAR packaging."

---
