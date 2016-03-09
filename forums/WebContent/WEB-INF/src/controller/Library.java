package controller;

import com.ibm.xsp.component.xp.XspDataIterator;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.raidomatic.JSFUtil;
import com.raidomatic.forum.model.Posts;

import frostillicus.controller.BasicXPageController;

public class Library extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	public void photosBack() {
		XspDataIterator photos = (XspDataIterator)ExtLibUtil.getComponentFor(JSFUtil.getViewRoot(), "photos");
		int newIndex = photos.getFirst() - photos.getRows();
		photos.setFirst(newIndex < 0 ? 0 : newIndex);
	}
	public void photosForward() throws Exception {
		XspDataIterator photos = (XspDataIterator)ExtLibUtil.getComponentFor(JSFUtil.getViewRoot(), "photos");
		int newIndex = photos.getFirst() + photos.getRows();

		Posts posts = (Posts)JSFUtil.getVariableValue("Posts");
		if(newIndex < posts.getScreenshotPosts().size()) {
			photos.setFirst(newIndex);
		}
	}
}
