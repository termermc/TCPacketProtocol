package net.termer.tcpacketprotocol;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import net.termer.tcpacketprotocol.util.IntGenerator;

/**
 * Main class for storing, creating, and sending packets.
 * @author termer
 * @since 1.0
 */
public class Packet {
	// Type for the packet
	private final short _type;
	// The body of the packet
	private byte[] _body = {};
	// The ID of the packet
	private int _id = Integer.MIN_VALUE;
	// The ID this packet is replying to
	private int _replyId = Integer.MIN_VALUE;
	// Sending flags for this packet
	private boolean _expectReply = false;
	private boolean _reply = false;
	// Socket this came from
	private Socket _source = null;
	
	/**
	 * Creates a new packet with the specified type
	 * @param type The type for this packet
	 * @since 1.0
	 */
	public Packet(short type) {
		_type = type;
	}
	/**
	 * Creates a new packet with the default type
	 * @since 1.0
	 */
	public Packet() {
		_type = 0;
	}
	/**
	 * Creates a new packet with the default type from the provided source
	 * @param source The Socket this packet was sent from
	 * @since 1.0
	 */
	public Packet(Socket source) {
		_type = 0;
		_source = source;
	}
	/**
	 * Creates a new packet with the specified type and ID
	 * @param type The type for this packet
	 * @param id The ID of this packet
	 * @since 1.0
	 */
	public Packet(short type, int id) {
		_type = type;
		_id = id;
	}
	/**
	 * Creates a new packet with the specified type
	 * @param type The type for this packet
	 * @param source The Socket this packet was sent from
	 * @since 1.0
	 */
	public Packet(short type, Socket source) {
		_type = type;
		_source = source;
	}
	/**
	 * Creates a new packet with the specified type and ID
	 * @param type The type for this packet
	 * @param id The ID of this packet
	 * @param source The Socket this packet was sent from
	 * @since 1.0
	 */
	public Packet(short type, int id, Socket source) {
		_type = type;
		_id = id;
		_source = source;
	}
	
	/**
	 * Returns this packet's body
	 * @return The packet's body
	 * @since 1.0
	 */
	public byte[] body() {
		return _body;
	}
	
	/**
	 * Returns this packet's body as a String
	 * @return The packet's body as a String
	 * @since 1.0
	 */
	public String bodyAsString() {
		return new String(_body);
	}
	/**
	 * Returns this packet's body as a String
	 * @param charset The charset to use when decoding the body
	 * @return The packet's body as a String
	 * @since 1.0
	 */
	public String bodyAsString(Charset charset) {
		return new String(_body, charset);
	}
	/**
	 * Returns this packet's body as an Object
	 * Fields are used to deserialize packets, and read sequentially
	 * Provided class must have a public constructor that requires no arguments, or an IllegalAccessException will be thrown.
	 * The following types can be serialized: byte, boolean, short, char, int, float, long, double, String.
	 * @param type The Object type to deserialize this packet's body to
	 * @return The packet body as an Object
	 * @throws InstantiationException If an Object of the class type specified cannot be instantiated
	 * @throws IllegalArgumentException If a value is provided that is not on the list of types this method can serialize.
	 * @throws IllegalAccessException If the Java reflection methods required to serialize fails
	 * @since 1.0
	 */
	public Object bodyAsObject(Class<? extends Object> type) throws InstantiationException, IllegalAccessException {
		return packetBodyToObject(_body, type);
	}
	
	/**
	 * Returns whether this packet is expecting a reply
	 * @return Whether this packet is expecting a reply
	 * @since 1.0
	 */
	public boolean expectingReply() {
		return _expectReply;
	}
	
	/**
	 * Returns the type for this packet
	 * @return The type for this packet
	 * @since 1.0
	 */
	public int type() {
		return _type;
	}
	
	/**
	 * Returns the ID of the packet this packet is replying to, or Integer.MIN_VALUE if not a reply
	 * @return Returns the ID of the packet this packet is replying to
	 * @since 1.0
	 */
	public int replyTo() {
		return _replyId;
	}
	
	/**
	 * Returns whether this packet is a reply to another
	 * @return Whether this packet is a reply
	 * @since 1.0
	 */
	public boolean isReply() {
		return _reply;
	}
	
	/**
	 * This packet's ID, Integer.MIN_VALUE if none has been assigned
	 * @return This packet's ID
	 * @since 1.0
	 */
	public int id() {
		return _id;
	}
	
	/**
	 * Returns the socket this Packet was sent from (may be null)
	 * @return The socket this Packet came from
	 * @since 1.0
	 */
	public Socket source() {
		return _source;
	}
	
	/**
	 * Sets this packet's body
	 * @param body The packet's body
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public Packet body(byte[] body) {
		_body = body;
		return this;
	}
	/**
	 * Sets this packet's body as a String
	 * @param body The packet's body
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public Packet body(String body) {
		_body = body.getBytes();
		return this;
	}
	/**
	 * Sets this packet's body as a String
	 * @param body The packet's body
	 * @param charset The charset to use for the String
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public Packet body(String body, Charset charset) {
		_body = body.getBytes(charset);
		return this;
	}
	/**
	 * Sets this packet's body as a serialized Object.
	 * Fields are serialized into a packet body which can later be deserialized into another Object of the same type containing the same field values.
	 * The following types can be serialized: byte, boolean, short, char, int, float, long, double, String.
	 * @param obj The Object to use for the packet body
	 * @return This, to be used fluently
	 * @throws IllegalArgumentException If a value is provided that is not on the list of types this method can serialize.
	 * @throws IllegalAccessException If the Java reflection methods required to serialize fails
	 * @since 1.0
	 */
	public Packet body(Object obj) throws IllegalArgumentException, IllegalAccessException {
		_body = objectToPacketBody(obj);
		return this;
	}
	
	/**
	 * Sets this packet as a reply to another
	 * @param packetId The ID of the packet this packet is replying to 
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public Packet setReplyTo(int packetId) {
		_replyId = packetId;
		_reply = true;
		return this;
	}
	
	/**
	 * Sets whether this packet is expecting a reply
	 * @param expecting Whether this packet is expecting a reply
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public Packet expectingReply(boolean expecting) {
		_expectReply = expecting;
		if(_id == Integer.MIN_VALUE)
			_id = IntGenerator.nextInt();
		return this;
	}
	
	/**
	 * Sets the socket this Packet was sent from (may be null)
	 * @param source The socket this Packet came from
	 * @return This, to be used fluently
	 * @since 1.0
	 */
	public Packet source(Socket source) {
		_source = source;
		return this;
	}
	
	/**
	 * Returns the bytes for this packet
	 * @return This packet's bytes
	 * @since 1.0
	 */
	public byte[] toBytes() {
		// Calculate size
		byte type = 0;
		if(_expectReply)
			type = 1;
		else if(_reply)
			type = 2;
		int size = 3 + (type == 0 ? 0 : 4) + _body.length;
		
		// Create buffer
		ByteBuffer buf = ByteBuffer.allocate(size);
		
		// Put data in buffer
		buf
			.putShort(_type)
			.put(type);
		if(type == 1)
			buf.putInt(_id);
		else if(type == 2)
			buf.putInt(_replyId);
		
		buf.put(_body);
		
		return buf.array();
	}
	
	/**
	 * Sends this packet to the provided OutputStream
	 * @param out The OutputStream to write this packet to
	 * @throws IOException If writing to the stream fails
	 * @since 1.0
	 */
	public void sendTo(OutputStream out) throws IOException {
		byte[] bytes = toBytes();
		ByteBuffer buf = ByteBuffer.allocate(bytes.length+4)
				.putInt(bytes.length)
				.put(bytes);
		
		out.write(buf.array());
	}
	
	/**
	 * Replies to this packet with the provided packet to the specified destination
	 * @param pkt The packet to reply with
	 * @param destination The destination to send the reply to
	 * @throws IOException If sending the packet fails
	 * @throws IllegalStateException If this packet is not excepting a reply
	 * @since 1.0
	 */
	public void replyWith(Packet pkt, OutputStream destination) throws IOException {
		if(_expectReply) {
			pkt
					.setReplyTo(_id)
					.sendTo(destination);
		} else {
			throw new IllegalStateException("Cannot reply to packet that is not expecting a reply");
		}
	}
	/**
	 * Replies to this packet with the provided packet
	 * @param pkt The packet to reply with
	 * @throws IOException If sending the packet fails
	 * @throws IllegalStateException If this packet is not excepting a reply, or if source() is null
	 * @since 1.0
	 */
	public void replyWith(Packet pkt) throws IOException {
		if(_expectReply) {
			if(_source == null) {
				throw new IllegalStateException("Packet source is null");
			} else {
				pkt
						.setReplyTo(_id)
						.sendTo(_source.getOutputStream());
			}
		} else {
			throw new IllegalStateException("Cannot reply to packet that is not expecting a reply");
		}
	}
	
	/**
	 * Parses an array of bytes into a Packet object
	 * @param bytes The bytes to parse
	 * @return The Packet object parsed from the bytes
	 * @throws MalformedPacketException If parsing the bytes failed
	 * @since 1.0
	 */
	public static Packet parsePacket(byte[] bytes) throws MalformedPacketException {
		try {
			ByteBuffer buf = ByteBuffer.wrap(bytes);
			
			short type = buf.getShort();
			byte pktType = buf.get();
			int id = Integer.MIN_VALUE;
			if(pktType != 0)
				id = buf.getInt();
			
			int offset = 3 + (pktType == 0 ? 0 : 4);
			
			byte[] body = new byte[buf.array().length - offset];
			buf.get(body);
			
			Packet pkt = pktType == 1 ? new Packet(type, id) : new Packet(type);
			if(pktType == 1)
				pkt.expectingReply(true);
			else if(pktType == 2)
				pkt.setReplyTo(id);
			pkt.body(body);
			
			return pkt;
		} catch(Exception e) {
			throw new MalformedPacketException(e.getMessage());
		}
	}
	
	/**
	 * Serializes a Java object into a packet body.
	 * Fields are serialized into a packet body which can later be deserialized into another Object of the same type containing the same field values.
	 * The following types can be serialized: byte, boolean, short, char, int, float, long, double, String.
	 * @param obj The object to serialize
	 * @return The packet body
	 * @throws IllegalArgumentException If a value is provided that is not on the list of types this method can serialize.
	 * @throws IllegalAccessException If the Java reflection methods required to serialize fails
	 * @since 1.0
	 */
	public static byte[] objectToPacketBody(Object obj) throws IllegalArgumentException, IllegalAccessException {
		Class<? extends Object> cls = obj.getClass();
		int size = 0;
		for(Field field : cls.getDeclaredFields()) {
			// Bypass Java type checks to improve speed
			field.setAccessible(true);
			
			Class<? extends Object> type = field.getType(); 
			
			// Increment size
			if(type == String.class)
				size += 4+((String) field.get(obj)).length();
			else if(type == byte.class || type == boolean.class)
				size++;
			else if(type == short.class || type == char.class)
				size+=2;
			else if(type == int.class || type == float.class)
				size+=4;
			else if(type == long.class || type == double.class)
				size+=8;
			else
				throw new IllegalArgumentException("Cannot convert Objects other than String to a packet body!");
		}
		
		// Setup ByteBuffer and put values
		ByteBuffer buf = ByteBuffer.allocate(size);
		
		// Put values
		for(Field field : cls.getDeclaredFields()) {
			// Bypass Java type checks to improve speed
			field.setAccessible(true);
			
			Class<? extends Object> type = field.getType(); 
			
			// Put values in buffer
			if(type == String.class)
				buf
						.putInt(((String) field.get(obj)).length())
						.put(((String) field.get(obj)).getBytes(Charset.forName("UTF-8")));
			else if(type == byte.class)
				buf.put(field.getByte(obj));
			else if(type == boolean.class)
				buf.put(field.getBoolean(obj) ? (byte) 1 : (byte) 0);
			else if(type == short.class)
				buf.putShort(field.getShort(obj));
			else if(type == char.class)
				buf.putChar(field.getChar(obj));
			else if(type == int.class)
				buf.putInt(field.getInt(obj));
			else if(type == float.class)
				buf.putFloat(field.getFloat(obj));
			else if(type == long.class || type == double.class)
				buf.putDouble(field.getDouble(obj));
			else
				throw new IllegalArgumentException("Cannot convert Objects other than String to a packet body!");
		}
		
		return buf.array();
	}
	
	/**
	 * Deserializes a packet body into a Java Object.
	 * Fields are used to deserialize packets, and read sequentially
	 * Provided class must have a public constructor that requires no arguments, or an IllegalAccessException will be thrown.
	 * The following types can be serialized: byte, boolean, short, char, int, float, long, double, String.
	 * @param body The packet body to deserialize
	 * @param objectType The class of the Object to create from this packet body
	 * @return The Object created from the packet body
	 * @throws InstantiationException If an Object of the class type specified cannot be instantiated
	 * @throws IllegalArgumentException If a value is provided that is not on the list of types this method can serialize.
	 * @throws IllegalAccessException If the Java reflection methods required to serialize fails
	 * @since 1.0
	 */
	public static Object packetBodyToObject(byte[] body, Class<? extends Object> objectType) throws InstantiationException, IllegalAccessException {
		ByteBuffer buf = ByteBuffer.wrap(body);
		Object obj = objectType.newInstance();
		Class<? extends Object> cls = obj.getClass();
		
		// Iterate over fields and read body
		for(Field field : cls.getDeclaredFields()) {
			// Bypass Java type checks to improve speed
			field.setAccessible(true);
			
			Class<? extends Object> type = field.getType(); 
			
			// Assign fields
			if(type == String.class) {
				// Read string
				byte[] bytes = new byte[buf.getInt()];
				buf.get(bytes);
				
				// Set field
				field.set(obj, new String(bytes, Charset.forName("UTF-8")));
			} else if(type == byte.class) {
				field.set(obj, buf.get());
			} else if(type == boolean.class) {
				field.set(obj, buf.get() == 1 ? true : false);
			} else if(type == short.class) {
				field.set(obj, buf.getShort());
			} else if(type == char.class) {
				field.set(obj, buf.getChar());
			} else if(type == int.class) {
				field.set(obj, buf.getInt());
			} else if(type == float.class) {
				field.set(obj, buf.getFloat());
			} else if(type == long.class || type == double.class) {
				field.set(obj, buf.getDouble());
			} else {
				throw new IllegalArgumentException("Cannot convert Objects other than String from a packet body!");
			}
		}
		
		return obj;
	}
}