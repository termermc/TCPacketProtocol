package net.termer.tcpacketprotocol.server;

/**
 * Configuration class for TCPacketServer class.
 * @author termer
 * @since 1.0
 */
public class TCPacketServerSettings {
	// Max packet body size
	private int _maxPktBody = 1024;
	private int _maxConns = 20;
	private String _bindAddr = "0.0.0.0";
	private int _bindPort = 9006;
	private int _pktHandlePool = 10;
	private boolean _blockingHdlrs = false;
	private boolean _printErrs = false;
	
	/**
	 * Returns the max packet body size in bytes.
	 * Default: 1024
	 * @return The max size packet bodies
	 * @since 1.0
	 */
	public int maxPacketBodySize() {
		return _maxPktBody;
	}
	/**
	 * Returns the server connection limit.
	 * Default: 20
	 * @return The server connection limit
	 * @since 1.0
	 */
	public int maxConnections() {
		return _maxConns;
	}
	/**
	 * Returns the server bind address.
	 * Default: "0.0.0.0"
	 * @return The server bind address
	 * @since 1.0
	 */
	public String bindAddress() {
		return _bindAddr;
	}
	/**
	 * Returns the server bind port.
	 * Default: 9006
	 * @return The server bind port
	 * @since 1.0
	 */
	public int bindPort() {
		return _bindPort;
	}
	/**
	 * Returns the packet event handler execution thread pool size.
	 * Default: 10
	 * @return The packet event handler execution pool size
	 * @since 1.0
	 */
	public int packetHandlerPoolSize() {
		return _pktHandlePool;
	}
	/**
	 * Returns whether packet handlers will block the connection's handler thread.
	 * Default: false 
	 * @return Whether packet handlers will block
	 * @since 1.0
	 */
	public boolean blockingHandlers() {
		return _blockingHdlrs;
	}
	/**
	 * Returns whether the server will print error messages and stack traces to System.err
	 * Default: false 
	 * @return Whether the server will print error messages and stack traces
	 * @since 1.0
	 */
	public boolean printErrors() {
		return _printErrs;
	}
	
	/**
	 * Sets the max packet body size in bytes
	 * @param max The max body size
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketServerSettings maxPacketBodySize(int max) {
		_maxPktBody = max;
		return this;
	}
	/**
	 * Sets the server connection limit
	 * @param max The connection limit
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketServerSettings maxConnections(int max) {
		_maxConns = max;
		return this;
	}
	/**
	 * Sets the server bind address
	 * @param address The server bind address
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketServerSettings bindAddress(String address) {
		_bindAddr = address;
		return this;
	}
	/**
	 * Sets the server bind port
	 * @param port The server bind port
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketServerSettings bindPort(int port) {
		_bindPort = port;
		return this;
	}
	/**
	 * Sets the packet event handler execution thread pool size
	 * @param size The packet event handler execution pool size
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketServerSettings packetHandlerPoolSize(int size) {
		_pktHandlePool = size;
		return this;
	}
	/**
	 * Sets whether packet handlers will block the connection's handler thread
	 * @param blocking Whether packet handlers block connection handling threads
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketServerSettings blockingHandlers(boolean blocking) {
		_blockingHdlrs = blocking;
		return this;
	}
	/**
	 * Sets whether the server will print error messages and stack traces to System.err
	 * @param print Whether the server will print error messages and stack traces 
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketServerSettings printErrors(boolean print) {
		_printErrs = print;
		return this;
	}
}
