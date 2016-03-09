package com.raidomatic.model;

import java.util.*;

public class Signups extends AbstractCollectionManager<Signup> {

	public List<Signup> getSignups() throws Exception {
		return this.getAll();
	}
	public List<Signup> getSignupsForTopicId(String topicId) throws Exception {
		//System.out.println("Creating signup list for topic id " + topicId);
		return this.getCollection(SignupList.BY_TOPIC_ID, topicId, null, -1);
	}

	public List<Signup> getSignupsForTopicIdAndPlayerName(String topicId, String checkUserName) {
		//System.out.println("Creating signup list for topic id and player name " + topicId + ", " + checkUserName);
		return this.getCollection(SignupList.BY_TOPIC_ID_AND_PLAYER_NAME, topicId + "-" + checkUserName, null, 1);
	}
	public List<Signup> getSignupsForPlayerName(String playerName) {
		//System.out.println("Creating signup list for player " + playerName);
		return this.getCollection(SignupList.BY_PLAYER_NAME, playerName);
	}
	public List<Signup> getSignupsForPlayerNameAndLootTierId(String playerName, String lootTierId) throws Exception {
		return this.search("[Username]=" + playerName + " & [EventLootTier]=" + lootTierId);
	}
	public List<Signup> getSignupsForCharacterName(String characterName) {
		//System.out.println("Creating signup list for character " + characterName);
		return this.getCollection(SignupList.BY_CHARACTER_NAME, characterName);
	}
	public List<Signup> getAttendancesForPlayerName(String playerName) throws Exception {
		//return this.getCollection(SignupList.BY_PLAYER_NAME_AND_ATTENDED, playerName + "1");
		return this.search("[Username]=" + playerName + " and [Attended]=1");
	}
	public List<Signup> getAttendencesForPlayerNameAndLootTierId(String playerName, String lootTierId) throws Exception {
		//return this.getCollection(SignupList.BY_PLAYER_NAME_ATTENDED_AND_LOOT_TIER, playerName + "1" + lootTierId);
		return this.search("[Username]=" + playerName + " and [Attended]=1 and [EventLootTier]=" + lootTierId);
	}

	@Override
	protected AbstractDominoList<Signup> createCollection() { return new SignupList(); }


	private static final long serialVersionUID = -3231586149562779551L;



	public class SignupList extends AbstractDominoList<Signup> {
		public static final String BY_ID = "SignupID";
		public static final String BY_TOPIC_ID = "TopicID";
		public static final String BY_TOPIC_ID_AND_PLAYER_NAME = "$TopicIDAndUsername";
		public static final String BY_PLAYER_NAME = "Username";
		public static final String BY_CHARACTER_NAME = "$Character";
		public static final String BY_PLAYER_NAME_AND_ATTENDED = "$UsernameAttended";

		@Override
		protected String[] getColumnFields() {
			return new String[] { "id", "topicId", "createdBy", "dateTime", "canceled", "playerName", "", "preferredCharacter",
					"preferredRole", "status", "selected", "assignedCharacter", "assignedRole", "group",
					"attended", "", "signupNotes", "eventLeader", "eventLootTier", "eventDate"
			};
		}
		@Override
		protected String getDefaultSort() { return SignupList.BY_ID; }
		@Override
		protected String getViewName() { return "Signups"; }

		public int getSelectedCount() {
			return this.getSelectedCount(-1);
		}
		public int getSelectedCount(int group) {
			int count = 0;
			for(Signup signup : this) {
				if(signup != null) {
					if(
							(group == -1 ||
									signup.getGroup() == group) &&
									signup.isSelected()) {
						count++;
					}
				}
			}
			return count;
		}
		public int providesBuff(String unid) throws Exception {
			return this.providesBuff(unid, -1);
		}
		public int providesBuff(String unid, int group) throws Exception {
			// 2 = always provided
			// 1 = sometimes provided
			// 0 = not provided

			int result = 0;
			for(Signup signup : this) {
				if((group == -1 || signup.getGroup() == group) && signup.isSelected()) {
					if(signup.getProvidedBuffs().contains(unid)) {
						return 2;
					}
					if(signup.getSometimesProvidedBuffs().contains(unid)) {
						result = 1;
					}
				}
			}
			return result;
		}
		public int providesBuffCount(String unid) throws Exception {
			return this.providesBuffCount(unid, -1);
		}
		public int providesBuffCount(String unid, int group) throws Exception {
			int result = 0;
			for(Signup signup : this) {
				if((group == -1 || signup.getGroup() == group) && signup.isSelected()) {
					if(signup.getProvidedBuffs().contains(unid)) {
						result++;
					}
				}
			}
			return result;
		}
		public int sometimesProvidesBuffCount(String unid) throws Exception {
			return this.sometimesProvidesBuffCount(unid, -1);
		}
		public int sometimesProvidesBuffCount(String unid, int group) throws Exception {
			int result = 0;
			for(Signup signup : this) {
				if((group == -1 || signup.getGroup() == group) && signup.isSelected()) {
					if(signup.getSometimesProvidedBuffs().contains(unid)) {
						result++;
					}
				}
			}
			return result;
		}
		public List<Signup> findByRole(String role, boolean onlySelected) {
			return this.findByRole(role, onlySelected, -1);
		}
		public List<Signup> findByRole(String role, boolean onlySelected, int group) {
			List<Signup> result = new Vector<Signup>();

			for(Signup signup : this) {
				if((group == -1 || signup.getGroup() == group) && (signup.isSelected() || !onlySelected)) {
					if(signup.getAssignedRole().equals(role)) {
						result.add(signup);
					}
				}
			}

			return result;
		}

		public List<Signup> findByGroup(int group, boolean onlySelected) {
			List<Signup> result = new Vector<Signup>();

			for(Signup signup : this) {
				if(signup.getGroup() == group && (signup.isSelected() || !onlySelected)) {
					result.add(signup);
				}
			}

			return result;
		}
		public List<Signup> findByGroup(int group) {
			return this.findByGroup(group, false);
		}

		public List<Signup> findBySelected(boolean selected) {
			List<Signup> result = new Vector<Signup>();

			for(Signup signup : this) {
				if(signup.isSelected() == selected) {
					result.add(signup);
				}
			}

			return result;
		}


		private static final long serialVersionUID = -1270597992321530799L;
	}
}