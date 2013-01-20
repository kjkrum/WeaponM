package krum.weaponm.network;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import krum.jplex.UnderflowException;
import krum.weaponm.WeaponM;
import krum.weaponm.gui.GUI;
import krum.weaponm.script.ScriptEvent;

public class NetworkManager {
	public static final int BUFFER_SIZE = 8192;
	
	protected final WeaponM weapon;
	protected final ByteBuffer writeBuffer;
	protected NetworkThread thread;
	protected SocketChannel channel;
	protected int totalBytesWritten;
	
	public NetworkManager(WeaponM weapon) {
		this.weapon = weapon;
		writeBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
	}
	
	synchronized public boolean isConnected() {
		return thread != null;
	}
	
	synchronized public void connect() throws IOException {
		if(thread != null) return;
		thread = new NetworkThread(this);
		thread.start();
		weapon.gui.firePropertyChange(GUI.NETWORK_ACTIVE, false, true);
		weapon.scripts.fireEvent(ScriptEvent.CONNECTING);
	}
	
	public void blockingConnect() throws IOException, InterruptedException {
		synchronized(this) {
			if(thread != null) return;
			thread = new NetworkThread(this);
			thread.start();
			weapon.gui.firePropertyChange(GUI.NETWORK_ACTIVE, false, true);
			weapon.scripts.fireEvent(ScriptEvent.CONNECTING);
		}
		synchronized(thread) {
			wait();
		}
	}
	
	synchronized public void disconnect() {
		if(thread != null) {
			thread.interrupt();
			thread = null;
			weapon.gui.firePropertyChange(GUI.NETWORK_ACTIVE, true, false);
			weapon.scripts.fireEvent(ScriptEvent.DISCONNECTING);
		}
		if(channel != null) {
			try {
				channel.close();
			} catch(IOException e) {
				// whatever
			}
			channel = null;
			String msg = "\r\n\r\n\033[1;31m<< Disconnected >>\033[0m\r\n";
			try {
				weapon.emulation.write(msg, 0, msg.length(), true);
			} catch (UnderflowException e) {
				// can't underflow if endOfInput == true
			}
		}
	}
	
	synchronized public int write(ByteBuffer buf) throws IOException {
		if(channel == null) throw new IOException("not connected");
		int writtenThisMethod = 0;
		while(buf.hasRemaining()) {
			int writtenThisLoop = channel.write(buf);
			writtenThisMethod += writtenThisLoop;
			totalBytesWritten += writtenThisLoop;
		}
		return writtenThisMethod;
	}
	
	synchronized public int write(CharSequence seq) throws IOException {
		// FIXME: do in chunks if seq.length() > writeBuffer.remaining();
		if(channel == null) throw new IOException("not connected");
		int len = seq.length();
		for(int i = 0; i < len; ++i) {
			writeBuffer.put((byte) seq.charAt(i));
		}
		writeBuffer.flip();
		int written = write(writeBuffer);
		writeBuffer.clear();
		return written;
	}
	
	synchronized public int getTotalBytesWritten() {
		return totalBytesWritten;
	}
}
