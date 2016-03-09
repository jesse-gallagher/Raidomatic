package controller;

import javax.servlet.http.HttpServletRequest;
import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.component.UIViewRootEx2;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.raidomatic.JSFUtil;
import com.raidomatic.model.*;
import java.util.*;
import frostillicus.controller.BasicModelPageController;
import lotus.domino.*;
import com.raidomatic.model.Players;

public class Player extends BasicModelPageController {
	private static final long serialVersionUID = 1L;

	@Override
	public void beforePageLoad() {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();

		// Set the player object
		Players players = new Players();
		com.raidomatic.model.Player player = null;
		try {
			HttpServletRequest req = JSFUtil.getRequest();
			if(!StringUtil.isEmpty(req.getPathInfo())) {
				String[] args = req.getPathInfo().split("\\/");
				player = players.getByName(args[1]);
			}
		} catch(Exception e) { }
		viewScope.put("player", player);

		// Set the page title
		UIViewRootEx2 view = JSFUtil.getViewRoot();

		if(player != null) {
			view.setPageTitle(player.getName());
		} else {
			view.setPageTitle("Error");
			viewScope.put("fatalErrors", new String[] { "The requested player does not exist or you are not permitted to access it." });
			return;
		}
	}

	public com.raidomatic.model.Player getPlayer() {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		return (com.raidomatic.model.Player)viewScope.get("player");
	}

	public boolean isCanCreateNewCharacters() {
		try {
			Database common = ExtLibUtil.getCurrentSession().getDatabase("", "wow/common.nsf");
			int level = common.getCurrentAccessLevel();
			common.recycle();
			return level >= 3;
		} catch(Exception e) { }
		return false;

	}

	public int getCurrentAttendances() throws Exception {
		LootTier lootTier = (LootTier)JSFUtil.getVariableValue("lootTier");
		return this.getPlayer().getAttendanceCountForLootTier(lootTier) - 2;
	}
	public int getCurrentNeedCount() throws Exception {
		LootTier lootTier = (LootTier)JSFUtil.getVariableValue("lootTier");
		return this.getPlayer().getNeedCountForLootTier(lootTier);
	}
	public String getCurrentLootRatioTier() throws Exception {
		LootTier lootTier = (LootTier)JSFUtil.getVariableValue("lootTier");
		LootRatioTier tier = this.getPlayer().getLootRatioTierForTier(lootTier);
		return tier == null ? "-" : tier.getMin() + (tier.getMax() != null ? ("-" + tier.getMax()) : "+");
	}
}
