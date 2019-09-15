Tips for writing compiler APT tests:

 - If you add or modify an annotation processor, you must create a new test processor jar.
 To do so, right-click on apttestprocessors.jardesc and apttestprocessors8.jardesc and
 select "Create Jar".  The jar file must be committed to gerrit along with the new processor code.