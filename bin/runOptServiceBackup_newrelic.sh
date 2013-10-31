#!/bin/bash

# Set up classpath and invoke 'java' with it ...

root=$(dirname $0)/..

cp=".:$root/dist/TapjoyOptService.jar"

cp=$cp:$(echo $root/lib/*.jar | tr ' ' :)

echo "classpath are: $cp"

#ja="-javaagent:$root/Jmxetric/jmxetric-1.0.3.jar=host=10.90.226.208,port=8649,mode=unicast,wireformat31x=true,process=\"Optimization Backup\",config=$root/Jmxetric/etc/jmxetric.xml"
ja="-javaagent:/home/tjopt/GIT_opt/tapjoyoptimization/opt_server/newrelic/newrelic.jar"

echo "Javaagent: $ja"

# Becareful with the parameters. 
#  "production" controls which database to visit
#  "productionS3" controls which S3 bucket to upload the files
#  "databaseServer" controls which server to connect
#  The ORDER of algorithm MATTERS!!!
exec java -Xmx6g "$ja" -cp "$cp" com.tapjoy.opt.OptimizationService --production y --productionS3 n --databaseServer backup --algorithms 101

echo $! > pid
