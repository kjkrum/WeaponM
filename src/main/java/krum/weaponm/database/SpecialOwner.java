package krum.weaponm.database;

/**
 * Special owner identities.
 */
public enum SpecialOwner implements Owner {
	ALIEN,	// all alien races
	THE_FEDERATION, // needed for anything?
	SPACE_PIRATES, // maybe needed in the future
	ROGUE_MERCENARIES,
	ABANDONED,
	UNKNOWN; // fig hits, etc.
	
	@Override
	public String getName() {
		StringBuilder sb = new StringBuilder();
		String name = name();
		boolean cap = true;
		for(int i = 0; i < name.length(); ++i) {
			char c = name.charAt(i);
			if(c == '_') {
				sb.append(' ');
				cap = true;
			}
			else if(cap == true) {
				sb.append(c);
				cap = false;
			}
			else {
				sb.append(Character.toLowerCase(c));
			}
		}
		return sb.toString();
	}
	
	/*
	public static void main(String[] args) {
		for(SpecialOwner o : SpecialOwner.values()) {
			System.out.println(o.getName());
		}
	}
	*/
}
