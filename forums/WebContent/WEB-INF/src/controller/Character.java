package controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.component.UIViewRootEx2;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.raidomatic.JSFUtil;
import frostillicus.controller.BasicModelPageController;
import lotus.domino.NotesException;
import java.util.*;
import com.raidomatic.model.Players;

import static com.ibm.commons.util.StringUtil.trim;
import static com.ibm.commons.util.StringUtil.isEmpty;

public class Character extends BasicModelPageController {
	private static final long serialVersionUID = 1L;

	@Override
	public void beforePageLoad() throws Exception {

		// Set the context character or make a new one
		com.raidomatic.model.Characters characters = new com.raidomatic.model.Characters();
		HttpServletRequest request = JSFUtil.getRequest();
		Map<String, String> param = JSFUtil.getParam();
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		com.raidomatic.model.Character character = null;
		if(!StringUtil.isEmpty(request.getPathInfo())) {
			String[] args = request.getPathInfo().split("/");
			character = characters.getByName(args[1]);
		} else if("true".equals(param.get("newChar"))) {
			com.raidomatic.model.Character newChar = new com.raidomatic.model.Character();
			newChar.setPlayerName(param.get("player"));
			viewScope.put("editCharacter", true);
			viewScope.put("characterPlayer", param.get("player"));
			character = newChar;
		}

		// Set the page title
		UIViewRootEx2 view = JSFUtil.getViewRoot();
		if(character != null) {
			if("true".equals(param.get("newChar"))) {
				view.setPageTitle("New Character");
			} else {
				view.setPageTitle(character.getName());
			}
			viewScope.put("character", character);
		} else {
			view.setPageTitle("Error");
			viewScope.put("fatalErrors", new String[] { "The requested character does not exist or you are not permitted to access it." });
		}
	}

	public com.raidomatic.model.Character getCharacter() {
		return (com.raidomatic.model.Character)ExtLibUtil.getViewScope().get("character");
	}

	public void editCharacter() {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		viewScope.put("editCharacter", true);
		viewScope.put("characterName", getCharacter().getName());
		viewScope.put("characterPlayer", getCharacter().getPlayer().getName());
		viewScope.put("characterPrimaryGroup", getCharacter().getPrimaryGroup());
		viewScope.put("characterSecondaryGroup", getCharacter().getSecondaryGroup());
	}
	@SuppressWarnings("unchecked")
	public List<String> getPlayerNames() throws NotesException {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		if(!viewScope.containsKey("$$playerNames")) {
			Players players = new Players();
			viewScope.put("$$playerNames", players.getNames());
		}
		return (List<String>)viewScope.get("$$playerNames");
	}
	public void lookUpCharacter() throws Exception {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		String characterName = (String)viewScope.get("characterName");
		if(!isEmpty(trim(characterName))) {
			getCharacter().setName(characterName);
			getCharacter().updateFromArmory();
		}
	}
	public boolean isSaveable() {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		Boolean editMode = (Boolean)viewScope.get("editCharacter");

		return (editMode != null && editMode) && (!isEmpty(getCharacter().getPrimarySpec()) || !isEmpty(getCharacter().getSecondarySpec()));
	}
	public void saveCharacter() throws Exception {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		String characterName = (String)viewScope.get("characterName");
		if(!isEmpty(trim(characterName))) {
			getCharacter().setName((String)viewScope.get("characterName"));
			getCharacter().setPlayerName((String)viewScope.get("characterPlayer"));
			if(!isEmpty(getCharacter().getPrimarySpec())) {
				getCharacter().setPrimaryGroup((String)viewScope.get("characterPrimaryGroup"));
			}
			if(!isEmpty(getCharacter().getSecondarySpec())) {
				getCharacter().setSecondarySpec((String)viewScope.get("characterSecondarySpec"));
			}
			getCharacter().save();
		}
		JSFUtil.appRedirect("/Characters/" + java.net.URLEncoder.encode(getCharacter().getName(), "UTF-8"));
	}
}
