# Installation

## Install a JDK

JDK 6 is required.  7 should work.

## Install Play framework

(Here are instructions.)[http://www.playframework.org/documentation/2.0.2/Installing]

## Install MySQL

(However is best for your system.)[http://dev.mysql.com/downloads/]

## Create A Database

Create a database called `emperor`.

  CREATE DATABASE emperor CHARACTER SET utf8 COLLATE utf8_unicode_ci;

## Clone Emperor

  git clone https://github.com/gphat/emperor.git

## Start the Application

  cd emperor
  play
  run (after play prompt comes up)

## Setup Emperor

  visit http://127.0.0.1:9000
  Click "apply" when prompted to populate your database

## Log In

Login as the user 'admin' with the password 'test'.
