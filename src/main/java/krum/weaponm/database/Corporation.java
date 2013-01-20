package krum.weaponm.database;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Corporation implements Owner, Serializable {
	private static final long serialVersionUID = 1L;
	
	private final int number;
	private volatile String name;
	private final Set<Trader> members = Collections.synchronizedSet(new HashSet<Trader>());
	
	Corporation(int number) {
		this.number = number;
	}

	@Override
	public String getName() {
		return name;
	}
	
	void setName(String name) {
		this.name = name;		
	}
	
	public int getNumber() {
		return number;
	}
	
	synchronized public Set<Trader> getMembers() {
		return new HashSet<Trader>(members);
	}
	
	void clearMembers() {
		members.clear();
	}
	
	void removeMember(Trader member) {
		members.remove(member);
	}
	
	void addMember(Trader member) {
		members.add(member);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Corp #");
		sb.append(number);
		if(name != null) {
			sb.append(", ");
			sb.append(name);
		}
		return sb.toString();
	}
}
