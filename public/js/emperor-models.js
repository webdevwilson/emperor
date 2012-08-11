YUI.add('emperor-models', function(Y) {

  Y.Ticket = Y.Base.create('ticket', Y.Model, [Y.ModelSync.REST], {
      // The root or collection path segment for the server's Users resource.
      root: '/api/ticket'
  });

  Y.TicketWorkflow = Y.Base.create('ticketWorkflow', Y.Model, [Y.ModelSync.REST], {
    root: '/api/ticket/workflow'
  });

  // var ticket;

  // ticket = new Y.Ticket({ id: 'CAMP-7' }).load(function(err) {
  //   console.log(err);
  //   console.log(ticket.get("id"));
  //   console.log(ticket.get("summary"));
  //   ticket.set("summary", "fuck you");
  //   console.log(ticket.getAttrs());
  //   console.log(ticket.get("summary"));
  // });
});