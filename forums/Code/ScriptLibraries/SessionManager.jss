var DomSessionManager = {
	maintainSession: function() {
		return;
		var sessionID = cookie.get("SessionID");
		var domAuthSessID = cookie.get("DomAuthSessId");
		
		if(@UserName() != "Anonymous") {
			/*var newSessionID = new javax.servlet.http.Cookie("SessionID", sessionID.getValue());
			newSessionID.setMaxAge(60 * 60 * 24 * 14);
			newSessionID.setPath("/");
			var newDomAuthSessID = new javax.servlet.http.Cookie("DomAuthSessId", domAuthSessID.getValue());
			newDomAuthSessID.setMaxAge(60 * 60 * 24 * 14);
			newDomAuthSessID.setPath("/");*/
			
			var raidomaticAuthToken = new javax.servlet.http.Cookie("RaidomaticAuthToken1", DomSessionManager.encodeUsername(@UserName()));
			raidomaticAuthToken.setPath("/");
			raidomaticAuthToken.setMaxAge(60 * 60 * 24 * 60);
			
			var response = facesContext.getExternalContext().getResponse();
			//response.addCookie(newSessionID);
			//response.addCookie(newDomAuthSessID);
			response.addCookie(raidomaticAuthToken);
		}
	},
	clearSession: function() {
		var raidomaticAuthToken = new javax.servlet.http.Cookie("RaidomaticAuthToken1", "")
		raidomaticAuthToken.setPath("/")
		
		var response = facesContext.getExternalContext().getResponse()
		response.addCookie(raidomaticAuthToken)
	},
	establishSession: function(username) {
		var tempDoc = database.createDocument();
		tempDoc.replaceItemValue("Username", username);
		var un = session.evaluate(" @NameLookup([Exhaustive]; Username; 'ShortName') ", tempDoc)[0];
		tempDoc.recycle();
		
		var raidomaticAuthToken = new javax.servlet.http.Cookie("RaidomaticAuthToken1", DomSessionManager.encodeUsername(un));
		raidomaticAuthToken.setPath("/");
		raidomaticAuthToken.setMaxAge(60 * 60 * 24 * 60);
		
		var response = facesContext.getExternalContext().getResponse();
		response.addCookie(raidomaticAuthToken);
	},
	encodeUsername: function(username) {
		var key = new java.util.Vector();
		key.add(1111111);
		key.add(1111111);
		key.add(1111111);
		var encodedName = com.raidomatic.JSFUtil.xor(username, key);
		return new sun.misc.BASE64Encoder().encode(encodedName.getBytes());
	}
}