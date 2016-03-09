package com.raidomatic;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.servlet.http.HttpServletRequest;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.component.UIViewRootEx2;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.DataSource;
import com.ibm.xsp.model.domino.DominoViewData;

import com.raidomatic.prefs.UserPrefs;
import com.raidomatic.model.*;
import lotus.domino.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import lombok.*;

public class JSFUtil {

	private JSFUtil() { }

	public static Object getBindingValue(String ref) {
		FacesContext context = FacesContext.getCurrentInstance();
		Application application = context.getApplication();
		return application.createValueBinding(ref).getValue(context);
	}
	public static void setBindingValue(String ref, Object newObject) {
		FacesContext context = FacesContext.getCurrentInstance();
		Application application = context.getApplication();
		ValueBinding binding = application.createValueBinding(ref);
		binding.setValue(context, newObject);
	}

	public static Object getVariableValue(String varName) {
		FacesContext context = FacesContext.getCurrentInstance();
		return context.getApplication().getVariableResolver().resolveVariable(context, varName);
	}

	public static ModelCache getModelCache() {
		return ModelCache.getInstance();
		//return (ModelCache)getVariableValue("ModelCache");
	}
	@SuppressWarnings("unchecked")
	public static Map<Object, Object> getMiscDBCache() {
		return (Map<Object, Object>)getVariableValue("MiscDBCache");
	}
	public static Map<String, AbstractModel> getModelIDCache() {
		//return ModelIDCache.getInstance();
		return (ModelIDCache)getVariableValue("ModelIDCache");
	}
	@SneakyThrows
	public static String getUserName() {
		return getSession().getEffectiveUserName();
	}
	public static Session getSession() {
		return ExtLibUtil.getCurrentSession();
	}
	public static Session getSessionAsSigner() {
		return ExtLibUtil.getCurrentSessionAsSigner();
	}
	public static Database getDatabase() {
		return ExtLibUtil.getCurrentDatabase();
	}
	public static ReadMarkManager getReadMarkManager() {
		return new ReadMarkManager();
	}
	public static Configuration getConfiguration() {
		return new Configuration();
	}
	public static UserPrefs getUserPrefs() {
		return new UserPrefs();
	}
	public static UIViewRootEx2 getViewRoot() {
		return (UIViewRootEx2)getVariableValue("view");
	}
	public static DominoProductSet getDominoProductSet() {
		return (DominoProductSet)getVariableValue("DominoProductSet");
	}

	public static void updateFTIndex() throws NotesException {
		Database dbAsSigner = getSessionAsSigner().getCurrentDatabase();
		dbAsSigner.updateFTIndex(true);
	}

	public static void registerProductObject(Base domObject) {
		//		if(domObject != null) {
		//			JSFUtil.getDominoProductSet().add(domObject);
		//		}
	}

	public static View getView(String serverName, String databaseName, String viewName) throws NotesException {
		if(serverName == null) { serverName = ""; }
		if(databaseName == null) { databaseName = ""; }

		//System.out.println("Looking for data source: " + serverName + ", " + databaseName + ", " + viewName);

		try {
			UIViewRootEx2 viewRoot = JSFUtil.getViewRoot();
			for(DataSource dataSource : viewRoot.getData()) {
				if(dataSource instanceof DominoViewData) {
					DominoViewData viewSource = (DominoViewData)dataSource;
					String sourceDatabaseName = (serverName.length() == 0 ? "" : (serverName + "!!")) + databaseName;

					// Data sources in the current database have null database names
					boolean matchesDB =
						(viewSource.getDatabaseName() == null && sourceDatabaseName.length() == 0) ||
						(viewSource.getDatabaseName() != null && viewSource.getDatabaseName().equalsIgnoreCase(sourceDatabaseName));

					if(matchesDB && viewSource.getViewName().equalsIgnoreCase(viewName)) {
						// If we found the view as a data source, then we don't need to fetch a new one - return it
						System.out.println("Returning data source for " +
								(serverName.length() == 0 ? "current server" : serverName) + ", " +
								(databaseName.length() == 0 ? "current database" : databaseName) + ", " +
								viewName
						);
						return viewSource.getView();
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		Database database;
		if(databaseName.length() == 0) {
			database = JSFUtil.getDatabase();
		} else {
			database = JSFUtil.getSession().getDatabase(serverName, databaseName);
		}
		return database.getView(viewName);
	}

	public static Map<String, String> getUserInfo(String username) throws NotesException {
		Map<String, String> result = new HashMap<String, String>();

		Players players = new Players();
		Player player = players.getCurrent();
		if(player != null) {
			result.put("mailServer", player.getMailServer());
			result.put("mailFile", player.getMailFile());
			result.put("mailDomain", player.getMailDomain());
			result.put("mailSystem", player.getMailSystem());
		}

		return result;
	}
	public static Map<String, String> getCurrentUserInfo() throws NotesException {
		return getUserInfo(getUserName());
	}
	public static int getInboxUnreadCount() {
		try {
			//Map<String, String> userInfo = getCurrentUserInfo();
			Players players = new Players();
			Player player = players.getCurrent();

			if(player == null || !player.getMailServer().equals(JSFUtil.getDatabase().getServer())) { return 0; }

			Database mailDB = getSession().getDatabase(player.getMailServer(), player.getMailFile());
			JSFUtil.registerProductObject(mailDB);
			int count = 0;
			if(mailDB.isOpen()) {
				View inbox = mailDB.getView("$Inbox");
				JSFUtil.registerProductObject(inbox);
				ViewEntryCollection unread = inbox.getAllUnreadEntries();
				JSFUtil.registerProductObject(unread);
				count = unread.getCount();
				//				unread.recycle();
				//				inbox.recycle();
				//				mailDB.recycle();
			}
			return count;
		} catch(Exception e) {
			return 0;
		}
	}

	public static String pluralize(String input) {
		if(input.endsWith("s")) {
			return input + "es";
		} else if(input.endsWith("y")) {
			return input.substring(0, input.length()-2) + "ies";
		}
		return input + "s";
	}
	public static String singularize(String input) {
		if(input.endsWith("ses")) {
			return input.substring(0, input.length()-2);
		} else if(input.endsWith("ies")) {
			return input.substring(0, input.length()-3) + "y";
		} else if(input.endsWith("s")) {
			return input.substring(0, input.length()-1);
		}
		return input;
	}

	public String fetchURL(String urlString) throws Exception {
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestProperty("User-Agent", "Firefox/2.0");
		//conn.setRequestProperty("Cookie", cookie);

		BufferedReader in = new BufferedReader(new InputStreamReader((InputStream)conn.getContent()));
		StringWriter resultWriter = new StringWriter();
		String inputLine;
		while((inputLine = in.readLine()) != null) {
			resultWriter.write(inputLine);
		}
		in.close();

		return resultWriter.toString().replace("<HTTP-EQUIV", "<meta http-equiv");

	}

	@SuppressWarnings("unchecked")
	public static String xor(String input, Vector key) {
		StringBuilder output = new StringBuilder();

		for(int i = 0; i < input.length(); i++) {
			int character = input.codePointAt(i);
			int keyNode = ((Double)key.get(i % key.size())).intValue();

			int onePass = character ^ keyNode;

			output.append((char)onePass);
		}

		return output.toString();
	}

	public static String strLeft(String input, String delimiter) {
		return input.substring(0, input.indexOf(delimiter));
	}
	public static String strRight(String input, String delimiter) {
		return input.substring(input.indexOf(delimiter) + delimiter.length());
	}
	public static String strLeftBack(String input, String delimiter) {
		return input.substring(0, input.lastIndexOf(delimiter));
	}
	public static String strRightBack(String input, String delimiter) {
		return input.substring(input.lastIndexOf(delimiter) + delimiter.length());
	}

	@SuppressWarnings("unchecked")
	public static List<String> toStringList(Object columnValue) {
		List<String> result = new Vector<String>();
		if(columnValue.getClass().getName().equals("java.util.Vector")) {
			for(Object reader : (Vector)columnValue) {
				result.add((String)reader);
			}
		} else if(((String)columnValue).length() > 0) {
			result.add((String)columnValue);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static List<Integer> toIntegerList(Object columnValue) {
		List<Integer> result = new Vector<Integer>();
		if(columnValue.getClass().getName().equals("java.util.Vector")) {
			for(Object element : (Vector)columnValue) {
				result.add(((Double)element).intValue());
			}
		} else {
			result.add(((Double)columnValue).intValue());
		}
		return result;
	}

	public static int toInteger(Object columnValue) {
		int result = 0;
		if(columnValue.getClass().getName().equals("java.lang.String")) {
			result = 0;
		} else {
			result = ((Double)columnValue).intValue();
		}
		return result;
	}
	public static Date toDate(Object columnValue) throws NotesException {
		return ((DateTime)columnValue).toJavaDate();
	}

	public static HttpServletRequest getRequest() {
		return (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String> getParam() {
		return (Map<String, String>)getVariableValue("param");
	}

	public static String getContextPath() {
		return FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
	}
	public static void appRedirect(String appPath) throws IOException {
		String cleanPath = appPath.startsWith("/") ? appPath : "/" + appPath;
		redirect(getContextPath() + cleanPath);
	}
	public static void redirect(String url) throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().redirect(url);
	}
	public static String[] getPathInfoArgs() {
		HttpServletRequest request = getRequest();
		return StringUtil.isEmpty(request.getPathInfo()) ? new String[] { } : request.getPathInfo().split("\\/");
	}
}