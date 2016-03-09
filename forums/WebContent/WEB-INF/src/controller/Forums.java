package controller;

import frostillicus.controller.BasicXPageController;
import java.util.*;

import com.raidomatic.JSFUtil;
import com.raidomatic.prefs.UserPrefs;

public class Forums extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	@Override
	public void beforePageLoad() {
		Map<String, String> param = JSFUtil.getParam();
		UserPrefs userPrefs = (UserPrefs)JSFUtil.getVariableValue("UserPrefs");
		if("index".equals(param.get("mode"))) {
			userPrefs.getCurrent().setForumPageMode("");
		} else if("recent".equals(param.get("mode"))) {
			userPrefs.getCurrent().setForumPageMode("Recent");
		}
	}
}
