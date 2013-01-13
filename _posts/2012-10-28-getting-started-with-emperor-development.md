---
layout: post
title: Getting Started With Emperor Development
description: emperor getting started
---

## Scala?

Emperor is written in [Scala](http://www.scala-lang.org/). Scala is becoming
increasingly popular, as evidenced by ThoughtWork's
[October Technology Report](http://www.thoughtworks.com/articles/technology-radar-october-2012)
in which Scala was promoted to "Adopt". Add this to it's popularity with many
<a href="http://www.scala-lang.org/node/1658">internet companies</a> and you've
got a pretty strong case for using Scala.

But lots of people are still unfamiliar with Scala.  This post will serve as
an introduction to Scala for new users and will be oriented toward helping
those interested working on Emperor get their start.

## Scope

This post assumes you are familiar with web development in other languages and
it describes how to get set up to work on Emperor. Other Scala projects
may require different instructions and the Play framework mentioned below is
only applicable for Play projects like Emperor.

## What Is Scala?

Scala is a programming language that is built on top of Java.  It blends
[familiar imperative programming](http://en.wikipedia.org/wiki/Imperative_programming)
with increasingly popular [functional programming](http://en.wikipedia.org/wiki/Functional_programming)
features.  It compiles to normal [Java bytecode](http://en.wikipedia.org/wiki/Java_bytecode)
and works with existing Java libraries.  This combination of features gives
Scala a rich future as more programmers adopt functional programming and a huge
body of existing work to draw from in the form of existing Java code and tools.

## Getting The Toolchain

### Java

First you'll need to make sure you have Java
installed.  You can use either's [Oracle's Java](http://www.java.com/en/download/index.jsp)
or [OpenJDK](http://openjdk.java.net/) which is easily available for Debian and
Ubuntu users.  You will need Java 6 or better.

### SBT

SBT is the [Simple Build Tool](http://www.scala-sbt.org/). You can read it's
[extensive documentation](http://www.scala-sbt.org/release/docs/Getting-Started/Welcome.html)
or skip directly to the [instructions for setting up SBT](http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html).

## Play

With SBT set up and in your path you are ready set up play. [The Play framework](http://www.playframework.org/)
is a lightweight, stateless Scala (and Java) web framework. [Download and install the Play framework](http://www.playframework.org/documentation/2.0.4/Installing)
and you are ready to get started!

## MySQL

You'll need a MySQL instance. I assume that anyone reading this knows how to handle that task.  So we'll skip to creating the database:

{% highlight sql %}
CREATE DATABASE emperor CHARACTER SET utf8 COLLATE utf8_unicode_ci;
{% endhighlight %}

## Check Out Emperor

Clone the <a href="http://git-scm.com/">Git</a> repository from <a href="https://github.com/gphat/emperor/">GitHub</a>:

{% highlight sh %}
git clone https://github.com/gphat/emperor.git
{% endhighlight %}

## Running Emperor

### Starting Up

You are now ready to run Emperor!  From within the repository checkout created
above run the command `play`.  This will start the Play shell which should look
something like this:

<img style="border: 1px solid #ccc; border-radius: 3px; margin-bottom: 1em" src="http://f.cl.ly/items/131L2D0h3j3j3k1Q3E0k/Screen%20Shot%202012-10-28%20at%204.22.23%20PM.png">

From inside the shell, type `run`.  Some dependencies will need to be fetched
then &mdash; after a few seconds &mdash; you should be greeted with a message
stating that your server is ready on at <a href="http://localhost:9000/">http://localhost:9000</a>.

<img style="border: 1px solid #ccc; border-radius: 3px; margin-bottom: 1em" src="http://cl.ly/image/3B3r313o4045/Screen%20Shot%202012-10-28%20at%204.23.18%20PM.png">

### Populating The Database

When you visit the aforementioned URL you should be prompted to apply
evolutions to your database.  This is the Play framework's
<a href="http://www.playframework.org/documentation/2.0/Evolutions">mechanism for tracking and apply database changes</a>.
Clicking apply will populate your database schema and create the default admin
user. It will take a few seconds for the results to show up, as the application
will be compiled on the fly.  You should now be greeted with a login screen.

<img style="border: 1px solid #ccc; border-radius: 3px; margin-bottom: 1em" src="http://f.cl.ly/items/3g2p0P2y2y030z183m3j/Screen%20Shot%202012-10-28%20at%204.36.41%20PM.png">

The default login is username **admin** and password **testing**.

## Conclusion&hellip; For Now

You have a running instance of Emperor and ready to start developing!  In the
next post we'll cover how to make changes.  In the meantime feel free to read
the [Play framework documentation](http://www.playframework.org/documentation/2.0.4/Home)
or [Emperor's documentation](https://emperorapp.atlassian.net/wiki/display/EMP/Emperor+Home).