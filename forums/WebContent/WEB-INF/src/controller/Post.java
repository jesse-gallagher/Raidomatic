package controller;

import javax.servlet.http.HttpServletRequest;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.raidomatic.Configuration;
import com.raidomatic.JSFUtil;
import frostillicus.controller.BasicModelPageController;
import com.raidomatic.forum.model.Posts;
import java.util.*;

public class Post extends BasicModelPageController {
	private static final long serialVersionUID = 1L;

	@Override
	public void beforePageLoad() {
		Map<String, String> param = JSFUtil.getParam();
		com.raidomatic.forum.model.Post post = null;
		try {
			HttpServletRequest request = JSFUtil.getRequest();
			String[] args = StringUtil.isEmpty(request.getPathInfo()) ? new String[] { } : request.getPathInfo().split("\\/");
			post = args.length < 2 ? null : ((Posts)JSFUtil.getVariableValue("Posts")).getById(args[1]);
			if(post == null) {
				post = new com.raidomatic.forum.model.Post();
				post.setTopicId(param.get("topicId"));
				post.setForumId(post.getTopic().getForumId());
				post.setTitle("Re: " + post.getTopic().getTitle());
				post.setReplyPostId(param.get("replyPostId"));
			}
		} catch(Exception e) { e.printStackTrace(); }

		if(post != null) {
			JSFUtil.getViewRoot().setPageTitle(post.getTitle());
		} else {
			JSFUtil.getViewRoot().setPageTitle("Error");
			ExtLibUtil.getViewScope().put("fatalErrors", new String[] { "The requested topic does not exist or you are not permitted to access it." });
		}
		ExtLibUtil.getViewScope().put("post", post);
	}

	public com.raidomatic.forum.model.Post getPost() {
		return (com.raidomatic.forum.model.Post)ExtLibUtil.getViewScope().get("post");
	}

	public String getDefaultBody() throws Exception {
		Map<String, String> param = JSFUtil.getParam();
		if("true".equals(param.get("quote")) && param.containsKey("replyPostId")) {
			com.raidomatic.forum.model.Post orig = ((Posts)JSFUtil.getVariableValue("Posts")).getById(param.get("replyPostId"));
			if(orig.getPlayer() != null) {
				return "<p>[QUOTETITLE]" + orig.getPlayer().getDispName() + " said:[/QUOTETITLE][QUOTE]" + orig.getBody() + "[/QUOTE]</p>";
			}
		}
		return "";
	}

	public void save() throws Exception {
		getPost().save();

		Configuration config = (Configuration)JSFUtil.getVariableValue("Configuration");
		int postCount = getPost().getTopic().getPostCount();
		double pageCount = postCount / (Double)config.getItemValue("ForumPostsPerTopicPage").get(0);
		pageCount = Math.ceil(pageCount);
		JSFUtil.appRedirect("/Topics/" + getPost().getTopicId() + "?page=" + pageCount + "#post=" + getPost().getId());
	}
}
