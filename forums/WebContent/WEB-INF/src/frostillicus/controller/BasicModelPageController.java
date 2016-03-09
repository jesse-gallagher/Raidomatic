package frostillicus.controller;

import javax.servlet.http.HttpServletRequest;
import com.raidomatic.JSFUtil;

public class BasicModelPageController extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	private String baseURL;

	public String getBaseURL() {
		if(this.baseURL == null) {
			HttpServletRequest request = JSFUtil.getRequest();
			String pageName = JSFUtil.getViewRoot().getPageName();
			this.baseURL = JSFUtil.pluralize(pageName.substring(0, pageName.length()-4)) + request.getPathInfo();
		}
		return this.baseURL;
	}
}
