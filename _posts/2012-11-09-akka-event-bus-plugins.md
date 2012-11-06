---
layout: post
title: Emperor's Akka EventBus Plugins
published: false
---

Issue Tracking and Project Management are never quite the same in any two
organizations. Therefore to meet the needs of all these teams it's important
that Emperor be extensible.

To that end I've leveraged [Akka](http://akka.io/) which is included in the
[Play Framework](http://www.playframework.org/). Akka is a library that uses
uses the [Actor Model](http://en.wikipedia.org/wiki/Actor_model) for building
distributed, concurrent systems. Emperor specfically uses Akka's
[Event Bus](http://doc.akka.io/docs/akka/2.1-M2/scala/event-bus.html) for
distributing messages to plugins that have expressed an interest in certain
events.

# Getting On The Bus

You can take at [Emperor's EventBus](https://github.com/gphat/emperor/blob/master/app/emp/event/EmperorEventBus.scala#L94)
to see how things are setup.  There is a central event bus and a collection
of events.  These events are [documented in Emperor's wiki](https://emperorapp.atlassian.net/wiki/display/EMP/Plugins#Plugins-Subscribing).
This little bit of code provides a mechanism for anything in Emperor to emit
an event out to plugins, like so:

{% highlight scala %}
EmperorEventBus.publish(
  SomeSortOfEvent(
    someArg = someValue
  )
)
{% endhighlight %}

# Expressing Interest

The mechanism by which Emperor will load plugins is not yet defined. At present
the plugins are hardcoded. We'll ignore that oversight for a moment and press
on to learn how to create a plugin and how to let Emperor know what events
it is interested in hearing about.

## First Steps

An Emperor plugin is a class that extends [akka.actor.Actor](http://doc.akka.io/api/akka/2.0.3/#akka.actor.Actor)
and a [companion object](http://daily-scala.blogspot.com/2009/09/companion-object.html)
that extends the trait [emp.Plugin](https://github.com/gphat/emperor/blob/master/app/emp/Plugin.scala).
The compantion object's job is to implement the `relevantEvents` function and
return a `List[String]` containing all of the [names of events](https://emperorapp.atlassian.net/wiki/display/EMP/Plugins#Plugins-Subscribing)
that the plugin would like to receive.

## Doing Something

A minimal plugin might look somethign like this:

{% highlight scala %}
class SomePlugin(configuration: Configuration) extends Actor {

  def receive = {
    case event: EmperorEvent => {
      event match {
        case nte: NewTicketEvent => {
          println("Doing somethign with a new ticket event")
        }
      }
    }
  }
}

object SomePlugin extends Plugin {

  def relevantEvents = List("ticket/created")
}
{% endhighlight %}

The companion object's implementation of `relevantEvents` lets Emperor know
that this plugin is interested in the `ticket/created` event. The class'
`receive` method uses a pattern match to isolate the specific event it received.
This is more relevant to a plugin that receives multiple events, but it's
always good to check!

# Do Something Interesting!

Emperor's plugin system is new and only as featureful as the plugins needed so
far. Some core features &mdash; email notification and search indexing
&mdash; are implemented as plugins.  More events will be added and at some point
plugins that affect Emperor's pages will need to exist. Want to help? [Join up](https://github.com/gphat/emperor/)!