package com.raidomatic.model;

import java.util.*;
import lotus.domino.*;
import lombok.*;

import com.raidomatic.JSFUtil;
//import com.raidomatic.ModelCache;

public class ShoutboxEntry extends AbstractModel {
	private @Getter @Setter String id;
	private @Getter @Setter Date dateTime;
	private @Getter @Setter String createdBy;
	private @Getter @Setter String body;

	public boolean save(boolean asSigner) {
		try {
			Database database = JSFUtil.getDatabase();

			Document doc;
			if(this.getDocExists()) {
				doc = asSigner ? this.getDocumentAsSigner() : this.getDocument();
			} else {
				doc = database.createDocument();
				JSFUtil.registerProductObject(doc);
				doc.replaceItemValue("Form", "Shoutbox Entry");
				this.setId(doc.getUniversalID());
			}

			doc.replaceItemValue("ShoutboxEntryID", this.getId());
			doc.replaceItemValue("DateTime", this.createDateTime(this.getDateTime() == null ? new Date() : this.getDateTime()));
			doc.replaceItemValue("CreatedBy", this.getCreatedBy() == null ? JSFUtil.getUserName() : this.getCreatedBy());
			doc.replaceItemValue("Body", this.getBody());

			doc.replaceItemValue("Authors", JSFUtil.getUserName());
			lotus.domino.Item authors = doc.getFirstItem("Authors");
			JSFUtil.registerProductObject(authors);
			authors.appendToTextList("[Admin]");
			authors.appendToTextList(this.getCreatedBy() == null ? JSFUtil.getUserName() : this.getCreatedBy());
			authors.setAuthors(true);

			doc.computeWithForm(false, false);
			doc.save();
			this.setUniversalId(doc.getUniversalID());
			this.clearCache();

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
		//		cache.clearMatches(".*-ShoutboxEntryList-.*");
		//		
		//		// Clear everything for now
		//		cache.clear();

		JSFUtil.getModelIDCache().clear();
	}

	private static final long serialVersionUID = -7570297824355595632L;
}
