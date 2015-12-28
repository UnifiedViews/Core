-- ##############################################################
-- ##    Database update script for standard UV installation    #
-- ##    Update from v. 2.0.x to 2.1.0                          #
-- ##############################################################

-- Execute update script
\ir 2.1.0-core.sql

-- Execute permissions script
\ir 2.1.0-permissions.sql