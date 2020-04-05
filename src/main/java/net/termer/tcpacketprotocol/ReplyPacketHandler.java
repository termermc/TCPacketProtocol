package net.termer.tcpacketprotocol;

/**
 * Interfacer for handling packet replies
 * @author termer
 * @since 1.0
 */
public interface ReplyPacketHandler {
	public void handle(Packet packet, boolean timedOut);
}