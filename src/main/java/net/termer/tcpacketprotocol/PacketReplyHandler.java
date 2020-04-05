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
	public final PacketHandler handler;
	
	public PacketReplyHandler(int timeoutSec, PacketHandler handler) {
		this.timeoutDate = new Date(Instant.now().toEpochMilli() + (timeoutSec * 1000));
		this.handler = handler;
	}
}