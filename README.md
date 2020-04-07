# TCPacketProtocol
Packet protocol supporting packet replies and a simple API

# What is it
This is a simple library implementing a basic packet protocol in an easy and event-based way.
You can receive messages and reply to them with a simple API, and reduce TCP server development time.
It also does not depend on any external libraries and is pure Java, so it will fit into any existing project without any changes or bloat.

Here's an example on setting up a server that will reply to packets.

```java

TCPacketServer server = new TCPacketServer(9006);

server.packetHandler(packet -> {
	if(packet.bodyAsString().equals("Ping!")) {
		Packet reply = new Packet()
				.body("Pong!");
		
		packet.replyWith(reply);
	}
});
server.start();

TCPacketClient client = TCPacketClient(9006);
client.connect();

Packet message = new Packet()
		.body("Ping!");

client.send(message, (reply, timedOut) -> {
	if(timedOut) {
		System.err.println("Reply timed out");
	} else {
		System.out.println("Got reply: "+reply.bodyAsString());
	}
});
```

# Features
Here's a list of all features in TCPacketProtocol:

 - Simple and efficient packet structure
 - Repliable packets with callbacks
 - Managed threading
 - Works with Java 8 lambdas
 - Fluent API

# Getting it
Download the JAR library from the releases tab, or compile the library using Gradle.

# Compiling
Download or clone the repository, and run either `gradlew.bat` (Windows), or `./gradlew` (OSX, Linux, Unix).
If all goes well, the library will be in `build/libs/`.

# Javadoc
The Javadoc is located at [https://termer.net/javadoc/tcpacketprotocol/1.0/](https://termer.net/javadoc/tcpacketprotocol/1.0/).