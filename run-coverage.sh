#!/bin/bash

echo "Running tests with coverage..."
mvn clean test

if [ $? -eq 0 ]; then
    echo "Tests completed successfully!"
    echo "Opening coverage report..."
    open target/site/jacoco/index.html
    echo "Coverage report opened in browser"
    echo ""
    echo "Coverage files generated:"
    echo "- HTML Report: target/site/jacoco/index.html"
    echo "- XML Report: target/site/jacoco/jacoco.xml"
    echo "- CSV Report: target/site/jacoco/jacoco.csv"
else
    echo "Tests failed. Please check the output above."
fi