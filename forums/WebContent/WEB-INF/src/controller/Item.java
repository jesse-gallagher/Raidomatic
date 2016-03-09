package controller;

import static com.ibm.commons.util.StringUtil.isEmpty;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import com.ibm.xsp.component.UIViewRootEx2;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.raidomatic.JSFUtil;
import com.raidomatic.model.Items;
import frostillicus.controller.BasicModelPageController;

public class Item extends BasicModelPageController {
	private static final long serialVersionUID = 1L;

	@Override
	public void beforePageLoad() throws Exception {

		com.raidomatic.model.Item item = null;
		// Find the item
		try {
			HttpServletRequest request = JSFUtil.getRequest();
			if(!isEmpty(request.getPathInfo())) {
				String[] args = request.getPathInfo().split("\\/");
				Items items = (Items)JSFUtil.getVariableValue("Items");
				item = items.getById(args[1]);
			}
		} catch(Exception e) { }

		UIViewRootEx2 view = JSFUtil.getViewRoot();
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		if(item != null) {
			view.setPageTitle(item.getName());
		} else {
			view.setPageTitle("Error");
			viewScope.put("fatalErrors", new String[] { "The requested item does not exist or you are not permitted to access it." });
		}

		viewScope.put("item", item);
	}

	public com.raidomatic.model.Item getItem() {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		return (com.raidomatic.model.Item)viewScope.get("item");
	}
}
