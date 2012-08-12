$(document).ready(function() {

  // Enable alerts
  $().alert();

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
});
