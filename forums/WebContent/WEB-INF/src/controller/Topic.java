package controller;

import frostillicus.controller.BasicModelPageController;
import java.io.IOException;
import java.util.*;

import javax.faces.component.UIInput;

import lotus.domino.NotesException;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.raidomatic.forum.model.Forums;
import com.raidomatic.forum.model.Forum;
import com.raidomatic.forum.model.Topics;
import com.raidomatic.forum.model.Post;
import com.raidomatic.forum.model.Posts;
import com.raidomatic.model.Players;
import com.raidomatic.model.Player;
import com.raidomatic.model.Characters;
import com.raidomatic.model.Signup;
import com.raidomatic.model.Signups;
import com.raidomatic.JSFUtil;
import com.raidomatic.Configuration;

public class Topic extends BasicModelPageController {
	private static final long serialVersionUID = 1L;

	@Override
	public void beforePageLoad() throws Exception {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		Topics topics = (Topics)JSFUtil.getVariableValue("Topics");
		String[] args = JSFUtil.getPathInfoArgs();
		com.raidomatic.forum.model.Topic topic = null;
		if(args.length > 1) {
			topic = topics.getById(args[1]);
		}

		if(topic != null) {
			JSFUtil.getViewRoot().setPageTitle(topic.getTitle());
			topic.markRead();
			viewScope.put("topic", topic);
		} else {
			JSFUtil.getViewRoot().setPageTitle("Error");
			ExtLibUtil.getViewScope().put("fatalErrors", new String[] { "The requested topic does not exist or you are not permitted to access it." });
		}
	}

	public void editTopic() {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		com.raidomatic.forum.model.Topic topic = (com.raidomatic.forum.model.Topic)viewScope.get("topic");

		viewScope.put("editTopic", true);
		viewScope.put("topicTitle", topic.getTitle());
		viewScope.put("topicApplicationStatus", topic.getApplicationStatus());
		viewScope.put("topicEventDate", topic.getEventDate());
		viewScope.put("topicEventLeader", topic.getEventLeader() == null ? new ArrayList<String>() : topic.getEventLeader());
		viewScope.put("topicEventType", topic.getEventType());
		viewScope.put("topicEventLootTier", topic.getEventLootTier());
		viewScope.put("topicEventGroupsText", topic.getEventGroupsText());

		List<String> attributes = new ArrayList<String>();
		if(topic.isSticky()) { attributes.add("Sticky"); }
		if(topic.getEventDate() != null) { attributes.add("Event"); }
		viewScope.put("topicAttributes", attributes);
	}
	public void deleteTopic() throws IOException {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		com.raidomatic.forum.model.Topic topic = (com.raidomatic.forum.model.Topic)viewScope.get("topic");
		topic.remove();
		JSFUtil.appRedirect("/");
	}

	public List<String> getMoveTopicChoices() throws Exception {
		List<String> result = new ArrayList<String>();
		Forums forums = (com.raidomatic.forum.model.Forums)JSFUtil.getVariableValue("Forums");
		for(Forum forum : forums.getAll()) {
			result.add(forum.getCategory().getTitle() + ": " + forum.getTitle() + "|" + forum.getId());
		}
		Collections.sort(result, String.CASE_INSENSITIVE_ORDER);

		return result;
	}
	public void moveTopic() throws Exception {
		UIInput newForumBox = (UIInput)ExtLibUtil.getComponentFor(JSFUtil.getViewRoot(), "newForumBox");
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		com.raidomatic.forum.model.Topic topic = (com.raidomatic.forum.model.Topic)viewScope.get("topic");
		Forums forums = (com.raidomatic.forum.model.Forums)JSFUtil.getVariableValue("Forums");
		topic.moveToForum(forums.getById((String)newForumBox.getValue()));
		JSFUtil.appRedirect("/Topics/" + topic.getId());
	}

	@SuppressWarnings("unchecked")
	public void saveTopic() throws IOException {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		com.raidomatic.forum.model.Topic topic = (com.raidomatic.forum.model.Topic)viewScope.get("topic");

		List<String> attributes = (List<String>)viewScope.get("topicAttributes");

		topic.setSticky(attributes.contains("Sticky"));
		topic.setTitle((String)viewScope.get("topicTitle"));
		if(topic.isApplication()) { topic.setApplicationStatus((String)viewScope.get("topicApplicationStatus")); }
		if(attributes.contains("Event")) {
			topic.setEventDate((Date)viewScope.get("topicEventDate"));
			topic.setEventType((String)viewScope.get("topicEventType"));
			topic.setEventLootTier((String)viewScope.get("topicEventLootTier"));
			topic.setEventGroupsText((String)viewScope.get("topicEventGroupsText"));

			Object topicEventLeader = viewScope.get("topicEventLeader");
			if(topicEventLeader instanceof List) {
				topic.setEventLeader((List<String>)topicEventLeader);
			} else {
				topic.setEventLeader((String)topicEventLeader);
			}
		} else {
			topic.setEventDate(null);
			topic.setEventType("");
			topic.setEventLeader(new ArrayList<String>());
			topic.setEventLootTier("");
		}

		topic.save();

		JSFUtil.appRedirect("/Topics/" + topic.getId());
	}

	public int getFirst() throws Exception {
		Configuration config = JSFUtil.getConfiguration();
		int perPage = ((Double)config.getDocumentItemValue("Forum Options", "ForumPostsPerTopicPage").get(0)).intValue();
		com.raidomatic.forum.model.Topic topic = this.getTopic();

		Map<String, String> param = JSFUtil.getParam();
		if(param.containsKey("showPost")) {
			try {
				Posts posts = (Posts)JSFUtil.getVariableValue("Posts");
				Post queriedPost = posts.getById(param.get("showPost"));
				int postIndex = topic.getPosts().indexOf(queriedPost);
				return Double.valueOf((postIndex * 1.0) / perPage).intValue() * perPage;
			} catch(Exception e) {
				return 0;
			}
		}

		if(!param.containsKey("page")) { return 0; }
		try {
			if("last".equals(param.get("page"))) {
				int count = topic.getPostCount();
				if(count - perPage <= 0) { return 0; }
				int page = Double.valueOf((count * 1.0) / perPage).intValue();
				if(count % perPage == 0) { page = page - 1; }
				return page * perPage;
			} else {
				int determinedFirst = (Integer.valueOf(param.get("page"))-1) * perPage;
				return determinedFirst < 0 ? 0 : determinedFirst;
			}
		} catch(Exception e) { return 0; }
	}

	/*********************************************************************************
	 * Event/Signup Stuff
	 *********************************************************************************/

	public List<String> getSignupCharacterChoices() throws Exception {
		List<String> result = new ArrayList<String>();

		Players players = (Players)JSFUtil.getVariableValue("Players");
		Player current = players.getCurrent();
		if(current != null) {
			for(com.raidomatic.model.Character character : current.getCharacters()) {
				result.add(character.getName() + "|" + character.getId());
			}
		}

		return result;
	}

	public List<String> getSignupRoleChoices() throws Exception {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		Players players = (Players)JSFUtil.getVariableValue("Players");
		Player current = players.getCurrent();

		List<String> result = new ArrayList<String>();

		if(current != null) {
			String signupPreferredCharacter = (String)viewScope.get("signupPreferredCharacter");
			if(StringUtil.isEmpty(signupPreferredCharacter)) {
				if(current.getCharacters().size() > 0) {
					result.addAll(current.getCharacters().get(0).getAvailableRoles());
				}
			} else {
				Characters characters = (Characters)JSFUtil.getVariableValue("Characters");
				result.addAll(characters.getByName(signupPreferredCharacter).getAvailableRoles());
			}
		}

		return result;
	}

	public void signMeUp() throws NotesException {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		com.raidomatic.forum.model.Topic topic = (com.raidomatic.forum.model.Topic)viewScope.get("topic");
		if(!topic.getIsSignedUp()) {
			Signup signup = new Signup();
			signup.setTopicId(topic.getId());
			signup.setPlayerName(JSFUtil.getUserName());
			signup.setPreferredRole((String)viewScope.get("signupPreferredRole"));
			signup.setPreferredCharacter((String)viewScope.get("signupPreferredCharacter"));
			signup.setStatus((String)viewScope.get("signupStatus"));
			signup.save();
		}
	}
	public void cancelSignup() {
		com.raidomatic.forum.model.Topic topic = this.getTopic();

		Signups signups = (Signups)JSFUtil.getVariableValue("Signups");
		Signup signup = signups.getSignupsForTopicIdAndPlayerName(topic.getId(), JSFUtil.getUserName()).get(0);
		signup.setCanceled(true);
		signup.save();
	}
	public void unCancelSignup() {
		com.raidomatic.forum.model.Topic topic = this.getTopic();

		Signups signups = (Signups)JSFUtil.getVariableValue("Signups");
		Signup signup = signups.getSignupsForTopicIdAndPlayerName(topic.getId(), JSFUtil.getUserName()).get(0);
		signup.setCanceled(false);
		signup.save();
	}


	/*********************************************************************************
	 * Internal utility methods
	 *********************************************************************************/
	private com.raidomatic.forum.model.Topic getTopic() {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		return (com.raidomatic.forum.model.Topic)viewScope.get("topic");
	}
}
