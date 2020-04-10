package net.termer.tcpacketprotocol.client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.termer.tcpacketprotocol.ExceptionHandler;
import net.termer.tcpacketprotocol.Packet;
import net.termer.tcpacketprotocol.PacketHandler;
import net.termer.tcpacketprotocol.PacketReplyHandler;
import net.termer.tcpacketprotocol.ReplyPacketHandler;

/**
 * Client for TCPacketProtocol servers
 * @author termer
 * @since 1.0
 */
public class TCPacketClient implements AutoCloseable {
	// The Socket to use for I/O
	private Socket _socket = null;
	
	// Client settings
	private TCPacketClientSettings _settings = new TCPacketClientSettings();
	
	// Executor pool for events
	private ExecutorService _execs = null;
	
	// Handlers for replies
	private ConcurrentHashMap<Integer, PacketReplyHandler> _replyHandlers = new ConcurrentHashMap<Integer, PacketReplyHandler>();
	
	// Packet handlers
	private CopyOnWriteArrayList<PacketHandler> _packetHandlers = new CopyOnWriteArrayList<PacketHandler>();
	// Exception handlers
	private CopyOnWriteArrayList<ExceptionHandler> _exceptionHandlers = new CopyOnWriteArrayList<ExceptionHandler>();
	// Connect handlers
	private CopyOnWriteArrayList<ConnectHandler> _connectHandlers = new CopyOnWriteArrayList<ConnectHandler>();
	// Disconnect handlers
	private CopyOnWriteArrayList<DisconnectHandler> _disconnectHandlers = new CopyOnWriteArrayList<DisconnectHandler>();
	
	// Timer that handles reply timeouts
	private Timer _replyTimeoutTimer = new Timer();
	
	/**
	 * Creates a new TCPacketClient
	 * @since 1.0
	 */
	public TCPacketClient() {}
	/**
	 * Creates a new TCPacketClient with the specified settings
	 * @param settings The settings for this client
	 * @since 1.0
	 */
	public TCPacketClient(TCPacketClientSettings settings) {
		_settings = settings;
	}
	/**
	 * Creates a new TCPacketClient on and assigns the port to connect to
	 * @param port The port this client will connect to
	 * @since 1.0
	 */
	public TCPacketClient(int port) {
		_settings.port(port);
	}
	/**
	 * Creates a new TCPacketClient on and assigns the port and address to connect to
	 * @param port The port this client will connect to
	 * @param address The address this server will connect to
	 * @since 1.0
	 */
	public TCPacketClient(int port, String address) {
		_settings
				.port(port)
				.address(address);
	}
	
	/**
	 * Returns this client's settings
	 * @return This client's settings
	 * @since 1.0
	 */
	public TCPacketClientSettings settings() {
		return _settings;
	}
	
	/**
	 * Returns this client's Socket
	 * @return This client's Socket
	 * @since 1.0
	 */
	public Socket socket() {
		return _socket;
	}
	
	/**
	 * Returns whether this client is closed
	 * @return Whether this client is closed
	 * @since 1.0
	 */
	public boolean isClosed() {
		return _socket == null ? true : _socket.isClosed();
	}
	
	/**
	 * Sends a packet
	 * @param packet The packet to send
	 * @throws IOException If sending the packet fails
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketClient send(Packet packet) throws IOException {
		packet.sendTo(_socket.getOutputStream());
		return this;
	}
	
	/**
	 * Sends a packet and calls the specified handler when a reply is received for it
	 * @param packet The packet to send
	 * @param replyHandler The handler to execute when a reply is received
	 * @return This, to be used fluently
	 * @throws IOException If sending the packet fails
	 * @since 1.0
	 */
	public TCPacketClient send(Packet packet, PacketReplyHandler replyHandler) throws IOException {
		// Set expecting reply before assigning handler so ID is generated
		packet.expectingReply(true);
		
		// Register handler
		replyHandler(packet.id(), replyHandler);
		
		// Send packet
		packet.sendTo(_socket.getOutputStream());
		return this;
	}
	/**
	 * Sends a packet and calls the specified handler when a reply is received for it.
	 * Uses the default 5 second timeout time for the reply handler.
	 * @param packet The packet to send
	 * @param handler The handler to execute when a reply is received
	 * @return This, to be used fluently
	 * @throws IOException If sending the packet fails
	 * @since 1.0
	 */
	public TCPacketClient send(Packet packet, ReplyPacketHandler handler) throws IOException {
		send(packet, new PacketReplyHandler(5, handler));
		return this;
	}
	
	/**
	 * Registers a new packet handler
	 * @param handler The packet handler
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketClient packetHandler(PacketHandler handler) {
		_packetHandlers.add(handler);
		return this;
	}
	/**
	 * Registers a new packet reply handler
	 * @param packetId The ID of the packet the reply will be for
	 * @param handler The reply handler
	 * @return This, to be used fluently
	 */
	public TCPacketClient replyHandler(int packetId, PacketReplyHandler handler) {
		_replyHandlers.put(packetId, handler);
		return this;
	}
	/**
	 * Registers a new exception handler
	 * @param handler The exception handler
	 * @return This, to be used fluently
	 */
	public TCPacketClient exceptionHandler(ExceptionHandler handler) {
		_exceptionHandlers.add(handler);
		return this;
	}
	/**
	 * Registers a new connection handler
	 * @param handler The connection handler
	 * @return This, to be used fluently
	 */
	public TCPacketClient connectHandler(ConnectHandler handler) {
		_connectHandlers.add(handler);
		return this;
	}
	/**
	 * Registers a new disconnection handler
	 * @param handler The disconnection handler
	 * @return This, to be used fluently
	 */
	public TCPacketClient disconnectHandler(DisconnectHandler handler) {
		_disconnectHandlers.add(handler);
		return this;
	}
	
	/**
	 * Triggers a client packet event
	 * @param pkt The packet
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketClient triggerPacketHandlers(Packet pkt) {
		if(_settings.blockingHandlers())
			for(PacketHandler hdlr : _packetHandlers)
				hdlr.handle(pkt);
		else
			for(PacketHandler hdlr : _packetHandlers)
				_execs.execute(() -> {
					hdlr.handle(pkt);
				});
		
		return this;
	}
	/**
	 * Triggers a packet reply event
	 * @param packetId The ID of the packet this is a reply for
	 * @param pkt The reply
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketClient triggerPacketReplyHandler(int packetId, Packet pkt) {
		// Check if handler exists
		if(_replyHandlers.containsKey(packetId)) {
			PacketReplyHandler hdlr = _replyHandlers.get(packetId);
			
			// Trigger event
			if(_settings.blockingHandlers())
				hdlr.handler.handle(pkt, false);
			else
				_execs.execute(() -> {
					hdlr.handler.handle(pkt, false);
				});
		}
		
		return this;
	}
	/**
	 * Triggers an exception event
	 * @param exception The exception
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketClient triggerExceptionHandler(Exception exception) {
		if(_settings.blockingHandlers())
			for(ExceptionHandler hdlr : _exceptionHandlers)
				hdlr.handle(exception);
		else
			for(ExceptionHandler hdlr : _exceptionHandlers)
				_execs.execute(() -> {
					hdlr.handle(exception);
				});
		
		return this;
	}
	/**
	 * Triggers a connection event
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketClient triggerConnectHandlers() {
		if(_settings.blockingHandlers())
			for(ConnectHandler hdlr : _connectHandlers)
				hdlr.handle();
		else
			for(ConnectHandler hdlr : _connectHandlers)
				_execs.execute(() -> {
					hdlr.handle();
				});
		
		return this;
	}
	/**
	 * Triggers a disconnection event
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketClient triggerDisconnectHandlers() {
		if(_settings.blockingHandlers())
			for(DisconnectHandler hdlr : _disconnectHandlers)
				hdlr.handle();
		else
			for(DisconnectHandler hdlr : _disconnectHandlers)
				_execs.execute(() -> {
					hdlr.handle();
				});
		
		return this;
	}
	
	/**
	 * Connects to the server
	 * @throws IOException If connection fails
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketClient connect() throws IOException {
		// Connect
		_socket = new Socket(InetAddress.getByName(_settings.address()), _settings.port());
		
		// Setup event executor thread pool
		_execs = Executors.newFixedThreadPool(_settings.packetHandlerPoolSize());
		
		// Setup reply timeout timer
		_replyTimeoutTimer = new Timer();
		_replyTimeoutTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				Instant now = Instant.now();
				
				// Loop through handlers and remove if they've expired
				for(int id: _replyHandlers.keySet()) {
					PacketReplyHandler hdlr = _replyHandlers.get(id);
					if(now.isAfter(hdlr.timeoutDate.toInstant())) {
						// Execute handler with timed out status
						if(_settings.blockingHandlers())
							hdlr.handler.handle(null, true);
						else
							_execs.execute(() -> {
								hdlr.handler.handle(null, true);
							});
						
						// Remove the handler
						_replyHandlers.remove(id);
					}
				}
			}
		}, 0, 1000);
		
		// Start input thread
		Thread thread = new Thread(() -> {
			try {
				// Buffer the input
				BufferedInputStream in = new BufferedInputStream(_socket.getInputStream());
				
				// Buffer for packets
				int leftToRead = 0;
				int leftToSkip = 0;
				byte[] pktBuf = new byte[] {};
				
				// Input loop
				int b = 0;
				while((b = in.read()) > -1) {
					if(leftToSkip < 1 && leftToRead > 0) {
						pktBuf[pktBuf.length - leftToRead] = (byte) b;
						leftToRead--;
						
						// If finished, parse and handle packet
						if(leftToRead < 1) {
							try {
								// Parse the packet
								Packet pkt = Packet.parsePacket(pktBuf).source(_socket);
								
								// Fire reply handler if packet is a reply
								if(pkt.isReply()) {
									triggerPacketReplyHandler(pkt.replyTo(), pkt);
									_replyHandlers.remove(pkt.replyTo());
								}
								
								// Send it to handlers
								triggerPacketHandlers(pkt);
							} catch(Exception e) {
								if(_settings.printErrors()) {
									System.err.println("Error in TCPacketClient:");
									e.printStackTrace();
								}
								
								// Trigger exception handlers
								triggerExceptionHandler(e);
							}
						}
					} else if(leftToSkip < 1) {
						// Get length of next packet
						byte[] lenBytes = new byte[] { (byte) b, (byte) in.read(), (byte) in.read(), (byte) in.read() };
						int size = ByteBuffer.wrap(lenBytes).getInt();
						
						// Determine how much needs to be read or skipped
						if(size > _settings.maxPacketBodySize()) {
							leftToSkip = size;
						} else {
							leftToRead = size;
							pktBuf = new byte[size];
						}
					}
				}
			} catch(Exception e) {
				if(_settings.printErrors()) {
					System.err.println("Error in TCPacketClient:");
					e.printStackTrace();
				}
				
				// Trigger exception handlers
				triggerExceptionHandler(e);
			}
		});
		thread.setName("TCPacketClient");
		thread.start();
		
		// Trigger handlers
		triggerConnectHandlers();
		
		return this;
	}
	
	/**
	 * Closes this client and all of its resources
	 * @throws IOException If closing resources failed
	 * @since 1.0
	 */
	public void close() throws IOException {
		// Close resources
		if(_socket != null && !_socket.isClosed())
			_socket.close();
		if(_execs != null)
			_execs.shutdown();
		if(_replyTimeoutTimer != null)
			_replyTimeoutTimer.cancel();
		
		// Trigger handlers
		triggerDisconnectHandlers();
	}
}
