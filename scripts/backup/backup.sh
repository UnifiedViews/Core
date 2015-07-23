#*******************************************************************************
# This file is part of UnifiedViews.
#
# UnifiedViews is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# UnifiedViews is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with UnifiedViews.  If not, see <http://www.gnu.org/licenses/>.
#*******************************************************************************
#!/bin/bash
BACKUP_CONFIG="/home/uv/backup_script/backup.conf"


if [ -r "$BACKUP_CONFIG" ]; then
  . $BACKUP_CONFIG
fi

timestamp=$(date +"%Y-%m-%d_%H:%M:%S")

echo "start: "$timestamp

zip -r  backend.zip $BACKEND_JAR $BACKEND_CONF $BACKEND_LIB
zip  backend-init.zip $BACKEND_INIT $BACKEND_INIT_CONF $BACKEND_RUN

zip frontend.zip $FRONTEND_JAR $FRONTEND_CONF
zip -r plugins.zip $PLUGINS

zip tomcat.zip $TOMCAT

DB_DUMP_NAME=db_$timestamp.sql
mysqldump $MYSQL_NAME  -u $MYSQL_USER --password=$MYSQL_PASS --ignore-table=$MYSQL_NAME.logging > $DB_DUMP_NAME
mysqldump $MYSQL_NAME  -u $MYSQL_USER --password=$MYSQL_PASS  logging --where "unix_timestamp(DATE_SUB(NOW(), INTERVAL 1 day)) <  timestmp/1000 order by id desc  " >> $DB_DUMP_NAME


BACKUP_NAME=uv_backup_$timestamp.zip
zip $BACKUP_NAME  backend.zip frontend.zip plugins.zip tomcat.zip backend-init.zip  $DB_DUMP_NAME

if [ ! -d $OUT_DIRECTORY ]; then
    mkdir -p $OUT_DIRECTORY
fi

mv $BACKUP_NAME $OUT_DIRECTORY

rm  backend.zip frontend.zip plugins.zip tomcat.zip backend-init.zip $DB_DUMP_NAME 

timestamp=$(date +"%Y-%m-%d_%H:%M:%S")

echo "successfully end: "$timestamp





