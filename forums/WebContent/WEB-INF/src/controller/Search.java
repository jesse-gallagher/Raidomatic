package controller;

import frostillicus.controller.BasicXPageController;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import com.ibm.commons.util.StringUtil;
import com.raidomatic.JSFUtil;
import com.raidomatic.forum.model.Posts;
import com.raidomatic.forum.model.Post;
import java.util.*;

public class Search extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	public String getSearchQuery() throws UnsupportedEncodingException {
		HttpServletRequest request = JSFUtil.getRequest();
		String[] args = StringUtil.isEmpty(request.getPathInfo()) ? new String[] { } : request.getPathInfo().split("\\/");
		if(args.length > 1) {
			return URLDecoder.decode(args[1], "UTF-8");
		}
		return "";
	}
	public String getSearchScope() throws UnsupportedEncodingException {
		if(!StringUtil.isEmpty(this.getSearchQuery())) {
			return JSFUtil.getParam().get("scope");
		}
		return "";
	}

	public List<Post> getSearchResult() throws UnsupportedEncodingException {
		String searchQuery = this.getSearchQuery();
		String searchScope = this.getSearchScope();
		String query;
		if("Title".equals(searchScope) || "Body".equals(searchScope) || "CreatedBy".equals(searchScope)) {
			query = "[" + searchScope + "]=" + searchQuery;
		} else {
			query = searchQuery;
		}

		if(!StringUtil.isEmpty(query)) {
			return ((Posts)JSFUtil.getVariableValue("Posts")).searchSortedByDateTime(query);
		}

		return null;
	}
}
