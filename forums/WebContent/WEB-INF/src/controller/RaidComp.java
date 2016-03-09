package controller;

import frostillicus.controller.BasicXPageController;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.component.UIViewRootEx2;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.raidomatic.JSFUtil;
import com.raidomatic.forum.model.Topic;
import com.raidomatic.forum.model.Topics;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

public class RaidComp extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	@Override
	public void beforePageLoad() {
		UIViewRootEx2 view = JSFUtil.getViewRoot();
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		Topics topics = (Topics)JSFUtil.getVariableValue("Topics");
		Topic topic = null;
		try {
			HttpServletRequest request = JSFUtil.getRequest();
			if(!StringUtil.isEmpty(request.getPathInfo())) {
				String[] args = request.getPathInfo().split("\\/");
				topic = topics.getById(args[1]);
			} else {
				topic = topics.getById(JSFUtil.getParam().get("topicId"));
			}
			viewScope.put("topic", topic);
			view.setPageTitle(topic.getTitle());
		} catch(Exception e) {
			view.setPageTitle("Error");
			viewScope.put("fatalErrors", new String[] { "The requested raid does not exist or you are not permitted to access it.", e.toString() });
		}
		viewScope.put("topic", topic);
	}

	public Topic getTopic() {
		return (Topic)ExtLibUtil.getViewScope().get("topic");
	}
}
