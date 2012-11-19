function Ticket(data) {
  this.id           = ko.observable(data.id);
  this.ticketId     = ko.observable(data.ticket_id);
  this.projectId    = ko.observable(data.project_id);
  this.projectName  = ko.observable(data.project_name);
  this.priorityId   = ko.observable(data.priority_id);
  this.priorityName = ko.observable(data.priority_name);
  this.priorityNameI18N = ko.observable(data.priority_name_i18n);
  this.summary      = ko.observable(data.summary);
}

function TicketLink(data) {
  this.id           = ko.observable(data.id);
  this.typeId       = ko.observable(data.type_id);
  this.typeName     = ko.observable(data.type_name);
  this.typeNameI18N = ko.observable(data.type_name_i18n);
  this.parentId     = ko.observable(data.parent_id);
  this.parentResolutionId = ko.observable(data.parent_resolution_id);
  this.parentSummary= ko.observable(data.parent_summary);
  this.childId      = ko.observable(data.child_id);
  this.childResolutionId = ko.observable(data.child_resolution_id);
  this.childSummary = ko.observable(data.child_summary);
  this.dateCreated  = ko.observable(data.date_created);
}

function TicketViewModel() {
  // Data
  var self = this;
  self.ticket = ko.observable(new Ticket({}));
  self.links = ko.observableArray([]);

  $.getJSON("http://127.0.0.1:9000/api/ticket/SCOUT-28?callback=?")
  .done(function(allData) { self.ticket(new Ticket(allData)); console.log(self.ticket()) })
  .fail(function(err) { console.log(err); })

  $.getJSON("/api/ticket/link/SCOUT-28", function(allData) {
    var mappedLinks = $.map(allData, function(item) { return new TicketLink(item) });
    self.links(mappedLinks);
  });
}

$.getJSON("/api/ticket/SCOUT-29");

var foo = new TicketViewModel();

ko.applyBindings(foo);

foo.ticket.subscribe(function(newValue) {
    console.log("NEW!");
    console.log(newValue);
});