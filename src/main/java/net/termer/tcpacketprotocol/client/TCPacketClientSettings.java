package net.termer.tcpacketprotocol.client;

/**
 * Configuration class for TCPacketClient class.
 * @author termer
 * @since 1.0
 */
public class TCPacketClientSettings {
	// Max packet body size
	private int _maxPktBody = 1024;
	private String _addr = "0.0.0.0";
	private int _port = 9006;
	private int _pktHandlePool = 3;
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
	 * Returns the connection address.
	 * Default: "127.0.0.1"
	 * @return The connection address
	 * @since 1.0
	 */
	public String address() {
		return _addr;
	}
	/**
	 * Returns the connection port.
	 * Default: 9006
	 * @return The connection port
	 * @since 1.0
	 */
	public int port() {
		return _port;
	}
	/**
	 * Returns the packet event handler execution thread pool size.
	 * Default: 3
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
	 * Returns whether the client will print error messages and stack traces to System.err
	 * Default: false 
	 * @return Whether the client will print error messages and stack traces
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
	public TCPacketClientSettings maxPacketBodySize(int max) {
		_maxPktBody = max;
		return this;
	}
	/**
	 * Sets the connection address
	 * @param max The connection address
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketClientSettings address(String address) {
		_addr = address;
		return this;
	}
	/**
	 * Sets the connection port
	 * @param max The connection port
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketClientSettings port(int port) {
		_port = port;
		return this;
	}
	/**
	 * Sets the packet event handler execution thread pool size
	 * @param size The packet event handler execution pool size
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketClientSettings packetHandlerPoolSize(int size) {
		_pktHandlePool = size;
		return this;
	}
	/**
	 * Sets whether packet handlers will block the connection's handler thread
	 * @param blocking Whether packet handlers block connection handling threads
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketClientSettings blockingHandlers(boolean blocking) {
		_blockingHdlrs = blocking;
		return this;
	}
	/**
	 * Sets whether the client will print error messages and stack traces to System.err
	 * @param print Whether the client will print error messages and stack traces 
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public TCPacketClientSettings printErrors(boolean print) {
		_printErrs = print;
		return this;
	}
}
