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

function GroupUser(data) {
  this.id       = ko.observable(data.id);
  this.userId   = ko.observable(data.userId);
  this.groupId  = ko.observable(data.groupId);
  this.username = ko.observable(data.username);
  this.realName = ko.observable(data.realName);
  this.realNameI18N = ko.observable(data.realNameI18N);
  this.dateCreated = ko.observable(data.dateCreated);
}

function Permission(data, users, groups) {
  this.name           = ko.observable(data.name);
  this.nameI18N       = ko.observable(data.nameI18N);
  this.descriptionI18N= ko.observable(data.descriptionI18N);
  this.users          = ko.observableArray(users);
  this.groups         = ko.observableArray(groups);
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
  this.typeNameI18NInverted = ko.observable(data.type_name_i18n_inverted);
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
  this.visibleType = ko.computed(function() {
    if(this.ticketId() === this.childId()) {
      return this.typeNameI18NInverted()
    } else {
      return this.typeNameI18N()
    }
  }, this);
  this.visibleURL = ko.computed(function() {
    return "/ticket/" + this.visibleTicketId()
  }, this);
}

function AdminPermissionSchemeViewModel(permissionSchemeId) {
  var self = this
  self.permissions = ko.observableArray([]);

  self.addGroup = function(perm) {
    var $modal = $("#ajax-modal");
    $('body').modalmanager('loading');
    $modal.load("/admin/permission_scheme/group/" + permissionSchemeId + "/" + perm.name(), '', function(){
      $modal.modal();
    });
  }

  self.removeGroup = function(parent, data) {
    $.ajax({
      type: "DELETE",
      url: "/api/permission_scheme/" + permissionSchemeId + "/" + data.permissionId + "/group/" + data.id
    })
    .done(parent.groups.remove(data))
    .fail(function() { ShowAlert("alert-error", "XXX Failed to delete link!") })
  }

  self.addUser = function(perm) {
    var $modal = $("#ajax-modal");
    $('body').modalmanager('loading');
    $modal.load("/admin/permission_scheme/user/" + permissionSchemeId + "/" + perm.name(), '', function(){
      $modal.modal();
    });
  }

  self.removeUser = function(parent, data) {
    $.ajax({
      type: "DELETE",
      url: "/api/permission_scheme/" + permissionSchemeId + "/" + data.permissionId + "/user/" + data.id
    })
    .done(parent.users.remove(data))
    .fail(function() { ShowAlert("alert-error", "XXX Failed to delete link!") })
  }

  $.getJSON("/api/permission")
  .done(function(data) {
    var mappedPerms = $.map(data, function(item) {

      var perm = new Permission(item, [], []);

      var users = $.getJSON("/api/permission_scheme/" + permissionSchemeId + "/users/"  + perm.name())
        .done(function(data) { perm.users(data) });
      var groups= $.getJSON("/api/permission_scheme/" + permissionSchemeId + "/groups/" + perm.name())
        .done(function(data) { perm.groups(data) });
      return perm;
    });
    self.permissions(mappedPerms);
  })
}

function GroupViewModel(groupId) {
  var self = this;
  self.users = ko.observableArray([]);

  self.addUser = function() {
    var username = $("#userInput").val();
    $.ajax({
      type: "PUT",
      url: "/api/group/" + groupId + "/" + username
    })
      .done(function(data) {
        self.users.push(new GroupUser(data));
        $("#userInput").val("");
      })
      .fail(function() { ShowAlert("alert-error", "XXX Failed to add user!") });
  }

  self.removeUser = function(data) {
    $.ajax({
      type: "DELETE",
      url: "/api/group/" + groupId + "/" + data.id()
    })
      .done(function() {
        self.users.remove(data)
      })
      .fail(function() { ShowALert("alert-error", "XXX Failed to remove user!") });
  }

  $.getJSON("/api/group/" + groupId + "/users")
    .done(function(data) {
      var mappedGUs = $.map(data, function(item) { return new GroupUser(item) });
      self.users(mappedGUs);
    })
    .fail(function() { ShowAlert("alert-error", "XXX Failed to retrieve group users!") });
}

function TicketLinkViewModel() {
  var self = this;
  self.query = ko.observable("");
  self.tickets = ko.observableArray([]);
  self.selectedTickets = ko.observableArray([]);
  self.maybeTicket = ko.observable(-1);

  var subscription = this.query.subscribe(function(newValue) {
    // Don't search unless we get at least 2 characters
    if(newValue.length > 1) {
      self.searchTickets(newValue);
    }
  });

  self.searchTickets = function(q) {
    $.getJSON("/api/ticket/startswith?q=" + q + "&callback=?")
      .done(function(data) {
        var mappedTickets = $.map(data, function(item) { return new Ticket(item) });
        self.tickets(mappedTickets);
      })
      .fail(function(e) { console.log(e); ShowAlert("alert-error", "XXX Failed to search tickets!") });
  }

  self.moveTicket = function(data, event) {
    if(event.keyCode == 40 || event.keyCode == 38 || event.keyCode == 13) {
      var tsize = self.tickets().length - 1 // 0 based
      if(tsize > 0) {
        switch(event.keyCode) {
          case 38: // up
            // Decrement, unless we go below 0
            self.maybeTicket(Math.max(0, self.maybeTicket() - 1))
          break

          case 40: // down
            // Increment, unless we go above the number of tickets
            self.maybeTicket(Math.min(tsize, self.maybeTicket() + 1))
          break

          case 13: // enter
            // Select the selected ticket
            if(self.maybeTicket != -1) {
              self.selectTicket(self.tickets()[self.maybeTicket()]);
            }
          break
        }
      }
      return false;
    } else {
      return true;
    }
  }

  self.selectTicket = function(data) {
    // Add to the selected ones
    self.selectedTickets.push(data);
    // Clear the inputs
    self.query("");
    self.tickets([])
  }

  self.removeTicket = function(data) {
    self.selectedTickets.remove(data);
  }
}

function TicketViewModel(ticketId) {
  // Data
  var self = this;
  self.links = ko.observableArray([]);

  function showLinks(){
    $.getJSON("/api/ticket/link/" + ticketId + "?callback=?")
      .done(function(data) {
        var mappedLinks = $.map(data, function(item) { return new TicketLink(ticketId, item) });
        self.links(mappedLinks);
      })
      .fail(function() { ShowAlert("alert-error", "XXX Failed to retrieve links!") });
  }

  self.changeState = function(ticketId, statusId) {
    var $modal = $("#ajax-modal");
    $('body').modalmanager('loading');
    $modal.load("/ticket/change/" + ticketId + "/" + statusId, '', function(){
      $modal.modal();
    });
  }

  self.makeLink = function(ticketId) {
    var $modal = $("#ajax-modal");
    $('body').modalmanager('loading');
    $modal.load("/ticket/link/" + ticketId, '', function(){
      $modal.modal();
    });
  }

  self.removeLink = function(data) {
    $.ajax({
      type: "DELETE",
      url: "/api/ticket/link/" + ticketId + "/" + data.id()
    })
    .done(self.links.remove(data))
    .fail(function() { ShowAlert("alert-error", "XXX Failed to delete link!") })
  }

  showLinks();
}
