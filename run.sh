#!/usr/bin/env bash
# Equivalent de run.bat per a Linux/macOS: executa el Main del projecte.
# Cal haver compilat abans (mvn compile). Ús: bash run.sh
set -e
cd "$(dirname "$0")/ac1-app"
java -Xmx2g -cp ./target/classes:../ac1-cerca/target/classes:../ac1-model/target/classes edu.epsevg.prop.ac1.Main
