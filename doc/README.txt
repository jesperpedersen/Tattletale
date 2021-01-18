JBoss Tattletale
================

JBoss Tattletale is a tool that can help you get an overview of the
project you are working on or a product that you depend on.

The tool will provide you with reports that can help you

* Identify dependencies between archives
* Find missing classes from the classpath
* Spot if a class/package is located in multiple archives
* Spot if the same archive is located in multiple locations
* List what each archive requires and provides
* Verify the SerialVersionUID of a class
* Find similar archives that have different version numbers
* Find archives without a version number
* Find unused archives
* Identify sealed/signed archives
* Locate a class in an archive
* Get the OSGi status of your project
* Identify blacklisted API usage

JBoss Tattletale will recursively scan the hash-separated list of
directories for archives and/or analyse a list of archives passed as
the argument and then build the reports as HTML files.

The main HTML file is: index.html

JBoss Tattletale is licensed under GNU Lesser General Public License
(LGPL) version 2.1 or later.

We hope that JBoss Tattletale will help you in your development tasks!


Quick start:
------------
java -jar tattletale.jar [-title=<title>] [-components[=<regex>]] [-exclude=<excludes>] <input>[#<input>]* [<output-directory>]

Analysis reports are generated in current directory if no
output-directory is set.


User guide:
-----------
The JBoss Tattletale user guide is located in JBossTattletale-UsersGuide.pdf.


Developer guide:
----------------
The JBoss Tattletale developer guide is located in JBossTattletale-DevelopersGuide.pdf.


Development:
------------
Home          : http://www.jboss.org/projects/tattletale
Download      : http://www.jboss.org/projects/tattletale
Forum         : http://www.jboss.org/index.html?module=bb&op=viewforum&f=306
Issue tracking: http://jira.jboss.com/jira/browse/TTALE
AnonSVN       : http://anonsvn.jboss.org/repos/tattletale
Developer SVN : https://svn.jboss.org/repos/tattletale
