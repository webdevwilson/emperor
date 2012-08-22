## 0.0.7
 * Show unresolved tickets on project page
 * Add JSON serialization for Ticket Priority
 * Add YUI model for Project
 * Remove old, unused templates
 * Add project-level defaults for various ticket fields

## 0.0.6
 * There is now an edit button on the ticket view page. (EMP-2)
 * A ticket's resolution status is now clearly shown on the ticket view page (EMP-3)
 * Reporter now defaults to the logged in user when creating a ticket (EMP-4)
 * Resolution status (via strikethrough) and summary are now shown in links (EMP-6)
 * Ticket creation now shows up in the timeline without a reindex (EMP-7)
 * Project is now listed as the first item in ticket creation and editing (EMP-8)
 * Revamp search results page to show more information (EMP-9)
 * Ticket links are now styled
 * Ticket links can now be removed (and have an API call)
 * Add more API docs (but they are still bad)
 * Change color of search filter buttons

## 0.0.5.1
 * Fix JS error in linking button

## 0.0.5
 * Start of API
  * Getting a project
  * Getting a ticket
  * Linking two tickets
  * Setting the link ticket
  * Removing the link ticket
 * Revamped JS for linking and ticket workflow
 * Better link UI

## 0.0.4
 * Events
 * Ticket Linking
 * Ticket Resolution & Unresolution with comments
  * Full unit tests
 * Ticket advance & revert dialog w/refreshing
 * Case class docs
 * Timeline
