function ShowAlert(aclass, message) {

  // Get the alert area
  var area = $("#alert-area");
  // and find any existing alerts
  var existing = area.find("div.alert");
  // run the template
  var source   = $("#alerter").html();
  var template = Handlebars.compile(source);
  var al = $(template({ alert_class: aclass, message: message }));
  al.hide();

  // Stick the append into a function so we can use it later.
  var appender = function() { al.appendTo(area).slideDown(); };

  // If we have any existing alerts, trash 'em
  if(existing.size() > 0) {
    existing.slideUp("fast", function() {
      existing.remove();
      appender();
    });
  // else, just append it.
  } else {
    appender();
  }
}

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

function TicketLink(ticketId, data) {
  this.id           = ko.observable(data.id);
  this.ticketId     = ko.observable(ticketId);
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
  // Begin computed values for handling the "show the OTHER ticket" problem
  // in the UI
  this.visibleResolved = ko.computed(function() {
    if(this.ticketId() === this.childId()) {
      return !(this.parentResolutionId() == null)
    } else {
      return !(this.childResolutionId() == null)
    }
  }, this);
  this.visibleTicketId = ko.computed(function() {
    if(this.ticketId() === this.childId()) {
      return this.parentId()
    } else {
      return this.childId()
    }
  }, this);
  this.visibleTicketSummary = ko.computed(function() {
    if(this.ticketId() === this.childId()) {
      return this.parentSummary()
    } else {
      return this.childSummary()
    }
  }, this);
}

function TicketViewModel(ticketId) {
  // Data
  var self = this;
  self.links = ko.observableArray([]);

  function showLinks(){
    $.getJSON("/api/ticket/link/" + ticketId + "?callback=?")
    .done(function(allData) {
      var mappedLinks = $.map(allData, function(item) { return new TicketLink(ticketId, item) });
      self.links(mappedLinks);
    })
    .fail(function() { ShowAlert("alert-error", "XXX Failed to retrieve links!") })
  }

  self.changeState = function(statusId) {
    console.log("asdasd: " + statusId)
  }

  self.removeLink = function(data) {
    $.ajax({
      type: "DELETE",
      url: "/api/ticket/link/" + ticketId + "/" + data.id()
    })
    .done(showLinks())
    .fail(function() { ShowAlert("alert-error", "XXX Failed to delete link!") })
  }

  showLinks();
}
