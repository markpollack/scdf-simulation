# scdf-simulation

Experimenting with https://github.com/opentracing-contrib/java-jms with four applications.  These simulate the flow of a stream and a 'tap' in SCDF.  That is

```
stream1 = source | transformer | sink
stream2 = :stream1.source > tap
```

The transformer app and tap app have a sleep of 50ms to simulate processing.  Note, the sink does not sleep.

Steps

# Install ActiveMQ

I downloaded the tarball, disabled all the `<transportConnector/>` elements in `conf/activemq.xml` except for `openwire`, and ran via `./bin/activemq console`

# Install Jaeger backend

* Run Jaeger via
```
$ docker run --rm -it --network=host jaegertracing/all-in-one
```

* Use the `build.sh` script to build the four applications
```
$ ./build.sh
```

# Run the apps

* Run the listener applications, each command in a separate shell window.

```
$ java -jar sink/target/scdf-sink-0.1.0.jar

$ java -jar transformer/target/scdf-transformer-0.1.0.jar 

$ java -jar tap/target/scdf-tap-0.1.0.jar
```

* Now run the 'source`' which will send one message

```
$ java -jar source/target/scdf-source-0.1.0.jar 
```

# View the Traces

* Open the Jaeger UI at `http://localhost:16686/` and look for the service named `scdf-simulation` and push the `Find Traces` button, you should see one trace with a two sends and three receives.  The first send is from the source to the transformer, the second send is from the transformer to the sink.  The second send is 'inside' the overall recieve of the transformer app.  One receive is from the source to the transformer and another receive is from the source to the tap.  The last receive is from the transformer to the sink.  If you run the source multiple times, the length of some of the processing goes down as JIT will kick in.

The tagging and metadata is not yet present to identify the components easily.

![Jaeger Trace view](/scdf-simulation-1.png)

The [Trace JSON](trace.json) file can be used to build other views that show the topology over the timeline.
