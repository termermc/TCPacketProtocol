package net.termer.tcpacketprotocol;

/**
 * Interfacer for packet handling
 * @author termer
 * @since 1.0
 */
public interface PacketHandler {
	public void handle(Packet packet);
}