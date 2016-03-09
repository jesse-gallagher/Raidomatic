package com.raidomatic.model;

import java.util.*;

import com.ibm.xsp.extlib.util.ExtLibUtil;

public class WoWVersions extends AbstractCollectionManager<WoWVersion> {

	public List<WoWVersion> getVersions() throws Exception {
		return this.getAll();
	}
	public List<WoWVersion> getVersionsByDate() throws Exception {
		return this.createCollection(WoWVersionList.BY_RELEASE_DATE, null, null, -1);
	}

	@SuppressWarnings("unchecked")
	public WoWVersion versionForDate(Date date) throws Exception {
		// Versions only rarely change and are visible to all, so heavy caching is good
		Map<String, Object> applicationScope = ExtLibUtil.getSessionScope();
		Map<Date, WoWVersion> cache = (Map<Date, WoWVersion>)applicationScope.get("$$WoWVersionCache");
		if(cache == null) {
			cache = new HashMap<Date, WoWVersion>();
			applicationScope.put("$$WoWVersionCache", cache);
		}

		if(!cache.containsKey(date)) {
			List<WoWVersion> versionList = this.getVersionsByDate();
			for(int i = versionList.size()-1; i >= 0; i--) {
				WoWVersion version = versionList.get(i);
				if(version != null && date != null && date.compareTo(version.getReleaseDate()) > 0) {
					cache.put(date, version);
					break;
				}
			}
		}

		return cache.get(date);
	}

	@Override
	protected AbstractDominoList<WoWVersion> createCollection() { return new WoWVersionList(); }

	private static final long serialVersionUID = -1634348118366095912L;


	public class WoWVersionList extends AbstractDominoList<WoWVersion> {
		public static final String BY_RELEASE_DATE = "ReleaseDate";
		public static final String BY_VERSION_NUMBER = "VersionNumber";
		public static final String BY_ID = "VersionID";

		public static final String DEFAULT_SORT = WoWVersionList.BY_ID;

		@Override
		protected String[] getColumnFields() { return new String[] { "id", "releaseDate", "versionNumber", "expansionName", "patchName", "url" }; }
		@Override
		protected boolean isSessionAsSigner() { return false; }
		@Override
		protected String getDatabasePath() { return "wow/common.nsf"; }
		@Override
		protected String getViewName() { return "WoW Versions"; }


		private static final long serialVersionUID = 2625683019476237314L;
	}
}