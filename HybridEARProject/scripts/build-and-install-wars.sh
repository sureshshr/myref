#!/usr/bin/env bash
set -euo pipefail
# Build Module1 and Module2 with Ant, install WARs to local maven repo, then build EAR
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "Building Module1 with Ant..."
ant -f Module1/build.xml build

echo "Installing Module1 WAR into local Maven repo..."
mvn install:install-file -Dfile=Module1/dist/Module.war \
  -DgroupId=com.example -DartifactId=Module1 -Dversion=1.0.0 -Dpackaging=war -DgeneratePom=true

echo "Building Module2 with Ant..."
ant -f Module2/build.xml build

echo "Installing Module2 WAR into local Maven repo..."
mvn install:install-file -Dfile=Module2/dist/Module.war \
  -DgroupId=com.example -DartifactId=Module2 -Dversion=1.0.0 -Dpackaging=war -DgeneratePom=true

echo "Running mvn package for EAR..."
mvn -U -DskipTests=false package

echo "Done. If mvn package succeeded, the EAR will be in target/"
