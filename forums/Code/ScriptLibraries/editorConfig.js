CKEDITOR.editorConfig = function(config) {
    config.removePlugins = 'contextmenu';
    
    /* code from default config file */
    config.dialog_backgroundCoverColor = 'black';
    config.dialog_backgroundCoverOpacity = 0.3;
    config.skin='lotus';
    config.dialog_startupFocusTab = true;
    config.colorButton_enableMore = false;
    config.resize_enabled = false;
    config.toolbarCanCollapse = false;
    config.disableNativeSpellChecker = false;
    config.forceEnterMode = true;

    var removeRegex = new RegExp('(?:^|,)(?:scayt|wsc|contextmenu)(?=,|$)' , 'g');
    config.plugins = config.plugins.replace(removeRegex, '');
    config.plugins += ',sametimeemoticons,customdialogs,lotustoolbars,urllink,doclink';
    
    // Paste from Word configuration
    config.pasteFromWordRemoveFontStyles = false;
    config.pasteFromWordRemoveStyles = false;

    //Example Lotus Spell Checker config.
    /*
    config.extraPlugins += ',lotusspellchecker';
    config.lotusSpellChecker = {
        restUrl:'',
        lang:'en',
        suggestions:'5',
        format:'json',
        highlight: { element : 'span', styles : { 'background-color' : 'yellow', 'color' : 'black' } },
        preventCache: true
    };
    */

    // See the release notes for how to add a custom link dialog to the MenuLink button menu.
    config.menus = {
        // Create a menu called MenuLink containing a menu item for the urllink command.
        link : {
            buttonClass : 'cke_button_link',
            commands : ['link'],
            label : 'link.title'
        },

        // Create a menu called MenuPaste containing menu items for the specified commands.
        paste : {
            buttonClass : 'cke_button_paste',
            groupName : 'clipboard',
            commands : ['paste', 'pastetext', 'pastefromword', 'doclink']
            // label will default to editor.lang.ibm.menu.paste
        }
    };
}