package controller;

import frostillicus.controller.BasicXPageController;
import java.util.*;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.raidomatic.JSFUtil;
import com.raidomatic.model.*;
import com.raidomatic.model.Player;
import lombok.*;

public class Players extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	@Override
	public void beforePageLoad() throws Exception {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		viewScope.put("$$originalPlayerList", new ArrayList<Player>(getPlayers().getPlayers()));
		updatePlayerList();
	}

	@SuppressWarnings("unchecked")
	public List<String> getLootTierChoices() {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		if(!viewScope.containsKey("$$lootTierChoices")) {
			List<String> lootTierChoices = new ArrayList<String>();

			LootTiers lootTiers = (LootTiers)JSFUtil.getVariableValue("LootTiers");
			List<LootTier> tiers = lootTiers.getLootTiersByName();
			for(LootTier tier : tiers) {
				lootTierChoices.add(tier.getName() + "|" + tier.getId());
			}
			viewScope.put("$$lootTierChoices", lootTierChoices);
		}
		return (List<String>)viewScope.get("$$lootTierChoices");
	}

	private com.raidomatic.model.Players getPlayers() {
		return (com.raidomatic.model.Players)JSFUtil.getVariableValue("Players");
	}

	@SuppressWarnings("unchecked")
	public List<Player> getCurrentPlayerList() {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		return (List<Player>)viewScope.get("$$currentPlayerList");
	}

	@SuppressWarnings("unchecked")
	public void updatePlayerList() throws Exception {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();

		String sortBy = (String)viewScope.get("sortBy");
		LootTier lootTier = getCurrentLootTier();
		String onlyAttended = (String)viewScope.get("onlyAttended");

		List<Player> originalPlayerList = (List<Player>)viewScope.get("$$originalPlayerList");
		List<Player> currentPlayerList = new ArrayList<Player>(originalPlayerList);
		if("Signups".equals(sortBy)) {
			Collections.sort(currentPlayerList, new PlayerSignupComparator(lootTier));
		} else if("Attendances".equals(sortBy)) {
			Collections.sort(currentPlayerList, new PlayerAttendanceComparator(lootTier));
		} else if("Needs".equals(sortBy)) {
			Collections.sort(currentPlayerList, new PlayerNeedsComparator(lootTier));
		} else if("Ratio".equals(sortBy)) {
			Collections.sort(currentPlayerList, new PlayerRatioComparator(lootTier));
		}

		if("Yes".equals(onlyAttended)) {
			List<Player> result = new ArrayList<Player>();
			for(Player player : currentPlayerList) {
				if(player.isAttendedForLootTier(lootTier)) { result.add(player); }
			}
			currentPlayerList = result;
		} else if("Signed Up".equals(onlyAttended)) {
			List<Player> result = new ArrayList<Player>();
			for(Player player : currentPlayerList) {
				if(player.isSignedUpForLootTier(lootTier)) { result.add(player); }
			}
			currentPlayerList = result;
		}
		viewScope.put("$$currentPlayerList", currentPlayerList);
	}

	public String getCurrentLootRatioTier() throws Exception {
		LootTier lootTier = getCurrentLootTier();
		Player player = (Player)JSFUtil.getVariableValue("player");
		LootRatioTier tier = player.getLootRatioTierForTier(lootTier);
		return tier == null ? "-" : tier.getMin() + (tier.getMax() != null ? ("-" + tier.getMax()) : "+");
	}
	public String getCurrentLootRatioTierName() throws Exception {
		LootTier lootTier = getCurrentLootTier();
		Player player = (Player)JSFUtil.getVariableValue("player");
		LootRatioTier tier = player.getLootRatioTierForTier(lootTier);
		return tier == null ? "" : tier.getName();
	}

	public LootTier getCurrentLootTier() throws Exception {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		LootTiers lootTiers = (LootTiers)JSFUtil.getVariableValue("LootTiers");
		String lootTierId = (String)viewScope.get("lootTierId");
		return StringUtil.isEmpty(lootTierId) ? null : lootTiers.getById(lootTierId);
	}

	private class PlayerSignupComparator implements Comparator<Player> {
		private LootTier lootTier;
		public PlayerSignupComparator(LootTier lootTier) { this.lootTier = lootTier; }
		@Override @SneakyThrows
		public int compare(Player a, Player b) {
			int aSignup = lootTier == null ? a.getSignupCount() : a.getSignupCountForLootTier(lootTier);
			int bSignup = lootTier == null ? b.getSignupCount() : b.getSignupCountForLootTier(lootTier);
			return aSignup < bSignup ? -1 : aSignup == bSignup ? 0 : 1;
		}
	}
	private class PlayerAttendanceComparator implements Comparator<Player> {
		private LootTier lootTier;
		public PlayerAttendanceComparator(LootTier lootTier) { this.lootTier = lootTier; }
		@Override @SneakyThrows
		public int compare(Player a, Player b) {
			int aAttendance = lootTier == null ? a.getAttendanceCount() - 2 : a.getAttendanceCountForLootTier(lootTier) - 2;
			int bAttendance = lootTier == null ? b.getAttendanceCount() - 2 : b.getAttendanceCountForLootTier(lootTier) - 2;
			return aAttendance < bAttendance ? -1 : aAttendance == bAttendance ? 0 : 1;
		}
	}
	private class PlayerNeedsComparator implements Comparator<Player> {
		private LootTier lootTier;
		public PlayerNeedsComparator(LootTier lootTier) { this.lootTier = lootTier; }
		@Override @SneakyThrows
		public int compare(Player a, Player b) {
			int aNeeds = lootTier == null ? a.getNeedCount() - 1: a.getNeedCountForLootTier(lootTier) - 1;
			int bNeeds = lootTier == null ? b.getNeedCount() - 1: b.getNeedCountForLootTier(lootTier) - 1;
			return aNeeds < bNeeds ? -1 : aNeeds == bNeeds ? 0 : 1;
		}
	}
	private class PlayerRatioComparator implements Comparator<Player> {
		private LootTier lootTier;
		public PlayerRatioComparator(LootTier lootTier) { this.lootTier = lootTier; }
		@Override @SneakyThrows
		public int compare(Player a, Player b) {
			int aAttendance = lootTier == null ? a.getAttendanceCount() - 2 : a.getAttendanceCountForLootTier(lootTier) - 2;
			int bAttendance = lootTier == null ? b.getAttendanceCount() - 2 : b.getAttendanceCountForLootTier(lootTier) - 2;
			if(aAttendance == 0) { return -1; }
			if(bAttendance == 0) { return 1; }

			int aNeeds = lootTier == null ? a.getNeedCount() - 1 : a.getNeedCountForLootTier(lootTier) - 1;
			int bNeeds = lootTier == null ? b.getNeedCount() - 1 : b.getNeedCountForLootTier(lootTier) - 1;

			double aRatio = (aNeeds * 1.0) / aAttendance;
			double bRatio = (bNeeds * 1.0) / bAttendance;
			return aRatio < bRatio ? -1 : aRatio == bRatio ? 0 : 1;
		}
	}
}
