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
	 * Returns this client's Socket
	 * @return This client's Socket
	 * @since 1.0
	 */
	public Socket socket() {
		return _socket;
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
	 * Triggers a client packet event
	 * @param pkt The packet
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketClient triggerPacketHandlers(Packet pkt) {
		if(_settings.blockingHandlers())
			for(PacketHandler hdlr : _packetHandlers)
				hdlr.handle(pkt, false);
		else
			for(PacketHandler hdlr : _packetHandlers)
				_execs.execute(() -> {
					hdlr.handle(pkt, false);
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
	 * Connects to the server
	 * @throws IOException If connection fails
	 * @since 1.0
	 */
	public void connect() throws IOException {
		// Connect
		System.out.println(_settings.address()+":"+_settings.port());
		_socket = new Socket(InetAddress.getByName(_settings.address()), _settings.port());
		
		// Setup event executor thread pool
		_execs = Executors.newFixedThreadPool(_settings.packetHandlerPoolSize());
		
		// Setup reply timeout timer
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
								Packet pkt = Packet.parsePacket(pktBuf);
								
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
	}
	
	/**
	 * Closes this client and all of its resources
	 * @throws IOException If closing resources failed
	 * @since 1.0
	 */
	public void close() throws IOException {
		// Close resources
		if(_socket != null)
			_socket.close();
		if(_execs != null)
			_execs.shutdown();
		if(_replyTimeoutTimer != null)
			_replyTimeoutTimer.cancel();
	}
}
