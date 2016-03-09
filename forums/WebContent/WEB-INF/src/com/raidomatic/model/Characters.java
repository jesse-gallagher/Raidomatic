package com.raidomatic.model;

import java.util.*;

public class Characters extends AbstractCollectionManager<Character> {

	public List<Character> getCharacters() throws Exception {
		return this.getAll();
	}
	public List<Character> getCharactersForPlayer(String player) throws Exception {
		// pre-cache up to 5 characters per player, since that's the likely max boundary
		//System.out.println("Want to search for [Player]=" + player);
		return this.getCollection(CharacterList.BY_PLAYER, player, null, 5);
		//return this.search("[Player]=" + player, CharacterList.BY_NAME);
	}


	public Character getByName(String name) throws Exception {
		//System.out.println("Trying to fetch " + name);
		//return this.getById(name);
		return this.getByAnyName(name);
	}

	public Character getByAnyName(String name) {
		return this.getFirstOfCollection(CharacterList.BY_LOOKUP_NAME, name);
	}

	@Override
	protected AbstractDominoList<Character> createCollection() { return new CharacterList(); }

	private static final long serialVersionUID = 1420005680760474109L;


	public class CharacterList extends AbstractDominoList<Character> {
		public static final String BY_NAME = "Name";
		public static final String BY_ID = "$CharacterID";
		public static final String BY_PLAYER = "Player";
		public static final String BY_LOOKUP_NAME = "$LookupName";

		public static final String DEFAULT_SORT = CharacterList.BY_ID;

		@Override
		protected String[] getColumnFields() {
			return new String[] { "id", "name", "alias", "avatarPath", "className", "classId", "focus", "genderId", "playerName", "preferredSpec",
					"primaryProgression", "primaryProvides", "primarySometimesProvides", "primaryRole", "primarySpec", "primaryTalents", "primaryGroup",
					"secondaryProgression", "secondaryProvides", "secondarySometimesProvides", "secondaryRole", "secondarySpec", "secondaryTalents", "secondaryGroup",
					"raceId", "", "", "level" };
		}
		@Override
		protected String getViewName() { return "Characters"; }
		@Override
		protected String getDatabasePath() { return "wow/common.nsf"; }
		@Override
		protected boolean isSessionAsSigner() { return true; }

		private static final long serialVersionUID = 2487180447123794058L;
	}
}