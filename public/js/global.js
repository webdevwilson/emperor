$(document).ready(function() {
  $("#workflow-inuse").on("click", "button", function(event) {
    var t = $(event.currentTarget);
    if(t.hasClass("remover")) {
      var item = t.parents("li")
      item.slideUp("fast", function() {
        item.remove()
      })
      var ival = item.children("input:hidden").val()
      var iname = item.attr("data-name")
      $("#workflow-notinuse").append("<li data-name=\"" + iname + "\"><input type=\"hidden\" value=\"" + ival + "\">" + iname + " <button class=\"fire adder pull-right btn btn-mini\"><i class=\"icon-arrow-right\"></i></button></li>")
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
      var ival = item.children("input:hidden").val()
      var iname = item.attr("data-name")
      $("#workflow-inuse").append("<li data-name=\"" + iname + "\"><input type=\"hidden\" name=\"statusId\" value=\"" + ival + "\"><div class=\"btn-group\" style=\"display: inline; padding: 0; margin: 0\"><button class=\"btn btn-mini fire\"><i class=\"icon-arrow-up\"></i></button><button class=\"btn btn-mini fire\"><i class=\"icon-arrow-down\"></i></button></div>" + iname + "<button class=\"btn btn-mini btn-danger pull-right fire remover\"><i class=\"icon-remove icon-white\"></i></button></li>")
    }
    event.preventDefault();
  })
});
