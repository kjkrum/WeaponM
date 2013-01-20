package krum.weaponm.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SocketChannel;

import javax.swing.JOptionPane;

import krum.jplex.UnderflowException;
import krum.weaponm.WeaponM;
import krum.weaponm.database.DataParser;
import krum.weaponm.database.LoginOptions;
import krum.weaponm.emulation.Emulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// thread's job is to read from network and write to db & emu
public class NetworkThread extends Thread {
	protected static final Logger log = LoggerFactory.getLogger(NetworkThread.class);
	protected static int NAME_COUNTER;

	protected final NetworkManager manager;
	protected final DataParser parser;
	protected final Emulation emulation;
	protected final ByteBuffer readBuffer = ByteBuffer.allocateDirect(NetworkManager.BUFFER_SIZE);
	protected final CharBuffer parserBuffer = CharBuffer.allocate(NetworkManager.BUFFER_SIZE);
	protected final CharBuffer emulationBuffer = CharBuffer.allocate(NetworkManager.BUFFER_SIZE);
	protected SocketChannel channel;
	
	public NetworkThread(NetworkManager manager) {
		this.manager = manager;
		parser = manager.weapon.dbm.getDataParser();
		emulation = manager.weapon.emulation;
		synchronized(NetworkThread.class) {
			setName("Network-" + NAME_COUNTER);
			++NAME_COUNTER;
		}
	}
	
	@Override
	public void run() {
		log.info("network thread started");
		
		// establish connection
		try {
			LoginOptions options = manager.weapon.dbm.getDatabase().getLoginOptions();
			channel = SocketChannel.open(new InetSocketAddress(options.getHost(), options.getPort()));
			channel.configureBlocking(true);
			synchronized(manager) {
				manager.channel = channel;		
				// this satisfies TWGS we're a proper Telnet client
				manager.write("\u00FF\u00FC\u00F6");
			}
			parser.reset();
		} catch(IOException e) {
			log.error("network error", e);
			manager.weapon.gui.threadSafeMessageDialog(e.getMessage(), "Network Error", JOptionPane.ERROR_MESSAGE);
			manager.disconnect();
			log.info("network thread exiting");
			return;
		} finally {
			// notify thread in blocking connect
			synchronized(this) {
				notify();
			}
		}

		// main loop
		try {
			while(!isInterrupted()) {
				int bytesRead = channel.read(readBuffer);
				//log.debug("bytes read: {}", bytesRead);
				//System.err.println("bytes read: " + bytesRead);
				if(bytesRead == -1) break;
				
				// convert and copy buffers
				readBuffer.flip();
				int limit = Math.min(parserBuffer.remaining(), emulationBuffer.remaining());
				limit = Math.min(limit, readBuffer.remaining());
				for(int i = 0; i < limit; ++i) {
					char c = (char)(readBuffer.get() & 0xff);
					parserBuffer.put(c);
					emulationBuffer.put(c);
					//System.err.print(c);
				}
				//System.err.println("~");
				readBuffer.compact();
				
				// write to data parser
				parserBuffer.flip();
				try {
					int pos = parserBuffer.position();
					while(parserBuffer.hasRemaining()) {
						pos += parser.parse(parserBuffer, 0, parserBuffer.length(), false);
						//pos += parser.parse(parserBuffer, 0, parserBuffer.length(), true);
						parserBuffer.position(pos);
					}
				}
				catch(UnderflowException e) {
					// this is normal...
					if(parserBuffer.length() == parserBuffer.capacity()) {
						// ...but if *this* ever happens, something
						// is seriously wrong with the lexer rules
						throw e;
					}
				}
				parserBuffer.compact();
				
				// write to emulation
				emulationBuffer.flip();
				try {
					//System.out.println("writing " + emulationBuffer.remaining() + " chars to emulation");
					int pos = emulationBuffer.position();
					while(emulationBuffer.hasRemaining()) {
						//pos += emulation.write(emulationBuffer, 0, emulationBuffer.length(), false);
						int written = emulation.write(emulationBuffer, 0, emulationBuffer.length(), false);
						pos += written;
						if(WeaponM.DEBUG_ANSI) {
							// convert and print chars parsed
							System.err.print(AnsiConverter.convert(emulationBuffer, 0, written));
						}
						emulationBuffer.position(pos);
					}
				}
				catch(UnderflowException e) {
					//System.out.println("emulation underflow - buffer contains: " + emulationBuffer);
					// this is normal...
					if(emulationBuffer.length() == emulationBuffer.capacity()) {
						// ...but if *this* ever happens, something
						// is seriously wrong with the lexer rules
						throw e;
					}
				}
				emulationBuffer.compact();
			}
		} catch(ClosedByInterruptException e) {
			// this is the normal result of a commanded disconnect
		} catch(Throwable t) {
			log.error("unspecified error", t);
			String msg = t.getMessage();
			if(msg == null) msg = t.getClass().getName();
			manager.weapon.gui.threadSafeMessageDialog(msg, "Error", JOptionPane.ERROR_MESSAGE);
		} finally {
			manager.disconnect();
			log.info("network thread exiting");
		}
	}	
}
