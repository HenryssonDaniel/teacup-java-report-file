# [User Guide](https://henryssondaniel.github.io/teacup.github.io/)
Java **Te**sting Fr**a**mework for **C**omm**u**nication **P**rotocols and Web Services with file

[![Build Status](https://travis-ci.com/HenryssonDaniel/teacup-java-report-file.svg?branch=master)](https://travis-ci.com/HenryssonDaniel/teacup-java-report-file)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=HenryssonDaniel_teacup-java-report-file&metric=coverage)](https://sonarcloud.io/dashboard?id=HenryssonDaniel_teacup-java-report-file)
[![latest release](https://img.shields.io/badge/release%20notes-1.0.1-yellow.svg)](https://github.com/HenryssonDaniel/teacup-java-report-file/blob/master/doc/release-notes/official.md)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.henryssondaniel.teacup.report/file.svg)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22io.github.henryssondaniel.teacup.report%22%20AND%20a%3A%22file%22)
[![Javadocs](https://www.javadoc.io/badge/io.github.henryssondaniel.teacup.report/file.svg)](https://www.javadoc.io/doc/io.github.henryssondaniel.teacup.report/file)
## What ##
This project makes it possible to save logs on disc rather than just publish on the screen.
## Why ##
Save the logs on disc so that they are not deleted after each test execution, no matter what test engine you are using.
## How ##
Follow the steps below:
1. Add this repository as a dependency
1. Create a file named teacup.properties in a folder named .teacup in your home folder.
1. Add reporter=io.githb.henryssondaniel.teacup.report.file.DefaultReporter to the file