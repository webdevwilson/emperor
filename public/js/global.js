$(document).ready(function() {

  // Enable alerts
  $().alert();

  // Add hasAttr function, cuz it should exist
  $.fn.hasAttr = function(name) {
     return this.attr(name) !== undefined;
  };

  var thead = $("#ticket-header");
  if(thead.size() > 0) {
    showLinker(thead.attr("data-ticket"));
  }

  function showAlert(aclass, message) {

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
    var appender = function() { al.appendTo(area).slideDown(); }

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

  function removeLinker(callback) {
    var toggle = $("#linker").children(".link-item");
    if(toggle.size() > 0) {
      toggle.fadeOut("fast", function() {
        toggle.remove();
        callback();
      });
    }
  }

  function showLinker(ticketId) {

    // Fetch the linker markup
    $.get('/ticket/linker', function(resp) {
      // Install it into the linker element.
      var cont = $(resp);
      var lnkr = $("#linker");
      var toggle = lnkr.children(".link-item");
      if(toggle.size() > 0) {
        toggle.fadeOut("fast", function() {
          toggle.remove();
          cont.appendTo(lnkr);
        });
      } else {
        cont.appendTo(lnkr);
        var linkid = cont.attr("data-linkid")
        if(linkid == ticketId) {
          cont.children().first().addClass("disabled");
        }
        lnkr.on("click", "a.linker", function(event) {
          var clicked = $(event.currentTarget);
          var child = thead.attr("data-ticket");
          var parent = clicked.attr("data-parent");
          var ltid = clicked.attr("data-linktype");

          if(clicked.hasAttr("data-inverse")) {
            var oldChild = child;
            var child = parent;
            var parent = oldChild;
          }

          $.post("/ticket/link/" + ltid + "/" + parent + "/" + child, function(data) {
            showAlert("alert-info", data);
          }).error(function() {
            showAlert("alert-error", "Error: Link failed!")
          });
        });
        lnkr.on("click", "a.remover", function(event) {
          $.post("/ticket/stoplink", function(data) {
            removeLinker(function() {
              showAlert("alert-success", data);
              $("#startlink").removeClass("disabled");
            });
          });
        });
      }
    });
  }

  // Handle workflow changes
  $("#workflow-inuse").on("click", "button", function(event) {
    var t = $(event.currentTarget);
    if(t.hasClass("remover")) {
      var item = t.parents("li")
      item.slideUp("fast", function() {
        item.remove();
      })
      var ival = item.children("input:hidden").val();
      var iname = item.attr("data-name");
      var source   = $("#unused-item").html();
      var template = Handlebars.compile(source);
      $("#workflow-notinuse").append(template({ status_name: iname, status_id: ival }));
    }
    if(t.hasClass("upper")) {
      var here = t.parents("li")
      var above = here.prev()
      if(above != null) {
        above.before(here);
      }
    }
    if(t.hasClass("downer")) {
      var here = t.parents("li")
      var below = here.next()
      if(below != null) {
        below.after(here);
      }
    }
    event.preventDefault();
  });

  // Handle workflow changes
  $("#workflow-notinuse").on("click", "button", function(event) {
    var t = $(event.currentTarget);
    if(t.hasClass("adder")) {
      var item = t.parents("li")
      item.slideUp("fast", function() {
        item.remove();
      })
      var ival = item.children("input:hidden").val();
      var iname = item.attr("data-name");
      var source   = $("#used-item").html();
      var template = Handlebars.compile(source);
      $("#workflow-inuse").append(template({ status_name: iname, status_id: ival }));
    }
    event.preventDefault();
  });

  $("#startlink").click(function(event) {
    var button = $(event.currentTarget);
    var tickId = button.attr("data-ticket");

    $.post('/ticket/startlink/' + tickId, function(data) {
      showLinker(tickId);
      button.addClass("disabled");
      event.preventDefault();
    });
  });
});
