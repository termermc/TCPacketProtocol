package net.termer.tcpacketprotocol.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
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

/**
 * Main TCPacketProtocol server class. Handles all server functions.
 * @author termer
 * @since 1.0
 */
public class TCPacketServer implements AutoCloseable {
	// Server settings
	private TCPacketServerSettings _settings = new TCPacketServerSettings();
	
	// Server socket
	private ServerSocket _server = null;
	
	// Executor pool for events
	private ExecutorService _execs = null;
	
	// Handlers for replies
	private ConcurrentHashMap<Integer, PacketReplyHandler> _replyHandlers = new ConcurrentHashMap<Integer, PacketReplyHandler>();
	
	// Whether the server is shut down
	private boolean _shutDown = false;
	
	// Packet handlers
	private CopyOnWriteArrayList<PacketHandler> _packetHandlers = new CopyOnWriteArrayList<PacketHandler>();
	// Connect handlers
	private CopyOnWriteArrayList<ConnectHandler> _connectHandlers = new CopyOnWriteArrayList<ConnectHandler>();
	// Connect handlers
	private CopyOnWriteArrayList<DisconnectHandler> _disconnectHandlers = new CopyOnWriteArrayList<DisconnectHandler>();
	// Exception handlers
	private CopyOnWriteArrayList<ExceptionHandler> _exceptionHandlers = new CopyOnWriteArrayList<ExceptionHandler>();
	
	// Connections
	private CopyOnWriteArrayList<ServerConnection> _connections = new CopyOnWriteArrayList<ServerConnection>();
	
	// Timer that handles reply timeouts
	private Timer _replyTimeoutTimer = new Timer();
	
	/**
	 * Creates a new TCPacketServer
	 * @since 1.0
	 */
	public TCPacketServer() {}
	/**
	 * Creates a new TCPacketServer with the specified settings
	 * @param settings The settings for this server
	 * @since 1.0
	 */
	public TCPacketServer(TCPacketServerSettings settings) {
		_settings = settings;
	}
	/**
	 * Creates a new TCPacketServer on the specified port
	 * @param port The port this server will bind to
	 * @since 1.0
	 */
	public TCPacketServer(int port) {
		_settings.bindPort(port);
	}
	/**
	 * Creates a new TCPacketServer on the specified port and address
	 * @param port The port this server will bind to
	 * @param address The address this server will bind to
	 * @since 1.0
	 */
	public TCPacketServer(int port, String address) {
		_settings
				.bindPort(port)
				.bindAddress(address);
	}
	
	/**
	 * Returns this server's settings
	 * @return This server's settings
	 * @since 1.0
	 */
	public TCPacketServerSettings settings() {
		return _settings;
	}
	
	/**
	 * Returns all current server connections
	 * @return All current server connections
	 * @since 1.0
	 */
	public ServerConnection[] connections() {
		return _connections.toArray(new ServerConnection[0]);
	}
	
	/**
	 * Returns this server's ServerSocket object
	 * @return This server's ServerSocket
	 * @since 1.0
	 */
	public ServerSocket serverSocket() {
		return _server;
	}
	
	/**
	 * Returns whether this server is closed
	 * @return Whether this server is closed
	 * @since 1.0
	 */
	public boolean isClosed() {
		return _server == null ? true : _server.isClosed();
	}
	
	/**
	 * Registers a new packet handler
	 * @param handler The packet handler
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketServer packetHandler(PacketHandler handler) {
		_packetHandlers.add(handler);
		return this;
	}
	/**
	 * Registers a new connection handler
	 * @param handler The connection handler
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketServer connectHandler(ConnectHandler handler) {
		_connectHandlers.add(handler);
		return this;
	}
	/**
	 * Registers a new disconnection handler
	 * @param handler The disconnection handler
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketServer disconnectHandler(DisconnectHandler handler) {
		_disconnectHandlers.add(handler);
		return this;
	}
	/**
	 * Registers a new packet reply handler
	 * @param packetId The ID of the packet the reply will be for
	 * @param handler The reply handler
	 * @return This, to be used fluently
	 */
	public TCPacketServer replyHandler(int packetId, PacketReplyHandler handler) {
		_replyHandlers.put(packetId, handler);
		return this;
	}
	/**
	 * Registers a new exception handler
	 * @param handler The exception handler
	 * @return This, to be used fluently
	 */
	public TCPacketServer exceptionHandler(ExceptionHandler handler) {
		_exceptionHandlers.add(handler);
		return this;
	}
	
	
	/**
	 * Triggers a server packet event
	 * @param pkt The packet
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketServer triggerPacketHandlers(Packet pkt) {
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
	 * Triggers a server connection event
	 * @param connection The connection
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketServer triggerConnectHandlers(ServerConnection connection) {
		if(_settings.blockingHandlers())
			for(ConnectHandler hdlr : _connectHandlers)
				hdlr.handle(connection);
		else
			for(ConnectHandler hdlr : _connectHandlers)
				_execs.execute(() -> {
					hdlr.handle(connection);
				});
		
		return this;
	}
	/**
	 * Triggers a server disconnection event
	 * @param connection The connection
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketServer triggerDisconnectHandlers(ServerConnection connection) {
		if(_settings.blockingHandlers())
			for(DisconnectHandler hdlr : _disconnectHandlers)
				hdlr.handle(connection);
		else
			for(DisconnectHandler hdlr : _disconnectHandlers)
				_execs.execute(() -> {
					hdlr.handle(connection);
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
	public TCPacketServer triggerPacketReplyHandler(int packetId, Packet pkt) {
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
	public TCPacketServer triggerExceptionHandler(Exception exception) {
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
	
	// Safely read a byte and return -1 if there's an error
	private int safeReadByte(InputStream in) {
		try {
			return in.read();
		} catch(Exception e) {
			return -1;
		}
	}
	
	/**
	 * Starts the server
	 * @throws IOException If starting the server fails
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketServer start() throws IOException {
		_shutDown = false;
		
		// Start TCP server
		_server = new ServerSocket(_settings.bindPort(), 10, InetAddress.getByName(_settings.bindAddress()));
		
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
		
		for(int i = 0; i < _settings.maxConnections(); i++) {
			int threadId = i;
			
			// Create the handler thread
			Thread thread = new Thread(() -> {
				// Loop while the server is running
				while(!_shutDown) {
					try(ServerConnection sock = new ServerConnection(_server.accept(), this)) {
						// Add to connections
						_connections.add(sock);
						
						// Fire connect handlers
						if(_settings.blockingHandlers())
							for(ConnectHandler hdlr : _connectHandlers)
								hdlr.handle(sock);
						else
							for(ConnectHandler hdlr : _connectHandlers)
								_execs.execute(() -> {
									hdlr.handle(sock);
								});
						
						// Buffer the input
						BufferedInputStream in = new BufferedInputStream(sock.socket().getInputStream());
						
						// Buffer for packets
						int leftToRead = 0;
						int leftToSkip = 0;
						byte[] pktBuf = new byte[] {};
						
						// Input loop
						int b = 0;
						while(!sock.socket().isClosed() && (b = safeReadByte(in)) > -1) {
							if(leftToSkip < 1 && leftToRead > 0) {
								pktBuf[pktBuf.length - leftToRead] = (byte) b;
								leftToRead--;
								
								// If finished, parse and handle packet
								if(leftToRead < 1) {
									try {
										// Parse the packet
										Packet pkt = Packet.parsePacket(pktBuf).source(sock.socket());
										
										// Fire reply handler if packet is a reply
										if(pkt.isReply()) {
											triggerPacketReplyHandler(pkt.replyTo(), pkt);
											_replyHandlers.remove(pkt.replyTo());
										}
										
										// Send it to handlers
										triggerPacketHandlers(pkt);
										if(_settings.blockingHandlers())
											sock.triggerPacketHandlers(pkt);
										else
											_execs.execute(() -> {
												sock.triggerPacketHandlers(pkt);
											});
									} catch(Exception e) {
										if(_settings.printErrors()) {
											System.err.println("Error in TCPacketServer loop #"+threadId+':');
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
						
						// Remove connection
						_connections.remove(sock);
						
						// Fire disconnect handlers
						if(_settings.blockingHandlers())
							for(DisconnectHandler hdlr : _disconnectHandlers)
								hdlr.handle(sock);
						else
							for(DisconnectHandler hdlr : _disconnectHandlers)
								_execs.execute(() -> {
									hdlr.handle(sock);
								});
					} catch(Exception e) {
						if(_settings.printErrors()) {
							System.err.println("Error in TCPacketServer loop #"+threadId+':');
							e.printStackTrace();
						}
						
						// Trigger exception handlers
						triggerExceptionHandler(e);
					}
				}
			});
			thread.setName("TCPacketServer-"+i);
			
			// Start thread
			thread.start();
		}
		
		return this;
	}
	
	/**
	 * Closes this server and its resources
	 * @throws IOException If closing server resources fails
	 * @since 1.0
	 */
	public void close() throws IOException {
		_shutDown = true;
		
		// Close resources
		if(_server != null && !_server.isClosed())
			_server.close();
		_connections.clear();
		if(_execs != null)
			_execs.shutdown();
		if(_replyTimeoutTimer != null)
			_replyTimeoutTimer.cancel();
	}
}