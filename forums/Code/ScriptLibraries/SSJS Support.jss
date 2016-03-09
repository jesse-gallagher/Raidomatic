if(typeof RUtils == "undefined") {
	RUtils = {
		webDbName: function() {
			return database.getFilePath().replace("\\", "/")
		},
		urlEncode: function(val) {
			return val == null ? "" : java.net.URLEncoder.encode(val, "UTF-8")
		},
		textualList: function(val) {
			if(val.length == 1) { return val.toString(); }
			if(val.length == 2) { return val[0] + (val[0].endsWith(" ") ? "" : " ") + "and " + val[1]; }
			return RUtils.textualList([@LeftBack(val.join(", "), ", ") + ", ", val[val.length-1]])
		},
		userRoles: function() {
			return context.getUser().getRoles();
		},
		trimArray: function(val) {
			var result = [];
			for(var i = 0; i < val.length; i++) {
				if(val[i] != null && val[i] != "") {
					result.push(val[i]);
				}
			}
			return result;
		},
		tidy: function(val) {
			try {
				java.lang.Class.forName("org.w3c.tidy.Tidy");
		
				var nullOutput = new java.io.PrintWriter(new java.io.ByteArrayOutputStream());
				
				var tidy = new org.w3c.tidy.Tidy();
				tidy.setXHTML(true);
				tidy.setIndentAttributes(false);
				tidy.setIndentCdata(false);
				tidy.setIndentContent(false);
				tidy.setWrapAttVals(false);
				tidy.setWraplen(65535);
				tidy.setInputEncoding("UTF-8");
				tidy.setAltText(" ");
				tidy.setPrintBodyOnly(true);
				//tidy.setErrout(out);
				tidy.setForceOutput(true);
				//tidy.setOnlyErrors(true);
				tidy.setShowWarnings(false);
				tidy.setQuiet(true);
				tidy.setErrout(nullOutput);
				
				var output = new java.io.ByteArrayOutputStream();
				var input = new java.io.ByteArrayInputStream(val.toString().getBytes());
				tidy.parse(input, output);
				return output.toString();
			} catch(e) {
				//e.printStackTrace();
				return val;
			}
		}
	}
}