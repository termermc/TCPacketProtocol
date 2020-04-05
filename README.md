# TCPacketProtocol
Packet protocol supporting packet replies and a simple API

# WIP
Work in progress. Run `./gradlew build` to build.

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
		.withBody("Ping!")

client.send(message, (reply, timedOut) -> {
	if(timedOut) {
		System.err.println("Reply timed out");
	} else {
		System.out.println("Got reply: "+reply.bodyAsString());
	}
});
```