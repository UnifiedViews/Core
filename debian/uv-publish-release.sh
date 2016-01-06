#!/bin/bash
#called e.g. with argument uv-release-2.3.0
#includes paths being specific for tomas-knap's machine

TIME=$(date +%Y%m%d_%H%M%S_%N)
LOG_FILE=/Users/tomasknap/.aptly/uv-release.log
# Close STDOUT file descriptor
exec 1<&-
# Close STDERR FD
exec 2<&-

# Open STDOUT as $LOG_FILE file for read and write.
exec 1<>$LOG_FILE

# Redirect STDERR to STDOUT
exec 2>&1

echo "start publishing: "$TIME

name_prefix=$1
snapshot_name=$name_prefix

aptly repo remove uv-release 'Name (%unifiedviews*)' #remove deb packages
aptly publish drop wheezy || true #unpublish snapshot
aptly snapshot drop $snapshot_name #drop snapshot
aptly repo drop uv-release #drop repository
aptly repo create -distribution=wheezy -component=main uv-release #create repo
aptly repo add -force-replace=true  uv-release  /Users/tomasknap/Documents/PROJECTS/ETL-SWProj/UnifiedView/Core/debian  #add deb packages
aptly repo add -force-replace=true  uv-release  /Users/tomasknap/Documents/PROJECTS/ETL-SWProj/UnifiedView/Plugins/debian  #add deb packages
aptly snapshot create $snapshot_name from repo uv-release  #create snapshot
aptly publish snapshot -distribution=wheezy -architectures=i386,amd64 $snapshot_name   #publish snapshot

#send to the server, available at http://odcs.xrg.cz/unifiedviews/
scp -r -P 42222 /Users/tomasknap/.aptly/public/* knap@odcs.xrg.cz:/data/intlib/debian/
echo "end publishing"


#Please setup your webserver to serve directory '/Users/tomasknap/.aptly/public' with autoindexing.
#echo "deb http://packages.unifiedviews.eu/debian/ wheezy main" > /etc/apt/sources.list.d/unifiedviews.list
#Don't forget to add your GPG key to apt with apt-key.
#wget -O - http://packages.unifiedviews.eu/key/unifiedviews.gpg.key | apt-key add -


#set not tty for gpg - because gpg needs this when it is working remotely, it will be fixed in aptly version 0.9
#/usr/bin/env script -qfc "aptly publish snapshot  -config=/home/aptly/odn-prerelease/.aptly.conf  -passphrase-file="/home/aptly/aptly-commons/key.txt" -distribution=wheezy  -architectures=i386,amd64  $snapshot_name"
#echo "end publishing"

