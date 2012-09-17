# API

## General

All API access is accessed from `youremporor.app/api`. All data is sent and received as JSON.

All timestamps are in ISO 8601 format.

    YYYY-MM-DDTHH:MM:SSZ

## Errors

1. Sending invalid JSON will result in a `400 Bad Request` response.

2. Sending the wrong type of JSON values will result in a `400 Bad Request` response.

## Group

### Add a user

    PUT /api/group/:id/:userId

#### Response

    {
      "ok": "ok"
    }

## Group

### Remove a user

    DELETE /api/group/:id/:userId

#### Response

    {
      "ok": "ok"
    }

### Find groups that start with a string

    GET /api/group/startsWith?q=foo

#### Response

    [
      {
        "date_created":"20120805T033343Z",
        "id":11,
        "name":"emperor-users"
      },
      …
    ]

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

    DELETE /api/ticket/link/:ticketid/:linkid

#### Input

#### Response

    { "ok" : "ok" }

## User

### Find users that start with a string

    GET /api/user/startsWith?q=foo

#### Response

    [
      {
        "date_created":"20120805T033343Z",
        "email":"park@example.com",
        "username":"park",
        "id":11,
        "real_name":"Park Chu-Young",
        "password":"$2a$12$OPppaFb5A.E2GpB0LbQN9ePyF6mlWrJsC4tFKIVsvva4H5Jl.0Gi2"
      },
      …
    ]
