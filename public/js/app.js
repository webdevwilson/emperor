YUI({
  gallery: 'gallery-2012.05.16-20-37'
}).use('app', 'model', 'gallery-model-sync-rest', function(Y) {

  var app = new Y.App({
  });
  app.render().dispatch();
  console.log("Hello World!");

  Y.TicketModel = Y.Base.create('ticketModel', Y.Model, [Y.ModelSync.REST], {
    root: '/api/ticket'
  }, {
    ATTRS: {
      summary: {}
    }
  });

  var ticket = new Y.TicketModel({ id: 'CAMP-7' });
  ticket.load();
  console.log(ticket);
});