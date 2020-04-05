package net.termer.tcpacketprotocol.server;

/**
 * Interface for disconnection handlers to implement
 * @author termer
 * @since 1.0
 */
public interface DisconnectHandler {
	public void handle(ServerConnection connection);
}