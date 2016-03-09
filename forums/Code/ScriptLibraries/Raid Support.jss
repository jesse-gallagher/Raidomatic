var RaidSupport = {
	tiers: null,
	
	getLootTier: function(percent) {
		if(RaidSupport.tiers == null) {
			RaidSupport.tiers = []
			
			var tiersView:NotesView = database.getView("Loot Ratio Tiers")
			tiersView.setAutoUpdate(false)
			var tierNav:NotesViewNavigator = tiersView.createViewNav()
			
			var tierEntry:NotesViewEntry = tierNav.getFirst()
			while(tierEntry != null) {
				RaidSupport.tiers.push({
					min: tierEntry.getColumnValues()[0],
					max: tierEntry.getColumnValues()[1],
					name: tierEntry.getColumnValues()[2]
				})
				
				tierEntry = tierNav.getNext(tierEntry)
			}
			
			tierNav.recycle()
			tiersView.recycle()
		}
		for(var i = 0; i < RaidSupport.tiers.length; i++) {
			if(RaidSupport.tiers[i].max && percent < RaidSupport.tiers[i].max) {
				return RaidSupport.tiers[i]
			}
		}
		
		return RaidSupport.tiers[RaidSupport.tiers.length-1]
	}
}