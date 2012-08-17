# API

## General

All API access is accessed from `youremporor.app/api`. All data is sent and received as JSON.

All timestamps are in ISO 8601 format.

    YYYY-MM-DDTHH:MM:SSZ

## Errors

1. Sending invalid JSON will result in a `400 Bad Request` response.

2. Sending the wrong type of JSON values will result in a `400 Bad Request` response.

## Project

### Get a project

    GET /api/project/:id

#### Response

    {
      "foo": 1
    }

## Link Ticket

### Get a link ticket

    GET /api/linkticket/:id

#### Response

### Set the link ticket

    PUT /api/linkticket/:id

#### Input

#### Response

## Ticket

## Get a ticket

    GET /api/ticket/:id

#### Response

## Ticket Link

### Get a ticket link

    GET /api/ticket/link/:ticketId

#### Input

#### Response

### Link two tickets

    PUT /api/ticket/link/:ticketId

#### Input

#### Response

    XXX

### Remove a link between tickets

    DELETE /api/ticket/link/:linkid

#### Input

#### Response

    { "ok" : "ok" }
