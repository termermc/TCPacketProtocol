package net.termer.tcpacketprotocol.server;

/**
 * Interface for connection handlers to implement
 * @author termer
 * @since 1.0
 */
public interface ConnectHandler {
	public void handle(ServerConnection connection);
}