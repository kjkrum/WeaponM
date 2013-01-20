package krum.weaponm.database;

import java.io.Serializable;

/**
 * Your personal game settings.  These settings are accessible from the
 * Computer prompt, option &lt;N&gt;.
 */
public class PersonalSettings implements Serializable {
	private static final long serialVersionUID = 1425444818948697839L;

	// TODO: set default values to game defaults
	private volatile boolean ansi;
	private volatile boolean animation;
	private volatile boolean page;
	private volatile int subspaceChannel;
	private volatile boolean fedComm;
	private volatile boolean privateHails;
	private volatile boolean silenceAll;
	private volatile boolean abortOnAnyKey;
	private volatile boolean compactMessages;
	private volatile boolean screenPauses;
	private volatile boolean onlineAutoFlee;
	private volatile boolean sectorAutoReturn;
	
	/**
	 * True if ANSI is on.  Weapon M will not parse data if ANSI is off,
	 * because turning ANSI off opens you to all kinds of spoofing attacks.
	 */
	boolean isAnsi() {
		return ansi;
	}
	
	void setAnsi(boolean ansi) {
		this.ansi = ansi;
	}
	
	/**
	 * True if animation is on.  Turn animation off if you care about speed.
	 */
	boolean isAnimation() {
		return animation;
	}
	
	void setAnimation(boolean animation) {
		this.animation = animation;
	}
	
	/**
	 * If true, you will receive an ASCII bell character every time you
	 * receive a message.  Weapon M's terminal currently ignores the bell
	 * character.
	 */
	boolean isPage() {
		return page;
	}
	
	void setPage(boolean page) {
		this.page = page;
	}
	
	/**
	 * Returns the subspace radio channel you're currently using.
	 */
	int getSubspaceChannel() {
		return subspaceChannel;
	}
	
	void setSubspaceChannel(int subspaceChannel) {
		this.subspaceChannel = subspaceChannel;
	}
	
	/**
	 * If true, you will receive global public chat messages.
	 */
	boolean isFedComm() {
		return fedComm;
	}
	
	void setFedComm(boolean fedComm) {
		this.fedComm = fedComm;
	}
	
	/**
	 * If true, other traders will be able to send you private hails.
	 */
	boolean isPrivateHails() {
		return privateHails;
	}
	
	void setPrivateHails(boolean privateHails) {
		this.privateHails = privateHails;
	}
	
	/**
	 * If true, all messages will be silenced.  This is not recommended as it
	 * may result in missing important information.  Scripts for other helpers
	 * sometimes silenced messages to avoid interference from other scripts.
	 * Weapon M scripts should lock the network instead.
	 */
	boolean isSilenceAll() {
		return silenceAll;
	}
	
	void setSilenceAll(boolean silenceAll) {
		this.silenceAll = silenceAll;
	}
	
	/**
	 * If true, displays can be aborted by sending any key.  If false,
	 * displays can only be aborted by sending a space.
	 */
	boolean isAbortOnAnyKey() {
		return abortOnAnyKey;
	}
	
	void setAbortOnAnyKey(boolean abortOnAnyKey) {
		this.abortOnAnyKey = abortOnAnyKey;
	}
	
	/**
	 * If true, messages will be displayed in a compact format.
	 */
	boolean isCompactMessages() {
		return compactMessages;
	}
	
	void setCompactMessages(boolean compactMessages) {
		this.compactMessages = compactMessages;
	}
	
	/**
	 * If true, you will receive a pause prompt after every page of text.
	 */
	boolean isScreenPauses() {
		return screenPauses;
	}
	
	void setScreenPauses(boolean screenPauses) {
		this.screenPauses = screenPauses;
	}
	
	/**
	 * If true, you will automatically attempt to flee if attacked while
	 * online.
	 */
	boolean isOnlineAutoFlee() {
		return onlineAutoFlee;
	}
	
	void setOnlineAutoFlee(boolean onlineAutoFlee) {
		this.onlineAutoFlee = onlineAutoFlee;
	}
	
	/**
	 * If true, typing a sector number at the Command prompt may not require
	 * pressing return.  Auto-return is supposed to activate if entering one
	 * more digit would make the input an invalid sector number.  But it's
	 * rather buggy, and not likely to be fixed because the bug is ancient and
	 * many helpers and scripts have been written to work around it.  Scripts
	 * should generally use the &lt;M&gt;move command instead of entering
	 * sector numbers directly at the Command prompt.
	 */
	boolean isSectorAutoReturn() {
		return sectorAutoReturn;
	}
	
	void setSectorAutoReturn(boolean sectorAutoReturn) {
		this.sectorAutoReturn = sectorAutoReturn;
	}
	
}
