Running the interface with eclipse:
-----------------------------------

In the run configuration it's important to add the path "src/test/resources" to the classpath.
At this location all files are present needed for the execution. When creating the installer,
the files from "src/main/release" will be taken instead and copied into a "conf"-directory.
The conf-dir has to be added to the classpath, which is normally done by the start script. 