#!/bin/bash
# Shell script to run LiteCam Java Viewer

set -e

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_PATH="$SCRIPT_DIR/litecam.jar"

# Check if JAR file exists
if [ ! -f "$JAR_PATH" ]; then
    echo "Error: litecam.jar not found in $SCRIPT_DIR" >&2
    echo "Please run build-jar.sh first to build the JAR file." >&2
    exit 1
fi

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not found in PATH" >&2
    echo "Please install Java and make sure it's accessible from PATH." >&2
    exit 1
fi

echo "Starting LiteCam Java Viewer..."

# Run the application
java -cp "$JAR_PATH" com.example.litecam.LiteCamViewer "$@"
