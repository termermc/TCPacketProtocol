package net.termer.tcpacketprotocol;

import java.time.Instant;
import java.util.Date;

/**
 * Handler for packet replies
 * @author termer
 * @since 1.0
 */
public class PacketReplyHandler {
	public final Date timeoutDate;
	public final ReplyPacketHandler handler;
	
	/**
	 * Defines a new packet reply handler
	 * @param timeoutSec The time in seconds before the handler is called with a timed out status
	 * @param handler The handler
	 * @since 1.0
	 */
	public PacketReplyHandler(int timeoutSec, ReplyPacketHandler handler) {
		this.timeoutDate = new Date(Instant.now().toEpochMilli() + (timeoutSec * 1000));
		this.handler = handler;
	}
}