package controller;

import javax.servlet.http.HttpServletRequest;

import com.ibm.xsp.component.UIViewRootEx2;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.raidomatic.Configuration;
import com.raidomatic.JSFUtil;
import frostillicus.controller.BasicModelPageController;
import com.raidomatic.forum.model.Forums;

import java.io.IOException;
import java.util.*;

import static com.ibm.commons.util.StringUtil.isEmpty;

public class Forum extends BasicModelPageController {
	private static final long serialVersionUID = 1L;

	@Override
	public void beforePageLoad() throws Exception {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();

		com.raidomatic.forum.model.Forum forum = null;
		// Find the forum
		try {
			HttpServletRequest request = JSFUtil.getRequest();
			if(!isEmpty(request.getPathInfo())) {
				String[] args = request.getPathInfo().split("\\/");
				Forums forums = (Forums)JSFUtil.getVariableValue("Forums");
				forum = forums.getById(args[1]);
			}
		} catch(Exception e) { }

		UIViewRootEx2 view = JSFUtil.getViewRoot();
		if(forum != null) {
			view.setPageTitle(forum.getTitle());
			forum.markRead();
		} else {
			view.setPageTitle("Error");
			viewScope.put("fatalErrors", new String[] { "The requested forum does not exist or you are not permitted to access it." });
		}

		// Set the posts-per-page config
		Configuration config = (Configuration)JSFUtil.getVariableValue("Configuration");
		viewScope.put("postsPerPage", ((Double)config.getDocumentItemValue("Forum Options", "ForumPostsPerTopicPage").get(0)).intValue());

		viewScope.put("forum", forum);
	}

	public void newTopic() throws IOException {
		JSFUtil.appRedirect("/NewTopic.xsp?forumId=" + getForum().getId());
	}
	public int getFirst() {
		Map<String, String> param = JSFUtil.getParam();
		if(!param.containsKey("page")) { return 0; }
		try {
			int determinedFirst = (Integer.parseInt(param.get("page"))-1) * 20;
			return determinedFirst < 0 ? 0 : determinedFirst;
		} catch(Exception e) { return 0; }
	}

	// TODO: this isn't actually currently used in Forum.xsp - fix that
	public int getPostsPerPage() {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		int posts = (Integer)viewScope.get("postsPerPage");
		return posts;
	}

	public com.raidomatic.forum.model.Forum getForum() {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		return (com.raidomatic.forum.model.Forum)viewScope.get("forum");
	}
}
