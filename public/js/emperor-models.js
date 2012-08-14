YUI.add('emperor-models', function(Y) {

  Y.Ticket = Y.Base.create('ticket', Y.Model, [Y.ModelSync.REST], {
    root: '/api/ticket'
  });

  Y.LinkTicket = Y.Base.create('linkTicket', Y.Model, [Y.ModelSync.REST], {
    root: '/api/linkticket'
  });

  Y.TicketLink = Y.Base.create('ticketLink', Y.Model, [Y.ModelSync.REST], {
    root: '/api/ticket/link'
  });

  Y.TicketLinks = Y.Base.create('ticketLinks', Y.ModelList, [Y.ModelSync.REST], {
    model: Y.TicketLink
  });
});
