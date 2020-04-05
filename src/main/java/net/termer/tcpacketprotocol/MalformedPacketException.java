package net.termer.tcpacketprotocol;

import java.io.IOException;

/**
 * Exception to be thrown when a byte array cannot be parsed into a packet.
 * @author termer
 * @since 1.0
 */
public class MalformedPacketException extends IOException {
	private static final long serialVersionUID = 1L;

	public MalformedPacketException(String msg) {
		super(msg);
	}
}
