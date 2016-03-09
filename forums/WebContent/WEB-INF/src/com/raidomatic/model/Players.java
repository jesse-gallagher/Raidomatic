package com.raidomatic.model;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.raidomatic.JSFUtil;
import java.util.*;
import lotus.domino.*;

public class Players extends AbstractCollectionManager<Player> {
	public List<Player> getPlayers() throws Exception {
		return this.getAll();
	}

	@lombok.SneakyThrows
	public Player getByName(String name) {
		Player result = this.getFirstOfCollection(PlayerList.BY_LOOKUP_NAMES, name);
		return result;
	}
	public List<Player> getByNames(List<String> names) {
		List<Player> players = new ArrayList<Player>();

		for(String name : names) {
			Player player = this.getByName(name);
			if(player != null) {
				players.add(player);
			}
		}

		return players;
	}

	public Player getCurrent() {
		return this.getByName(JSFUtil.getUserName());
	}

	@SuppressWarnings("unchecked")
	public List<String> getNames() throws NotesException {
		Database wownames = ExtLibUtil.getCurrentSession().getDatabase("", "wownames.nsf");
		View people = wownames.getView("People");
		people.setAutoUpdate(false);
		List<String> names = people.getColumnValues(1);
		people.recycle();
		return names;
	}

	@Override
	protected AbstractDominoList<Player> createCollection() { return new PlayerList(); }

	private static final long serialVersionUID = -5105831930854850837L;


	public static class PlayerList extends AbstractDominoList<Player> {
		public static final String BY_SHORT_NAME = "ShortName";
		public static final String BY_ID = "$PlayerID";
		public static final String BY_LOOKUP_NAMES = "$LookupNames";

		public static final String DEFAULT_SORT = PlayerList.BY_ID;

		@Override
		protected String[] getColumnFields() {
			return new String[] {
					"id", "shortName", "alternateNames", "", "dispName", "signature", "dispTitle",
					"mailServer", "mailFile", "mailDomain", "mailSystem",
					"contactEmailAddress"
			};
		}
		@Override
		protected String getViewName() { return "Players"; }
		@Override
		protected String getDatabasePath() { return "wownames.nsf"; }
		//protected boolean isSessionAsSigner() { return true; }


		private static final long serialVersionUID = -401865425415543141L;
	}
}