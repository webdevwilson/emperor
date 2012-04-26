# TODO

* rank (for agile backlog ordering)
* sprint ticket type
* custom flag for user-definable rows
* users
* roles
* ticket-users
* handle "old" names of tickets (if moved)

----

While refactoring a small play 2.0 app, I moved some actions into 
subpackages of controllers (controllers.postings) and added some 
routes such as: 

GET     /postings/:id 
controllers.postings.Jobs.show(id: Long) 

This works fine, but controllers.routes only has ReverseRoutes for 
classes in the main controllers package, not subpackages.  Is there a 
way to get this to work?

-----

The routes are generated, but you're looking in the wrong package. 
Try looking in controllers.postings.routes.Jobs instead of 
controllers.routes.postings.Jobs.