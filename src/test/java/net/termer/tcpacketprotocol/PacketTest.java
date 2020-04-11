package net.termer.tcpacketprotocol;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests packet utilities
 * @author termer
 * @since 1.0
 */
public class PacketTest {
	/**
	 * Class used for testing Object (de)serialization
	 * @since 1.0
	 */
	public class TestObject {
		// The following types can be serialized: byte, boolean, short, char, int, float, long, double, String.
		public byte bt = 69;
		public boolean bool = true;
		public short shrt = 1337;
		public char ch = '!';
		public int i = 8080;
		public float flt = 101;
		public long lng = 1234567890;
		public double dbl = 1337.69;
		public String str = "Test";
		
		public TestObject() {}
		
		// Print all values in a colon-divided String
		public String toString() {
			return bt+":"+bool+":"+shrt+":"+ch+":"+flt+":"+lng+":"+dbl+":"+str;
		}
	}
	
	/**
	 * Expected body of a clean serialized TestObject
	 * @since 1.0
	 */
	public byte[] properBody = new byte[] {69,1,5,57,0,33,0,0,31,-112,66,-54,0,0,65,-99,111,52,84,0,0,0,64,-108,-26,-62,-113,92,40,-10,0,0,0,4,84,101,115,116};
	/**
	 * Expected toString() of a clean TestObject
	 * @since 1.0
	 */
	public String properToString = "69:true:1337:!:8080:101:1234567890:1337.69:Test";
	
	/**
	 * Test the Object serialization utility
	 * @since 1.0
	 */
    @Test public void testObjectSerialize() {
        try {
        	byte[] body = Packet.objectToPacketBody(new TestObject());
        	assertArrayEquals(properBody, body);
        } catch(Exception e) {
        	e.printStackTrace();
        }
    }
    
    /**
	 * Test the Object deserialization utility
	 * @since 1.0
	 */
    @Test public void testObjectDeserialize() {
        try {
        	TestObject obj = (TestObject) Packet.packetBodyToObject(properBody, TestObject.class);
        	assertEquals(properToString, obj.toString());
        } catch(Exception e) {
        	e.printStackTrace();
        }
    }
}