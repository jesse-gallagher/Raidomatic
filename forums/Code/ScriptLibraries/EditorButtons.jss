function getRichTextEditorButtons() {
	var result = [], currentGroup = 0, current = null
	var editorButtons:NotesView = database.getView("Editor Buttons")
	var nav:NotesViewNavigator = editorButtons.createViewNav()
	var entry:NotesViewEntry = nav.getFirst()
	while(entry != null) {
		if(entry.isCategory()) {
			currentGroup = entry.getColumnValues()[0]
			current = []
			result.push(current)
		} else {
			current.push(entry.getColumnValues()[3])
		}
	
		entry = nav.getNext(entry)
	}
	return result
}