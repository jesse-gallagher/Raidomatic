package controller;

import frostillicus.controller.BasicXPageController;
import java.util.*;
import javax.faces.component.UIInput;
import lotus.domino.NotesException;
import com.raidomatic.Configuration;
import com.raidomatic.JSFUtil;
import com.raidomatic.forum.model.Topics;
import com.raidomatic.forum.model.Topic;
import com.raidomatic.model.Players;
import com.raidomatic.model.Signup;
import com.ibm.xsp.extlib.util.ExtLibUtil;

public class EventCalendar extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	@Override
	public void beforePageLoad() {
		ExtLibUtil.getViewScope().put("selectedEvents", new java.util.HashMap<String, String>());
	}

	public String getCellStyleClass() throws NotesException {
		if(this.getCurrentDayTopics().size() > 0) {
			return "raids";
		}
		return "";
	}
	public List<Topic> getCurrentDayTopics() throws NotesException {
		Topics topics = (Topics)JSFUtil.getVariableValue("Topics");
		Date calendarDay = (Date)JSFUtil.getVariableValue("calendarDay");
		return topics.getTopicsForEventDay(calendarDay);
	}
	public String getCurrentRaidStyleClass() throws NotesException {
		Topic topic = (Topic)JSFUtil.getVariableValue("topic");
		return "raid" + (topic.getIsSignupSelected() ? " selected" : topic.getIsSignedUp() ? " signed-up" : "");
	}
	public String getRaidsForumId() throws Exception {
		Configuration config = (Configuration)JSFUtil.getVariableValue("Configuration");
		return (String)config.getDocumentItemValue("Forum Options", "ForumRaidsTargetForum").get(0);
	}
	public String getEventsForumId() throws Exception {
		Configuration config = (Configuration)JSFUtil.getVariableValue("Configuration");
		return (String)config.getDocumentItemValue("Forum Options", "ForumEventsTargetForum").get(0);
	}

	public List<String> getAvailableCharacters() throws Exception {
		List<String> result = new ArrayList<String>();
		Players players = (Players)JSFUtil.getVariableValue("Players");
		com.raidomatic.model.Player player = players.getCurrent();
		if(player == null) { return result; }
		for(com.raidomatic.model.Character character : player.getCharacters()) {
			result.add(character.getName() + "|" + character.getId());
		}
		return result;
	}
	@SuppressWarnings("unchecked")
	public void signMeUp() throws Exception {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		Map<String, String> selectedEvents = (Map<String, String>)viewScope.get("selectedEvents");
		Topics topics = (Topics)JSFUtil.getVariableValue("Topics");
		for(Map.Entry<String, String> node : selectedEvents.entrySet()) {
			if("true".equals(node.getValue())) {
				if(!topics.getById(node.getKey()).getIsPlayerSignedUp(JSFUtil.getUserName())) {
					Signup signup = new Signup();
					signup.setTopicId(node.getKey());
					signup.setPlayerName(JSFUtil.getUserName());

					UIInput signupPreferredRole = (UIInput)ExtLibUtil.getComponentFor(JSFUtil.getViewRoot(), "signupPreferredRole");
					UIInput signupPreferredCharacter = (UIInput)ExtLibUtil.getComponentFor(JSFUtil.getViewRoot(), "signupPreferredCharacter");
					UIInput signupStatus = (UIInput)ExtLibUtil.getComponentFor(JSFUtil.getViewRoot(), "signupStatus");

					signup.setPreferredRole((String)signupPreferredRole.getValue());
					signup.setPreferredCharacter((String)signupPreferredCharacter.getValue());
					signup.setStatus((String)signupStatus.getValue());

					signup.save();
				}
			}
		}

		selectedEvents.clear();
		JSFUtil.getModelCache().clear();
		JSFUtil.getModelIDCache().clear();
	}
}
