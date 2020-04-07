package net.termer.tcpacketprotocol;

import org.junit.Test;

import net.termer.tcpacketprotocol.client.TCPacketClient;
import net.termer.tcpacketprotocol.client.TCPacketClientSettings;
import net.termer.tcpacketprotocol.server.TCPacketServer;
import net.termer.tcpacketprotocol.server.TCPacketServerSettings;

import static org.junit.Assert.*;

import java.io.IOException;

/**
 * Tests basic server capabilities
 * @author termer
 * @since 1.0
 */
public class ServerClientTest {
	// Utility functions to start and return a client or server
	@SuppressWarnings("resource")
	private TCPacketServer server(boolean start) throws IOException {
		TCPacketServer server = new TCPacketServer(
				new TCPacketServerSettings()
				.bindPort(0)
				.printErrors(true)
		).start();
		
		return start ? server.start() : server;
	}
	private TCPacketClient client(TCPacketServer server, boolean conn) throws IOException {
		TCPacketClient client = new TCPacketClient(
				new TCPacketClientSettings()
				.port(server.serverSocket().getLocalPort())
				.printErrors(true)
		);
		return conn ? client.connect() : client;
	}
	
	/**
	 * Test a server's ability to start and stop
	 * @since 1.0
	 */
    @Test public void testServerStartAndClose() {
    	boolean started = false;
        try {
        	TCPacketServer server = server(true);
        	server.close();
        	started = true;
        } catch(Exception e) {
        	e.printStackTrace();
        }
        assertTrue("Assert that server started and stopped", started);
    }
    
    /**
     * Test a client's ability to connect and disconnect from a server
     * @since 1.0
     */
    @Test public void testClientConnectAndClose() {
    	boolean connected = false;
    	try {
    		// Start server
        	TCPacketServer server = server(true);
        	
        	// Connect to server
        	client(server, true);
        	
        	connected = true;
        } catch(Exception e) {
        	e.printStackTrace();
        }
    	assertTrue("Assert that the server started and client connected", connected);
    }
    
    private boolean serverGotPacket = false;
    /**
     * Test a server's ability to handle a packet
     * @since 1.0
     */
    @Test public void testServerPacketReceive() {
    	TCPacketServer server;
		try {
			server = server(true);
			TCPacketClient client = client(server, true);
			
			int triesLeft = 10;
			
			server.packetHandler(pkt -> {
				// When a packet is received, notify that it was gotten
				serverGotPacket = true;
			});
			
			// Try to send a packet
			client.send(new Packet((short) 0).body("Test"));
			
			// Sleep until got a packet or ran out of tries
			while(!serverGotPacket && triesLeft > 0) {
				Thread.sleep(100);
				triesLeft--;
			}
		} catch (Exception e) {
			System.err.println("Error:");
			e.printStackTrace();
		}
		assertTrue("Assert that server recieved a packet", serverGotPacket);
    }
    
    private boolean clientGotPacket = false;
    /**
     * Test a client's ability to handle a packet
     * @since 1.0
     */
    @Test public void testClientPacketReceive() {
    	TCPacketServer server;
		try {
			server = server(true);
			
			int triesLeft = 10;
			
			// Send packet on connection
			server.connectHandler(conn -> {
				try {
					conn.send(new Packet().body("Test"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			
			TCPacketClient client = client(server, false);
			
			// Try to send a packet
			client.packetHandler(pkt -> {
				clientGotPacket = true;
			});
			
			client.connect();
			
			// Sleep until got a packet or ran out of tries
			while(!clientGotPacket && triesLeft > 0) {
				Thread.sleep(100);
				triesLeft--;
			}
		} catch (Exception e) {
			System.err.println("Error:");
			e.printStackTrace();
		}
		assertTrue("Assert that client recieved a packet", clientGotPacket);
    }
    
    private boolean serverGotReply = false;
    /**
     * Test a server's ability to handle a packet reply
     * @since 1.0
     */
    @Test public void testServerPacketReply() {
    	TCPacketServer server;
		try {
			server = server(true);
			TCPacketClient client = client(server, false);
			
			int triesLeft = 50;
			
			// On connect, send packet and wait for reply
			server.connectHandler(conn -> {
				try {
					conn.send(new Packet().body("Test"), (reply, timedOut) -> {
						serverGotReply = !timedOut;
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			
			// Reply to any packet received
			client.packetHandler(pkt -> {
				try {
					pkt.replyWith(new Packet().body("Test"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			
			client.connect();
			
			// Sleep until got a reply or ran out of tries
			while(!serverGotReply && triesLeft > 0) {
				Thread.sleep(100);
				triesLeft--;
			}
		} catch (Exception e) {
			System.err.println("Error:");
			e.printStackTrace();
		}
		assertTrue("Assert that server recieved a reply", serverGotReply);
    }
    
    private boolean clientGotReply = false;
    /**
     * Test a client's ability to handle a packet reply
     * @since 1.0
     */
    @Test public void testClientPacketReply() {
    	TCPacketServer server;
		try {
			server = server(true);
			TCPacketClient client = client(server, true);
			
			int triesLeft = 50;
			
			// Reply to received packets
			server.packetHandler(pkt -> {
				try {
					pkt.replyWith(new Packet().body("Test"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			
			// Send packet and get reply
			client.send(new Packet().body("Test"), (pkt, timedOut) -> {
				clientGotReply = !timedOut;
			});
			
			
			// Sleep until got a reply or ran out of tries
			while(!clientGotReply && triesLeft > 0) {
				Thread.sleep(100);
				triesLeft--;
			}
		} catch (Exception e) {
			System.err.println("Error:");
			e.printStackTrace();
		}
		assertTrue("Assert that client recieved a reply", clientGotReply);
    }
}