Installation
============

PostgreSQL, PostGIS and inital data
-----------------------------------

After installing PostgreSQL and PostGIS you need to set up the database:

<blockquote>
su - postgres
createuser osmgress
createdb osmgress_mapnik

psql -d osmgress_mapnik -U postgres --file=/usr/share/postgresql/contrib/postgis-2.1/postgis.sql
psql -d osmgress_mapnik -U postgres --file=/usr/share/postgresql/contrib/postgis-2.1/spatial_ref_sys.sql
psql -d osmgress_mapnik -U postgres --command="alter table geometry_columns owner to osmgress"
psql -d osmgress_mapnik -U postgres --command="alter table spatial_ref_sys owner to osmgress"
</blockquote>

Inserting the initial data into the database

<blockquote>
osm2pgsql -s -U postgres -d osmgress_mapnik Downloads/rheinland-pfalz-latest.osm.pbf
</blockquote>

Apache, renderd and mod_tile

<blockquote>
/etc/renderd.conf
[renderd]
num_threads=4
tile_dir=/var/lib/mod_tile
stats_file=/var/run/renderd/renderd.stats

[mapnik]
plugins_dir=/usr/lib/mapnik/input
font_dir=/usr/share/fonts/TTF
font_dir_recurse=1

[default]
URI=/osm_tiles/
TILEDIR=/var/lib/mod_tile
XML=/home/cbrill/workspace/osmgress/style/osmgress.xml
HOST=localhost
TILESIZE=256
</blockquote>

/etc/httpd/conf/httpd.conf
<blockquote>
Include conf/extra/mod_tile.conf
Include conf/extra/mod_jk.conf
</blockquote>

/etc/httpd/conf/extra/mod_tile.conf
<blockquote>
LoadModule tile_module modules/mod_tile.so
ModTileTileDir /var/lib/mod_tile
LoadTileConfigFile /etc/renderd.conf
ModTileRenderdSocketName /var/run/renderd/renderd.sock
</blockquote>

/etc/httpd/conf/extra/mod_jk.conf
<blockquote>
LoadModule jk_module modules/mod_jk.so
<IfModule jk_module>
    JkWorkersFile   /etc/httpd/conf/workers.properties
    JkShmFile       /var/run/shm.file
    JkShmSize       1048576
    JkLogFile       /var/log/httpd/mod_jk.log
</IfModule>
</blockquote>

/etc/httpd/conf/workers.properties
<blockquote>
workers.apache_log=/var/log/httpd/
workers.tomcat_home=/opt/tomcat
workers.java_home=/opt/java
ps=/
worker.list=worker2
worker.worker2.type=ajp13
worker.worker2.host=localhost
worker.worker2.port=8009
worker.worker2.mount=/osmgress /osmgress/*
</blockquote>

Style
=====

Colors
------

<table>
<tr>
<td></td>
<td></td>
<td></td>
</tr>
<tr>
<td>Blue</td>
<td>#005ca7</td>
<td>#269eff</td>
</tr>
<tr>
<td>Green</td>
<td>#03a700</td>
<td>#3bea5e</td>
</tr>
</table>
