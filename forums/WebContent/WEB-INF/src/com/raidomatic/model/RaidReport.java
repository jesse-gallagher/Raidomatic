package com.raidomatic.model;

import java.util.*;
import lombok.*;

import com.raidomatic.JSFUtil;
//import com.raidomatic.ModelCache;
import com.raidomatic.forum.model.Topic;
import com.raidomatic.forum.model.Topics;

import lotus.domino.*;

public class RaidReport extends AbstractModel {
	private @Getter @Setter String id;
	private @Getter @Setter String url;
	private @Getter @Setter List<String> bossIds;
	private @Getter @Setter String topicId;
	private @Getter @Setter List<String> eventLeader;
	private @Getter @Setter String notes;
	private @Getter @Setter int group = 1;
	private @Getter @Setter String createdBy;

	public String getGroupText() { return group + ""; }
	public void setGroupText(String groupText) {
		this.group = Integer.parseInt(groupText);
	}

	public Topic getTopic() throws Exception {
		Topics topics = new Topics();
		return topics.getById(this.getTopicId());
	}

	public boolean save(boolean asSigner) {
		try {
			//Session session = (Session)JSFUtil.getBindingValue("#{session}");
			Database database = JSFUtil.getDatabase();

			Document doc;
			if(this.getDocExists()) {
				doc = asSigner ? this.getDocumentAsSigner() : this.getDocument();
			} else {
				doc = database.createDocument();
				JSFUtil.registerProductObject(doc);
				doc.replaceItemValue("Form", "Raid Report");
				doc.makeResponse(this.getTopic().getDocument());
				this.setId(doc.getUniversalID());
			}

			doc.replaceItemValue("RaidReportID", this.getId());
			doc.replaceItemValue("TopicID", this.getTopicId());
			doc.replaceItemValue("BossIDs", this.getBossIds());
			doc.replaceItemValue("URL", this.getUrl());
			doc.replaceItemValue("Notes", this.getNotes());
			doc.replaceItemValue("Group", this.getGroup());

			this.setEventLeader(this.getTopic().getEventLeader());
			doc.replaceItemValue("EventLeader", this.getEventLeader());
			lotus.domino.Item eventLeader = doc.getFirstItem("EventLeader");
			JSFUtil.registerProductObject(eventLeader);
			eventLeader.setAuthors(true);

			doc.replaceItemValue("CreatedBy", this.getCreatedBy());

			doc.replaceItemValue("Authors", JSFUtil.getUserName());
			lotus.domino.Item authors = doc.getFirstItem("Authors");
			JSFUtil.registerProductObject(authors);
			for(String leader : this.getTopic().getEventLeader()) {
				authors.appendToTextList(leader);
			}
			authors.appendToTextList(this.getCreatedBy());
			authors.appendToTextList("[RaidLeader]");
			authors.appendToTextList("[Admin]");
			authors.setAuthors(true);

			doc.computeWithForm(false, false);
			doc.save();
			this.setUniversalId(doc.getUniversalID());
			this.clearCache();

			//			try {
			//				eventLeader.recycle();
			//				authors.recycle();
			//				doc.recycle();
			//			} catch(NotesException ne) { }

		} catch(Exception ne) {
			ne.printStackTrace();
			return false;
		}
		return true;
	}
	@Override
      public boolean save() { return this.save(false); }

	@Override
      public void clearCache() throws Exception {
		//		ModelCache cache = JSFUtil.getModelCache();
		//		cache.clearMatches(".*-RaidReportList-.*");
		//		
		//		cache.clear();
		//		
		JSFUtil.getModelIDCache().clear();
	}


	public void remove() throws Exception {
		Document doc = this.getDocumentAsSigner();
		doc.replaceItemValue("Form", "Deleted" + doc.getItemValueString("Form"));
		doc.save(false, false);
		//		try { doc.recycle(); } catch(NotesException ne) { }
		this.clearCache();
	}

	private static final long serialVersionUID = -5991525016482590299L;
}
