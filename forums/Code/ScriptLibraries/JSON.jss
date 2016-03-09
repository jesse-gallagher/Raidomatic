var JSON = {
		serialize: function(input) {
			switch(this.identify(input)) {
				case "string":
				case "date":
					return "\"" + input.toString().replace("\"", "\\\"") + "\""
				case "number":
				case "boolean":
					return input
				case "array":
					var result = []
					for(var i = 0; i < input.length; i++) {
						result.push(JSON.serialize(input[i]))
						
					}
					return "[\n" + result.join(", ") + "\n]"
				case "object":
					var result = []
					for(var node in input) {
						result.push(JSON.serialize(node) + ": " + JSON.serialize(input[node]))
					}
					return "{\n" + result.join(", ") + "\n}"
			}
		},
		
		identify: function(input) {
			if(typeof(input) == "java.util.Vector") {
				return "array"
			} else if(typeof(input) != 'object') {
				return typeof(input)
			} else if(input.getClass) {
				if(input.getClass().getName().toString() == "java.util.Date") {
					return "date"
				}
				return input.getClass().getName().toString()
			} else if(input.push) {
				return "array"
			} else {
				return "object"
			}
		}
}