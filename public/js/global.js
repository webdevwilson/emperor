$(document).ready(function() {
  $("#workflow-inuse").on("click", "button", function(event) {
    var t = $(event.currentTarget);
    if(t.hasClass("remover")) {
      var item = t.parents("li")
      item.slideUp("fast", function() {
        item.remove()
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
  })

  $("#workflow-notinuse").on("click", "button", function(event) {
    var t = $(event.currentTarget);
    if(t.hasClass("adder")) {
      var item = t.parents("li")
      item.slideUp("fast", function() {
        item.remove()
      })
      var ival = item.children("input:hidden").val();
      var iname = item.attr("data-name");
      var source   = $("#used-item").html();
      var template = Handlebars.compile(source);
      $("#workflow-inuse").append(template({ status_name: iname, status_id: ival }));
    }
    event.preventDefault();
  })
});
