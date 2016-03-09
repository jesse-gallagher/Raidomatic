package controller;

import javax.faces.component.UIInput;
import frostillicus.controller.BasicXPageController;
import java.util.*;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.raidomatic.JSFUtil;
import com.raidomatic.model.ShoutboxEntry;
import com.raidomatic.wowhead.News;
import com.sun.syndication.feed.synd.SyndEntry;

public class Home extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	public void submitShout() {
		try {
			UIInput shoutboxInput = (UIInput)ExtLibUtil.getComponentFor(JSFUtil.getViewRoot(), "shoutboxInput");
			ShoutboxEntry shout = new ShoutboxEntry();
			shout.setDateTime(new Date());
			shout.setCreatedBy(JSFUtil.getUserName());
			shout.setBody((String)shoutboxInput.getValue());
			shout.save();

			shoutboxInput.setValue("");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, String>> getNewsEntries() {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		if(!viewScope.containsKey("$$newsEntries")) {
			List<Map<String, String>> newsEntries = new ArrayList<Map<String, String>>();
			try {
				News wowheadNews = (News)JSFUtil.getVariableValue("WowheadNews");
				for(SyndEntry entry : wowheadNews.getNews()) {
					Map<String, String> thisEntry = new HashMap<String, String>();
					thisEntry.put("uri", entry.getUri());
					thisEntry.put("title", entry.getTitle());
					newsEntries.add(thisEntry);
				}
			} catch(Throwable e) {

			}
			viewScope.put("$$newsEntries", newsEntries);
		}
		return (List<Map<String, String>>)viewScope.get("$$newsEntries");
	}
}
