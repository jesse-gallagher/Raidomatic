package com.raidomatic.model;

import java.util.*;
import com.raidomatic.JSFUtil;
import lombok.*;

public class Item extends AbstractModel {
	private @Getter @Setter String id;
	private @Getter @Setter String name;
	private @Getter @Setter int level;
	private @Getter @Setter double gearScore;
	private @Getter @Setter int qualityId;
	private @Getter @Setter String qualityName;
	private @Getter @Setter int classId;
	private @Getter @Setter String className;
	private @Getter @Setter int subclassId;
	private @Getter @Setter String subclassName;
	private @Getter @Setter int iconId;
	private @Getter @Setter String iconName;
	private @Getter @Setter int slotId;
	private @Getter @Setter String slotName;
	private @Getter @Setter String htmlTooltip;
	private @Getter @Setter String json;
	private @Getter @Setter String jsonEquip;
	private @Getter @Setter String link;

	public List<Loot> getLoots() {
		Loots loots = new Loots();
		return loots.getLootsForItemId(this.getId());
	}

	public boolean isHeroic() {
		return this.getJson().contains("\"heroic\":1");
	}
	public boolean isRaidFinder() {
		return this.getJson().contains("\"raidfinder\":1") || this.getHtmlTooltip().contains("<span style=\"color: #00FF00\">Raid Finder</span>");
	}
	//public boolean getIsHeroic() { return this.isHeroic(); }

	@SuppressWarnings("unchecked")
	public boolean matchesMainFormula(Spec spec) throws Exception {
		lotus.domino.Document doc = this.getDocument();
		List result = JSFUtil.getSession().evaluate(spec.getMainItemFormula(), doc);
		//doc.recycle();
		return ((Double)result.get(0)).intValue() == 1;
	}
	@SuppressWarnings("unchecked")
	public boolean matchesOffFormula(Spec spec) throws Exception {
		lotus.domino.Document doc = this.getDocument();
		List result = JSFUtil.getSession().evaluate(spec.getOffItemFormula(), doc);
		//doc.recycle();
		return ((Double)result.get(0)).intValue() == 1;
	}

	public Collection<Signup> filterSignupList(Collection<Signup> lineup) throws Exception {

		Collection<Signup> result = new TreeSet<Signup>(new Signup.CharacterNameComparator());
		for(Signup signup : lineup) {
			Spec primarySpec = signup.getCharacter().getPrimarySpecObject();
			Spec secondarySpec = signup.getCharacter().getSecondarySpecObject();

			if(primarySpec != null || secondarySpec != null) {
				boolean matched = false;

				if(primarySpec != null && (this.matchesMainFormula(primarySpec) || this.matchesOffFormula(primarySpec))) { matched = true; }
				if(!matched && secondarySpec != null && (this.matchesMainFormula(secondarySpec) || this.matchesOffFormula(secondarySpec))) { matched = true; }

				if(matched) { result.add(signup); }
			}
		}

		return result;
	}

	@Override
	public lotus.domino.Database getDatabase() {
		try {
			return JSFUtil.getSession().getDatabase("", "wow/common.nsf");
		} catch(Exception e) { return null; }
	}

	private static final long serialVersionUID = -7163958037655303261L;
}