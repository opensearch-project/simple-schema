## Developer Guide

So you want to contribute code to this project? Excellent! We're glad you're here. Here's what you need to do.

### Install Prerequisites

#### JDK 11


OpenSearch builds using Java 11 at a minimum and supports JDK 11, 14 and 17. This means you must have a JDK of supported version installed with the environment variable `JAVA_HOME` referencing the path to Java home for your JDK installation::

``````
$ echo $JAVA_HOME
/Library/Java/JavaVirtualMachines/adoptopenjdk-11.jdk/Contents/Home

$ java -version
openjdk version "11.0.1" 2018-10-16
OpenJDK Runtime Environment 18.9 (build 11.0.1+13)
OpenJDK 64-Bit Server VM 18.9 (build 11.0.1+13, mixed mode)
``````

Here are the official instructions on how to set ``JAVA_HOME`` for different platforms: https://docs.oracle.com/cd/E19182-01/820-7851/inst_cli_jdk_javahome_t/.

### Build

To build the plugin's distributable zip simply run `gradle `.

Example output: `./build/simpleschema*.zip`

### Run


Deploying Locally
-----------------

Sometime you want to deploy your changes to local OpenSearch cluster, basically there are couple of steps you need to follow:

1. Re-assemble to generate plugin jar file with your changes.
2. Replace the jar file with the new one in your workspace.
3. Restart OpenSearch cluster to take it effect.

To automate this common task, you can prepare an all-in-one command for reuse. Below is a sample command for macOS::

```
./gradlew assemble && {echo y | cp -f build/distributions/opensearch-simple-search-1*0.jar <OpenSearch_home>/plugins/opensearch-simple-search} && {kill $(ps aux | awk '/[O]pensearch/ {print $2}'); sleep 3; nohup <OpenSearch_home>/bin/opensearch > ~/Temp/opensearch.log 2>&1 &}
```
Note that for the first time you need to create ``opensearch-simple-search`` folder and unzip ``build/distribution/opensearch-simple-search-xxxx.zip`` to it.

### Submitting Changes

See [CONTRIBUTING](CONTRIBUTING.md).

### Backports

The Github workflow in [`backport.yml`](.github/workflows/backport.yml) creates backport PRs automatically when the original PR
with an appropriate label `backport <backport-branch-name>` is merged to main with the backport workflow run successfully on the
PR. For example, if a PR on main needs to be backported to `1.x` branch, add a label `backport 1.x` to the PR and make sure the
backport workflow runs on the PR along with other checks. Once this PR is merged to main, the workflow will create a backport PR
to the `1.x` branch.