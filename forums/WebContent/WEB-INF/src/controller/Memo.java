package controller;

import java.util.List;
import java.util.Map;
import lotus.domino.NotesException;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.domino.wrapped.DominoDocument;
import com.raidomatic.JSFUtil;
import com.raidomatic.model.Players;
import frostillicus.controller.BasicXPageController;

public class Memo extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public List<String> getPlayerNames() throws NotesException {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		if(!viewScope.containsKey("$$playerNames")) {
			Players players = new Players();
			viewScope.put("$$playerNames", players.getNames());
		}
		return (List<String>)viewScope.get("$$playerNames");
	}

	public String getDefaultSubject() {
		DominoDocument originalMemo = (DominoDocument)JSFUtil.getVariableValue("originalMemo");
		if(originalMemo != null) {
			String subject = (String)originalMemo.getValue("Subject");
			if(subject.startsWith("Re: ")) { return subject; }
			return "Re: " + subject;
		}
		return "";
	}
	public String send() throws NotesException {
		DominoDocument memo = (DominoDocument)JSFUtil.getVariableValue("memo");
		memo.getDocument(true).send();
		return "xsp-success";
	}
}
