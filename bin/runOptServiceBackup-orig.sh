#!/bin/bash

# Set up classpath and invoke 'java' with it ...

root=$(dirname $0)/..

cp=".:./../hbaseconfig:$root/dist/TapjoyOptService.jar"

cp=$cp:$(echo $root/lib/*.jar | tr ' ' :)
echo "classpath are: $cp"

ja="-javaagent:/home/tjopt/GIT_opt/tapjoyoptimization/opt_server/newrelic/newrelic.jar"
echo "Javaagent: $ja"

gc="-XX:+PrintGCDetails -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:NewRatio=4 -XX:SurvivorRatio=10 -XX:PermSize=512m -XX:MaxPermSize=512m -XX:CMSInitiatingOccupancyFraction=70 -Xms2g -Xmx16g "
echo "GC params: $gc"

# Becareful with the parameters. 
#  "production" controls which database to visit
#  "productionS3" controls which S3 bucket to upload the files
#  "databaseServer" controls which server to connect
#  The ORDER of algorithm MATTERS!!!
# exec java -server -XX:+PrintGCDetails -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:NewRatio=4 -XX:SurvivorRatio=10 -XX:PermSize=512m -XX:MaxPermSize=512m -XX:CMSInitiatingOccupancyFraction=70 -Xms2g -Xmx6g "$ja" -cp "$cp" com.tapjoy.opt.OptimizationService --production y --productionS3 n --databaseServer backup --algorithms 101
exec java -server $gc $ja -cp $cp com.tapjoy.opt.OptimizationService --production y --productionS3 n --databaseServer backup --algorithms 101

echo $! > pid

