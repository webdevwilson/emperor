# Emperor

Emperor is a project management application for software development.

Emperor aims to replicate features of established bug-trackers such as JIRA, FogBugz and others while staying focused and opinionated.

## See It

Please have a look at the [project page](http://gphat.github.com/emperor/).

## Mission Statement

The mission of emperor is to provide project management software that is reasonable to administer, useful for developers and &mdash; perhaps most critical &mdash; visible and informative to non-developers.

## Goals

 * Easy installation and upgrade
 * Focus on UI and UX
 * Relatively simple administration
 * JVM as the only dependency
 * Focus on convenience and readability for non-developer users
 * More?

## Why It Exists

There is a need for a featureful, modern issue tracker and project management systems that is an alternative to heavy, commercial systems.

## Current Features

**Note:** See [feature list](https://github.com/gphat/emperor/blob/master/features.md).

* Multiple Users
* Multiple Projects
* Search
* Ticket Creation
* Ticket History
* Customizable, per-Project Workflow
* Markdown formatting

## Current Pain Points

* UI is a Work In Progress
* Not enough tests

## Planned Features

* Groups & Roles
* Search (with saved searches)
* Plugins
* Email notification
* First-class milestones
* No-nonsense UI with great user experience
* Ticket "stacks"
* Attention flags

# Running Emperor

See the [INSTALL instructions](https://github.com/gphat/emperor/blob/master/INSTALL.md).

# Development Status

Development on Emperor begain in April of 2012 and has been conducted on nights and weekends. It is currently considered to be Alpha quality.

# How To Help

Emperor is in very active development and may change dramatically before a 1.0 release. Feel free to follow this repo, create issues or contact me at \<cory@onemogin.com>.

# Technologies

Emperor is written in [Scala](http://www.scala-lang.org/) and uses the
[Play framework](http://www.playframework.org/).  It is written to work with
MySQL and uses an embedded [ElasticSearch](http://www.elasticsearch.org/)
instance.
