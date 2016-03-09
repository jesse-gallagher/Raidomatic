package controller;

import frostillicus.controller.BasicXPageController;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.raidomatic.JSFUtil;
import com.raidomatic.forum.model.Topic;
import com.raidomatic.forum.model.Topics;
import java.util.*;
import com.raidomatic.prefs.UserPrefs;

public class FavoriteTopics extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	@Override
	public void beforePageLoad() {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		Topics topics = (Topics)JSFUtil.getVariableValue("Topics");
		UserPrefs userPrefs = (UserPrefs)JSFUtil.getVariableValue("UserPrefs");
		List<String> favorites = userPrefs.getCurrent().getFavorites();
		List<Topic> favoriteTopics = new ArrayList<Topic>();
		for(String topicId : favorites) {
			try {
				Topic topic = topics.getById(topicId);
				if(topic != null) { favoriteTopics.add(topic); }
			} catch(Exception e) { }
		}
		Collections.sort(favoriteTopics, new TopicLatestPostComparator());
		viewScope.put("$$favoriteTopics", favoriteTopics);
	}
	@SuppressWarnings("unchecked")
	public List<Topic> getFavoriteTopics() {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		return (List<Topic>)viewScope.get("$$favoriteTopics");
	}

	private class TopicLatestPostComparator implements Comparator<Topic> {

		@Override
		public int compare(Topic arg0, Topic arg1) {
			return arg1.getLatestPostAt().compareTo(arg0.getLatestPostAt());
		}

	}
}
