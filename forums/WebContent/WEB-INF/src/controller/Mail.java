package controller;

import java.io.IOException;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.raidomatic.JSFUtil;
import com.raidomatic.model.Players;

import frostillicus.controller.BasicXPageController;

public class Mail extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	@Override
	public void beforePageLoad() {
		Players players = (Players)JSFUtil.getVariableValue("Players");
		if(players.getCurrent() == null || !"1".equals(players.getCurrent().getMailSystem())) {
			ExtLibUtil.getViewScope().put("fatalErrors", new String[] { "User is not set up for mail." });
		}
	}

	public void openMailFile() throws IOException {
		Players players = (Players)JSFUtil.getVariableValue("Players");
		JSFUtil.redirect("/" + players.getCurrent().getMailFile().replace('\\', '/'));
	}
}
