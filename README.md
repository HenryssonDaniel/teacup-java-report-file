# teacup-java-report-file
Java **Te**sting Fr**a**mework for **C**omm**u**nication **P**rotocols and Web Services with file

[![Build Status](https://travis-ci.com/HenryssonDaniel/teacup-java-report-file.svg?branch=master)](https://travis-ci.com/HenryssonDaniel/teacup-java-report-file)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=HenryssonDaniel_teacup-java-report-file&metric=coverage)](https://sonarcloud.io/dashboard?id=HenryssonDaniel_teacup-java-report-file)
## What ##
This project makes it possible to save logs on disc rather than just publish on the screen.
## Why ##
Save the logs on disc so that they are not deleted after each test execution, no matter what test engine you are using.
## How ##
Follow the steps below:
1. Add this repository as a dependency
1. Create a file named teacup.properties in a folder named .teacup in your home folder.
1. Add reporter=io.githb.henryssondaniel.teacup.report.file.DefaultReporter to the file