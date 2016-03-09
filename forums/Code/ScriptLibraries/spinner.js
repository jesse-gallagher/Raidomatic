function spinner(element, options) {
	element = element.srcElement ? element.srcElement : element.target ? element.target : element;
	options =
		options == true ? { hideCaller: true } :
		options ? options :
		{}

	var img = document.createElement("img")
	img.src = "/r/mozilla_spinner.gif"
	img.height = 18
	img.width = 18
	img.className = "spinner" + (options.className ? " " + options.className : "")
	if(options.style) {
		for(var node in options.style) {
			img.style[node] = options.style[node]
		}
	}
	if(options.id) {
		img.id = options.id
	}
	
	if(options.insideCaller == true) {
		element.appendChild(img)
	} else {
		if(element.parentNode.lastChild == element) {
			element.parentNode.appendChild(img)
		} else {
			element.parentNode.insertBefore(img, element.nextSibling)
		}
		
		if(options.hideCaller == true) element.style.display = "none"
		if(options.disableCaller == true) element.disabled = true
	}
}