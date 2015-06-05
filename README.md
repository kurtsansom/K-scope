# README

K-scope is a source code analysis tool with graphical user interface that
visualizes program structures of Fortran 90 and FORTRAN 77 source code.
It is suitable for source code reading for engineers who work the performance
improvement for an application.

K-scope is developed by RIKEN AICS Software Development Team, and is distributed
as Open Source Software. The latest version and documents are available in the
following download site.
http://www.aics.riken.jp/ungi/soft/kscope/

## Preliminaries

First of all, K-scope requires JDK7 or later. Please download from Oracle site.
http://www.oracle.com/technetwork/java/javase/downloads/index.html

K-scope uses intermediate code created by front end of
the Omni XcalableMP compiler. The compiler can be downloaded from the following site:
http://www.hpcs.cs.tsukuba.ac.jp/omni-compiler/xcalablemp/download.html


## Remote code building

As an alternative to installing Omni XcalableMP compiler on your computer, you can use
the compiler installed on a server. Installed compiler can be made available for remote
use in two different ways:

1. with SSH access without root privileges,
2. with SSH access with root privileges using Docker IaaS Tools. (github.com/pyotr777/dockerIaaSTools)

#### Difference between 1. and 2. 

For 1. compiler users have to replace absolute paths in their code with a placeholder ```#[remote_path]```.
For 2. users don't have to replace absolute paths.

For the 1. you need only to run an SSH server.
For the 2. setup see instructions below.


### K-scope with Docker IaaS Tools and Omni XMP Compiler

If you have a computer with Docker installed, you can use Docker IaaS Tools to quickly 
make a setup for building code inside a Docker container.
K-scope uses makeRemote.sh to build source code in a Docker container. If makeRemote.sh
is in the same directory as kscope.jar, additional options are enabled in new 
project wizard.

#### Server-side setup for using K-scope with Docker IaaS Tools (DIT) and Omni XMP Compiler (OmniXMP)

Make new directory on your server machine for DIT, cd into it and clone git repository:

```
git clone git@github.com:pyotr777/dockerIaaSTools.git .
```

Download OmniXMP Docker image:

```
docker pull pyotr777/omnixmp
```

Prepare K-scope user public SSH-key, copy it to the directory with DIT 
on the server. cd into DIT directory and run:
```
sudo ./createuser.sh <user name> pyotr777/omnixmp <public key file>
```

#### Local computer setup


Make sure you have makeRemote.sh file in your K-scope directory.
In new project wizard or in Project > Server settings menu set up:
server address, K-scope user name on the server, local path to SSH private key for K-scope
user. 


Demonstration: http://youtu.be/86ybJdnNvUc

*Docker IaaS Tools and makeRemote.sh are developed by RIKEN AICS HPC Usability Research Team
http://github.com/pyotr777/dockerIaaSTools*

*Docker http://docker.com*


## Compile and Run K-scope

This software is written by pure Java to improve the portability.
We provide two-type packages: jar-executable package and source code package
in our site. Especially this source cord packages includes all source codes
necessary for modify and compiling. In that case, we recommend IDE environments
such as Eclipse or NetBeans.
NOTICE) The source codes includes Japanese comments in UTF-8 encoding.

We provide build.xml to compile.

```bash
ant
```

After the compiling, you may obtain jar-executable or classes.
Run is simple as follows.

```bash
java -jar -Xmx1024m kscope.jar
```

K-scope requires specific folders for properties.
If the program cannot find that folders, it may terminate abnormally.
In the normally process, you may obtained start screen.

Tips on usage) "-Duser.language" VM-option is language selector, English(en) and Japanese(ja).

## Build jar file

Run in bin directory:
```bash
jar cfe ../kscope.jar jp.riken.kscope.Kscope *
```



## License

 K-scope
 Copyright 2012-2013 RIKEN, Japan

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
