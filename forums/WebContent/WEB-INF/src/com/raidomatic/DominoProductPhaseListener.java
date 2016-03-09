package com.raidomatic;

import javax.faces.event.*;

public class DominoProductPhaseListener implements PhaseListener {

	public void afterPhase(PhaseEvent event) {
		//		JSFUtil.getDominoProductSet().recycle();
	}
	public void beforePhase(PhaseEvent event) { }
	public PhaseId getPhaseId() { return PhaseId.RENDER_RESPONSE; }

	private static final long serialVersionUID = 5588323592318324922L;
}