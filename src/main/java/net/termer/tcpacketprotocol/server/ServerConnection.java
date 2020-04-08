package net.termer.tcpacketprotocol.server;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

import net.termer.tcpacketprotocol.Packet;
import net.termer.tcpacketprotocol.PacketHandler;
import net.termer.tcpacketprotocol.PacketReplyHandler;
import net.termer.tcpacketprotocol.ReplyPacketHandler;

/**
 * Class to hold methods and data for server connections.
 * @author termer
 * @since 1.0
 */
public class ServerConnection implements AutoCloseable {
	// The actual client socket
	private final Socket _sock;
	// The server this connection is for
	private final TCPacketServer _server;
	// Packet handlers
	private CopyOnWriteArrayList<PacketHandler> _packetHandlers = new CopyOnWriteArrayList<PacketHandler>();
	
	/**
	 * Creates a new ServerConnection
	 * @param socket This connection's Socket
	 * @param server The server this connection is for
	 * @since 1.0
	 */
	public ServerConnection(Socket socket, TCPacketServer server) {
		_sock = socket;
		_server = server;
	}
	
	/**
	 * Returns the Socket object for this connection
	 * @return The Socket object for this connection
	 * @since 1.0
	 */
	public Socket socket() {
		return _sock;
	}
	
	/**
	 * The server this ServerConnection is connected to
	 * @return This connection's server
	 * @since 1.0
	 */
	public TCPacketServer server() {
		return _server;
	}
	
	/**
	 * Registers a new packet handler
	 * @param handler The packet handler
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public ServerConnection packetHandler(PacketHandler handler) {
		_packetHandlers.add(handler);
		return this;
	}
	
	/**
	 * Triggers a server packet event
	 * @param pkt The packet
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public ServerConnection triggerPacketHandlers(Packet pkt) {
		for(PacketHandler hdlr : _packetHandlers)
			hdlr.handle(pkt);
		
		return this;
	}
	
	/**
	 * Sends a packet to this client
	 * @param packet The packet to send
	 * @return This, to be used fluently
	 * @throws IOException If sending the packet fails
	 * @since 1.0
	 */
	public ServerConnection send(Packet packet) throws IOException {
		packet.sendTo(_sock.getOutputStream());
		return this;
	}
	/**
	 * Sends a packet to this client and calls the specified handler when a reply is received for it
	 * @param packet The packet to send
	 * @param replyHandler The handler to execute when a reply is received
	 * @return This, to be used fluently
	 * @throws IOException If sending the packet fails
	 * @since 1.0
	 */
	public ServerConnection send(Packet packet, PacketReplyHandler replyHandler) throws IOException {
		// Set expecting reply before assigning handler so ID is generated
		packet.expectingReply(true);
		
		// Register handler
		_server.replyHandler(packet.id(), replyHandler);
		
		// Send packet
		packet.sendTo(_sock.getOutputStream());
		return this;
	}
	/**
	 * Sends a packet to this client and calls the specified handler when a reply is received for it.
	 * Uses the default 5 second timeout time for the reply handler.
	 * @param packet The packet to send
	 * @param handler The handler to execute when a reply is received
	 * @return This, to be used fluently
	 * @throws IOException If sending the packet fails
	 * @since 1.0
	 */
	public ServerConnection send(Packet packet, ReplyPacketHandler handler) throws IOException {
		send(packet, new PacketReplyHandler(5, handler));
		return this;
	}
	
	/**
	 * Disconnects this connection and closes all of its resources
	 * @throws IOException If closing this connection fails
	 * @since 1.0
	 */
	public void disconnect() throws IOException {
		if(!_sock.isClosed())
			_sock.close();
	}

	/**
	 * Alias to disconnect()
	 * @since 1.0
	 */
	public void close() throws Exception {
		disconnect();
	}
}
