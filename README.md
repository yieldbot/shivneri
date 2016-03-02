![Shivneri] (https://upload.wikimedia.org/wikipedia/commons/thumb/f/f1/Mahadaravaja_-_Shivneri_Fort.jpg/640px-Mahadaravaja_-_Shivneri_Fort.jpg)

# Shivneri 
It reads messages from kafka and inserts them in couchbase

## Usage
    $ java -jar shivneri-0.1.0-standalone.jar [args]

## Options
    -g, --group.id GROUP.ID (REQUIRED)                  Kafka Consumer group id  
    -k, --kafka.zk.connect KAFKA.ZK.CONNECT (REQUIRED)  zookeeper host:port/chroot e.g localhost:2181/kafka  
    -t, --topic.name TOPIC.NAME (REQUIRED)              Name of the kafka topic to consume  
    -p, --health.port HEALTH.PORT                       Port to listen for health requests  
    -b, --couchbase.bucket COUCHBASE.BUCKET (REQUIRED)  Name of couchbase bucket to insert kafka messages  
    -c, --couchbase.hosts COUCHBASE.HOSTS (REQUIRED)    Comma separated list of couchbase hosts  
    -s, --batch.size                                    BATCH.SIZE  
    -h, --help

## Examples

    java -jar shivneri-0.1.0-SNAPSHOT-standalone.jar  -g shivneri.prod -k server1.kafka.com:2181/kafka -t adimpression -b adimpression -c cb1.com,cb2.com


## License

Copyright © 2016 Yieldbot Inc

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
