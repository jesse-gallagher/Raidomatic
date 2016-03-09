package controller;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.raidomatic.JSFUtil;
import java.util.*;
import frostillicus.controller.BasicXPageController;

public class NewTopic extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	@Override
	public void beforePageLoad() throws Exception {
		Map<String, String> param = JSFUtil.getParam();
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();

		com.raidomatic.forum.model.Post post = new com.raidomatic.forum.model.Post();
		viewScope.put("post", post);
		post.setForumId(param.get("forumId"));

		// Find the forum
		com.raidomatic.forum.model.Forums forums = (com.raidomatic.forum.model.Forums)JSFUtil.getVariableValue("Forums");
		com.raidomatic.forum.model.Forum forum = forums.getById(param.get("forumId"));
		viewScope.put("forum", forum);

		// Set the category
		viewScope.put("category", forum.getCategory());

		// Set up some page details
		viewScope.put("topicData", new HashMap<Object, Object>());
		if("raid".equals(param.get("type")) || "event".equals(param.get("type"))) {
			viewScope.put("attributes", "Event");
			viewScope.put("topicEventType", "raid".equals(param.get("type")) ? "Raid" : "Event");
			viewScope.put("topicEventLeader", new ArrayList<String>());
		}

	}

	public com.raidomatic.forum.model.Post getPost() {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		return (com.raidomatic.forum.model.Post)viewScope.get("post");
	}
	public com.raidomatic.forum.model.Forum getForum() {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		return (com.raidomatic.forum.model.Forum)viewScope.get("forum");
	}
	public com.raidomatic.forum.model.Category getCategory() {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		return (com.raidomatic.forum.model.Category)viewScope.get("category");
	}

	@SuppressWarnings("unchecked")
	public void saveTopic() throws Exception {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		Collection<String> attributes = (Collection<String>)viewScope.get("attributes");

		com.raidomatic.forum.model.Topic topic = new com.raidomatic.forum.model.Topic();
		topic.setForumId(getForum().getId());
		topic.setSticky(attributes.contains("Sticky"));
		topic.setTitle(getPost().getTitle());
		topic.setFirstPostAt(new Date());
		if(topic.isApplication()) {
			topic.setFirstPostBy("System");
		} else {
			topic.setFirstPostBy(JSFUtil.getUserName());
		}

		if(attributes.contains("Event")) {
			topic.setEventDate((Date)viewScope.get("topicEventDate"));
			topic.setEventType((String)viewScope.get("topicEventType"));

			Object topicEventLeader = viewScope.get("topicEventLeader");
			if(topicEventLeader instanceof List) {
				topic.setEventLeader((List<String>)topicEventLeader);
			} else {
				topic.setEventLeader((String)topicEventLeader);
			}

			topic.setEventLootTier((String)viewScope.get("topicEventLootTier"));
			topic.setEventGroupsText((String)viewScope.get("topicEventGroupsText"));
		} else {
			topic.setEventDate(null);
			topic.setEventType("");
		}
		topic.save();

		getPost().setTopicId(topic.getId());
		getPost().save();

		JSFUtil.appRedirect("/Topics/" + topic.getId());
	}
}
