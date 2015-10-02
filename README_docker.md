# About K-scope with docker-SSHconnect

If you can access a remote server installed with Omni XcalableMP (XMP) compiler (For example, front-end of the K computer is suitable environment to make intermediate codes with XMP), you can build intermediate codes on the server remotely via SSH connection by SSHconnect.jar. This software is an utility for remote command execution with automatic transfer.

It is developed by RIKEN AICS HPC Usability Team in collaboration to enhance the K-scope's feature, and is bundled with the binary package of K-scope.

# Remote code building

As an alternative to installing Omni XcalableMP compiler on local computer it is possible:

1. to use the compiler installed on a remote server machine,
  1. to use the compiler installed in a Docker container,
2. to use Docker IaaS tools for easy access to the compiler installed in a Docker container 
either on a remote server or on user local computer. 

See below for usage instructions for each alternative. 

K-scope uses connect.sh script or SSHconnect.jar to build source code on a remote server or in a Docker container. 
If connect.sh or SSHconnect.jar are in the same directory as kscope.jar, additional options are enabled in new 
project wizard and project settings dialogs.

All settings for accessing Omni XMP on a remote location are stored in Remote settings files. 

Remote settings files for using Omni XMP installed on a remote server machine (1. and 1i. above) 
work with SSHconnect.jar program and are stored under sshconnect remote service folder. 
Remote settings files for using Docker IaaS tools for accessing the compiler in a Docker container (2.)
work with connect.sh script and are stored under dockeriaas remote service folder.   

Whether intermediate code is built on a remote server or on local computer 
"Build command" for building intermediate code and "Clean command" for removing
intermediate code do not change.  Intermediate code is built automatically
after a New Project wizard dialog is complete and can be rebuilt later with 
"Project > Rebuild intermediate code" menu.


## Why use Docker IaaS tools? 

When using Omni XMP on a remote location project files are copied to a temporary location
with paths different from their paths on local computer. Because of that all 
absolute paths in project files must be replaced with a placeholder `#[remote_path]` beforehand.

When using Docker IaaS tools project files are placed in Docker container with the same
paths as on local computer, so replacing absolute paths is not necessary.


## 1. Use Omni XMP compiler on remote server machine

Install Omni XcalableMP compiler on remote machine as described here: 
http://www.hpcs.cs.tsukuba.ac.jp/omni-compiler/doc/Install.html
To be able to connect to the server machine from local computer sshd must be 
configured and running on the server. Consult sshd manual for your server OS.

Substitute all absolute paths in your project source files with `#[remote_path]`
and include these files into "Absolute paths in" list. Create a new Remote settings file with
URL, port number, user name etc. necessary to login into the server with SSH.

## 1i. Use Docker container with Omni XMP compiler

Start Docker container with command: 

```
docker run -d -p 22 pyotr777/omnixmp:0.7.0 /usr/sbin/sshd -D
```

You can login into container with SSH as user "root" with password "docker".
Compiler front-end is installed inside the container in /opt/omnixmp/bin directory. 
Make sure to include it into PATH variable or set add_path parameter in 
K-scope Project settings > Remote settings file.  

Don't forget to substitute all absolute paths in your project source files with `#[remote_path]`
and include these files into "Absolute paths in" list. Create a new Remote settings file with
URL, port number, user name etc. necessary to login into the Docker container with SSH. 


## 2. Use K-scope with Docker IaaS Tools

If you have a server machine with Docker installed, you can use Docker IaaS Tools to quickly 
make a setup for building code inside a Docker container.


### Server-side setup for using K-scope with Docker IaaS Tools (DIT) and Omni XMP Compiler (OmniXMP)

Make new directory on your server machine for DIT, cd into it and clone git repository:

```
git clone git@github.com:pyotr777/dockerIaaSTools.git .
```

Download OmniXMP Docker image:

```
docker pull pyotr777/omnixmp:0.7.0
```

Prepare K-scope user public SSH-key, copy it to the directory with DIT 
on the server. cd into DIT directory and run:
```
sudo ./createuser.sh <user name> pyotr777/omnixmp <public key file>
```

### K-scope Project setup 


Make sure you have connect.sh file in your K-scope directory.
In new project wizard or in Project settings > Remote settings file press "Manage remote settings" buttin,
then press "+" button to create a new remote settings file. Select dockeriaas service and setup parameters
values as described below:
For server_address use you server machine address,
for port use 22 (or whatever port number is used by sshd on your server machine),
for key enter path to you public ssh key on your local computer,
for user name use the same name that was used in createuser.sh command on the server machine.


Demonstration: http://youtu.be/86ybJdnNvUc

*Docker IaaS Tools and connect.sh are developed by RIKEN AICS HPC Usability Research Team
http://github.com/pyotr777/dockerIaaSTools*

*Docker http://docker.com*
