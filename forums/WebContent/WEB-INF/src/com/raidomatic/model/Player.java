package com.raidomatic.model;

import java.util.*;

import com.raidomatic.JSFUtil;
import com.raidomatic.forum.model.*;
//import com.raidomatic.ModelCache;
import lombok.*;

import lotus.domino.*;

public class Player extends AbstractModel {
	private @Getter @Setter String id;
	private @Getter @Setter String shortName;
	private @Getter @Setter List<String> alternateNames;
	private @Getter @Setter String dispName;
	private @Getter @Setter String signature;
	private @Getter @Setter String dispTitle;
	private @Getter @Setter String mailServer;
	private @Getter @Setter String mailFile;
	private @Getter @Setter String mailDomain;
	private @Getter @Setter String mailSystem;
	private @Getter @Setter String contactEmailAddress;

	public Player() { }
	public Player(String id) { this.setId(id); }

	public String getName() { return this.shortName; }

	public List<Post> getPosts() throws Exception {
		Posts posts = new Posts();
		return posts.getPostsForUser(this.getShortName());
	}

	public List<Character> getCharacters() throws Exception {
		Characters characters = new Characters();
		return characters.getCharactersForPlayer(this.getShortName());
	}
	public List<String> getCharacterNames() throws Exception {
		List<String> result = new Vector<String>();
		for(Character character : this.getCharacters()) {
			result.add(character.getName());
		}
		return result;
	}
	public List<Signup> getSignups() {
		Signups signups = new Signups();
		return signups.getSignupsForPlayerName(this.getShortName());
	}
	public int getSignupCount() throws Exception {
		return this.getSignups().size();
	}
	public int getSignupCountForLootTier(LootTier tier) throws Exception {
		Signups signups = new Signups();
		return signups.getSignupsForPlayerNameAndLootTierId(this.getShortName(), tier.getId()).size();
	}
	public int getAttendanceCount() throws Exception {
		// Add two to account for initial "1 need, 2 attendances" PLS rule
		Signups signups = new Signups();
		return signups.getAttendancesForPlayerName(this.getShortName()).size() + 2;
	}
	public int getAttendanceCountForLootTier(LootTier tier) throws Exception {
		// Add two to account for initial "1 need, 2 attendances" PLS rule
		Signups signups = new Signups();
		return signups.getAttendencesForPlayerNameAndLootTierId(this.getShortName(), tier.getId()).size() + 2;
	}
	public List<Loot> getLoots() throws Exception {
		Loots loots = new Loots();
		return loots.getLootsForPlayerName(this.getShortName());
	}
	public List<Loot> getLootsForTier(LootTier tier) throws Exception {
		List<Loot> result = new Vector<Loot>();
		for(Loot loot : this.getLoots()) {
			if(loot.getLootTierId().equals(tier.getId()));
		}
		return result;
	}
	public int getNeedCount() throws Exception {
		int result = 0;
		Loots loots = new Loots();
		result += loots.getLootsForPlayerNameAndResult(this.getShortName(), "Need").size();
		result += loots.getLootsForPlayerNameAndResult(this.getShortName(), "Banked Need").size();
		// Add one to account for initial "1 need, 2 attendances" PLS rule
		return result + 1;
	}
	public int getNeedCountForLootTier(LootTier tier) throws Exception {
		Loots loots = new Loots();
		return loots.getLootsForPlayerNameResultAndLootTierId(this.getShortName(), "Need", tier.getId()).size() +
		loots.getLootsForPlayerNameResultAndLootTierId(this.getShortName(), "Banked Need", tier.getId()).size() +
		1;
	}

	public LootRatioTier getLootRatioTierForTier(LootTier lootTier) throws Exception {
		int attendanceCount = lootTier == null ? this.getAttendanceCount() : this.getAttendanceCountForLootTier(lootTier);
		if(attendanceCount == 0) { return null; }
		int needCount = lootTier == null ? this.getNeedCount() : this.getNeedCountForLootTier(lootTier);

		LootRatioTiers tiers = new LootRatioTiers();
		Double percent = ((needCount * 1.0) / attendanceCount) * 100;
		return tiers.getLootRatioTierForPercent(percent.intValue());
	}

	public boolean isSignedUpForLootTier(LootTier lootTier) throws Exception {
		int count = lootTier == null ? this.getSignupCount() : this.getSignupCountForLootTier(lootTier);
		return count > 0;
	}
	public boolean isAttendedForLootTier(LootTier lootTier) throws Exception {
		int count = lootTier == null ? this.getAttendanceCount()-2 : this.getAttendanceCountForLootTier(lootTier)-2;
		return count > 0;
	}

	public boolean save(boolean asSigner) {
		try {
			Document playerDoc = asSigner ? this.getDocumentAsSigner() : this.getDocument();
			playerDoc.replaceItemValue("Signature", this.getSignature().length() > 30000 ? this.getSignature().substring(0, 30000) : this.getSignature());

			playerDoc.replaceItemValue("ShortName", this.getId());
			Vector<Object> newFullName = new Vector<Object>();
			newFullName.add(this.getId());
			for(String name : this.getAlternateNames()) {
				if(!newFullName.contains(name)) { newFullName.add(name); }
			}
			playerDoc.replaceItemValue("FullName", newFullName);
			playerDoc.replaceItemValue("DispTitle", this.getDispTitle());
			playerDoc.replaceItemValue("DispName", this.getDispName());

			playerDoc.computeWithForm(false, false);

			playerDoc.save();
			this.clearCache();

			//			try {
			//				playerDoc.recycle();
			//			} catch(NotesException ne) { }

			return true;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	@Override
	public boolean save() { return this.save(false); }
	@Override
	public Database getDatabase() throws NotesException {
		Database database = JSFUtil.getSessionAsSigner().getDatabase("", "wownames.nsf");
		JSFUtil.registerProductObject(database);
		return database;
	}

	@Override
	public void clearCache() throws Exception {
		//		ModelCache cache = JSFUtil.getModelCache();
		//		cache.clearMatches(".*-PlayerList-.*");
		//		cache.clearMatches(".*-CharacterList-.*");
		//		
		JSFUtil.getModelIDCache().clear();
	}

	private static final long serialVersionUID = -4911084743066752005L;

	public static class SignupsComparator implements Comparator<Player> {
		LootTier lootTier = null;

		public SignupsComparator() { }
		public SignupsComparator(LootTier lootTier) { this.lootTier = lootTier; }

		@Override @SneakyThrows
		public int compare(Player o1, Player o2) {
			int a = this.lootTier == null ? o1.getSignupCount() : o1.getSignupCountForLootTier(lootTier);
			int b = this.lootTier == null ? o2.getSignupCount() : o2.getSignupCountForLootTier(lootTier);
			return a - b;
		}
	}
	public static class AttendancesComparator implements Comparator<Player> {
		LootTier lootTier = null;

		public AttendancesComparator() { }
		public AttendancesComparator(LootTier lootTier) { this.lootTier = lootTier; }

		@Override @SneakyThrows
		public int compare(Player o1, Player o2) {
			int a = this.lootTier == null ? o1.getAttendanceCount() : o1.getAttendanceCountForLootTier(lootTier);
			int b = this.lootTier == null ? o2.getAttendanceCount() : o2.getAttendanceCountForLootTier(lootTier);
			return a - b;
		}
	}
	public static class NeedsComparator implements Comparator<Player> {
		LootTier lootTier = null;

		public NeedsComparator() { }
		public NeedsComparator(LootTier lootTier) { this.lootTier = lootTier; }

		@Override @SneakyThrows
		public int compare(Player o1, Player o2) {
			int a = this.lootTier == null ? o1.getNeedCount() : o1.getAttendanceCountForLootTier(lootTier);
			int b = this.lootTier == null ? o2.getNeedCount() : o2.getAttendanceCountForLootTier(lootTier);
			return a - b;
		}
	}
	public static class RatioComparator implements Comparator<Player> {
		LootTier lootTier = null;

		public RatioComparator() { }
		public RatioComparator(LootTier lootTier) { this.lootTier = lootTier; }

		@Override @SneakyThrows
		public int compare(Player o1, Player o2) {
			double aAttendance = this.lootTier == null ? o1.getNeedCount() : o1.getAttendanceCountForLootTier(lootTier);
			double bAttendance = this.lootTier == null ? o2.getNeedCount() : o2.getAttendanceCountForLootTier(lootTier);

			if(aAttendance == 0) { return -1; }
			if(bAttendance == 0) { return 1; }

			double aNeed = this.lootTier == null ? o1.getNeedCount() : o1.getAttendanceCountForLootTier(lootTier);
			double bNeed = this.lootTier == null ? o2.getNeedCount() : o2.getAttendanceCountForLootTier(lootTier);

			double aRatio = aNeed / aAttendance;
			double bRatio = bNeed / bAttendance;

			return aRatio < bRatio ? -1 : aRatio == bRatio ? 0 : 1;
		}
	}
}