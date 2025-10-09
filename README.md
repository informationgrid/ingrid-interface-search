Search Interface
====================

This software is part of the InGrid software package. The Search interface provides

- an OpenSearch interface to the InGrid data space
- an ATOM Download Service Interface to geo services meta data records and the data they are based on

It encapsulates all interfaces that work directly on the InGrid data space. No intermediate storage or index is being
used.


Features
--------

- OpenSearch interface to integrate InGrid data into other applications
- delivers detailed data such as ISO 19115/19119 meta data
- high performance streaming implementation

- ATOM Download Service Interface to access base data for geo services meta data
- automatically discovers connected datasources
- add filters to narrow results to certain data providers
- user-friendly HTML based view on the data

Requirements
-------------

- a running InGrid Software System

Installation
------------

Download from https://distributions.informationgrid.eu/ingrid-interface-search/

or

build from source with `mvn clean package`.

Execute

```
java -jar ingrid-interface-search-x.x.x-installer.jar
```

and follow the install instructions.

Obtain further information at http://www.ingrid-oss.eu/ (sorry only in German)


Contribute
----------

- Issue Tracker: https://github.com/informationgrid/ingrid-interface-search/issues
- Source Code: https://github.com/informationgrid/ingrid-interface-search

### Setup Eclipse project

* import project as Maven-Project
* right click on project and select Maven -> Select Maven Profiles ... (Ctrl+Alt+P)
* choose profile "development"
* run "mvn compile" from Commandline (unpacks base-webapp)
* run de.ingrid.iface.IFaceServer as Java Application
* in browser call "http://localhost:10102" with login "admin/admin"

### Setup IntelliJ IDEA project

* choose action "Add Maven Projects" and select pom.xml
* in Maven panel expand "Profiles" and make sure "development" is checked
* run de.ingrid.iface.IFaceServer
* in browser call "http://localhost:10102" with login "admin/admin"

- set up a java application Run Configuration with start class `de.ingrid.iface.IFaceServer`

Support
-------

If you are having issues, please let us know: info@informationgrid.eu

License
-------

The project is licensed under the EUPL license.
