package com.raidomatic.bnet.wow;

import java.net.*;
import java.io.*;
import java.util.*;
import org.json.*;

import java.awt.Color;

public class API implements Serializable {
	private static final long serialVersionUID = -6670790365828280177L;
	boolean useSSL = false;
	String host = "us.battle.net";
	PrintWriter out;

	Map<String, Character> characterCache;
	Map<Integer, Item> itemCache;

	public API() {
		out = new PrintWriter(new ByteArrayOutputStream());
		characterCache = new HashMap<String, Character>();
		itemCache = new HashMap<Integer, Item>();
	}
	public void setOutput(PrintWriter out) {
		if(this.out != null) { this.out.close(); }
		this.out = out;
	}
	public PrintWriter getOutput() {
		return this.out;
	}

	public Realm getRealm(String realmName) throws IOException, JSONException {
		String url = getBaseURL() + "/realm/status?realms=" + generateEncodedSlug(realmName);

		JSONObject json = fetchJSON(url);
		JSONArray realms = json.getJSONArray("realms");
		JSONObject realm = realms.getJSONObject(0);

		return createRealm(realm);
	}
	public Map<String, Realm> getRealms(List<String> realmNames) throws IOException, JSONException {
		Map<String, Realm> result = new HashMap<String, Realm>();
		for(String realmName : realmNames) {
			result.put(realmName, getRealm(realmName));
		}
		return result;
	}

	public Character getCharacter(String realmName, String characterName) throws IOException, JSONException, CharacterNotFoundException, Exception {
		if(!characterCache.containsKey(realmName + characterName)) {
			String url = getBaseURL() + "/character/" + generateEncodedSlug(realmName) + "/" + generateEncodedSlug(characterName) + "?fields=guild,talents";

			JSONObject json = null;
			try {
				json = fetchJSON(url);
			} catch(FileNotFoundException fnf) {
				throw new CharacterNotFoundException();
			}
			if(json.has("status") && json.getString("status").equals("nok")) {
				if(json.getString("reason").equals("Character not found")) {
					throw new CharacterNotFoundException();
				} else {
					throw new Exception(json.getString("reason"));
				}
			}

			characterCache.put(realmName + characterName, createCharacter(json));
		}
		return characterCache.get(realmName + characterName);
	}

	public Item getItem(int itemId) throws Exception {
		if(itemId == 0) { return null; }

		if(!itemCache.containsKey(itemId)) {
			String url = getBaseURL() + "/item/" + itemId;

			JSONObject json = null;
			try {
				json = fetchJSON(url);
			} catch(FileNotFoundException fnf) {
				throw new ItemNotFoundException("Item not found: " + itemId);
			}
			if(json.has("status") && json.getString("status").equals("nok")) {
				if(json.getString("reason").equals("unable to get item information.")) {
					throw new ItemNotFoundException();
				} else {
					throw new Exception(json.getString("reason"));
				}
			}

			itemCache.put(itemId, createItem(json));
		}
		return itemCache.get(itemId);
	}

	public List<News> getNews(String realmName, String guildName) throws Exception {
		String url = getBaseURL() + "/guild/" + generateEncodedSlug(realmName) + "/" + java.net.URLEncoder.encode(guildName, "UTF-8").replace("+", "%20") + "?fields=news";

		JSONObject json = null;
		try {
			json = fetchJSON(url);
		} catch(FileNotFoundException fnf) {
			throw new GuildNotFoundException();
		}
		if(json.has("status") && json.getString("status").equals("nok")) {
			if(json.getString("reason").equals("Guild not found.")) {
				throw new GuildNotFoundException();
			} else {
				throw new Exception(json.getString("reason"));
			}
		}

		List<News> result = new Vector<News>();
		JSONArray news = json.getJSONArray("news");
		for(int i = 0; i < news.length(); i++) {
			result.add(createNews(news.getJSONObject(i)));
		}
		return result;
	}

	public static String generateSlug(String input) {
		return input.toLowerCase().replace(' ', '-');
	}
	private static String generateEncodedSlug(String input) {
		String slug = generateSlug(input);
		try {
			return URLEncoder.encode(slug, "UTF-8");
		} catch(UnsupportedEncodingException uee) { }
		return slug;
	}


	private Realm createRealm(JSONObject realmData) throws JSONException {
		Realm realm = new Realm();
		realm.setApi(this);

		realm.setType(realmData.getString("type"));
		realm.setQueue(realmData.getBoolean("queue"));
		realm.setStatus(realmData.getBoolean("status"));
		realm.setPopulation(realmData.getString("population"));
		realm.setName(realmData.getString("name"));
		realm.setSlug(realmData.getString("slug"));

		return realm;
	}
	private Character createCharacter(JSONObject characterData) throws JSONException {
		Character character = new Character();
		character.setApi(this);

		character.setLastModified(new Date(characterData.getLong("lastModified")));
		character.setName(characterData.getString("name"));
		character.setRealmName(characterData.getString("realm"));
		character.setClassId(characterData.getInt("class"));
		character.setRaceId(characterData.getInt("race"));
		character.setGenderId(characterData.getInt("gender"));
		character.setLevel(characterData.getInt("level"));
		character.setAchievementPoints(characterData.getLong("achievementPoints"));
		character.setThumbnail(characterData.getString("thumbnail"));
		if(characterData.has("guild")) {
			character.setGuild(createGuild(characterData.getJSONObject("guild")));
		}
		if(characterData.has("talents")) {
			JSONArray talents = characterData.getJSONArray("talents");
			character.setPrimaryTalentSpec(createTalentSpec(talents.getJSONObject(0)));
			character.setSecondaryTalentSpec(createTalentSpec(talents.getJSONObject(1)));
		}

		return character;
	}
	private Guild createGuild(JSONObject guildData) throws JSONException {
		Guild guild = new Guild();
		guild.setApi(this);

		guild.setName(guildData.getString("name"));
		guild.setRealmName(guildData.getString("realm"));
		guild.setLevel(guildData.getInt("level"));
		guild.setMembers(guildData.getInt("members"));
		guild.setAchievementPoints(guildData.getLong("achievementPoints"));
		guild.setEmblem(createGuildEmblem(guildData.getJSONObject("emblem")));

		return guild;
	}
	private GuildEmblem createGuildEmblem(JSONObject emblemData) throws JSONException {
		GuildEmblem emblem = new GuildEmblem();
		emblem.setApi(this);

		emblem.setIcon(emblemData.getInt("icon"));
		emblem.setBorder(emblemData.getInt("border"));

		emblem.setIconColor(createColor(emblemData.getString("iconColor")));
		emblem.setBorderColor(createColor(emblemData.getString("borderColor")));
		emblem.setBackgroundColor(createColor(emblemData.getString("backgroundColor")));

		return emblem;
	}
	private TalentSpec createTalentSpec(JSONObject talentData) throws JSONException {
		// Empty talent trees are represented as all-0 builds with no name
		if(talentData.has("spec")) {
			TalentSpec spec = new TalentSpec();
			spec.setApi(this);

			if(talentData.has("selected")) {
				spec.setSelected(talentData.getBoolean("selected"));
			} else {
				spec.setSelected(false);
			}
			JSONObject specObj = talentData.getJSONObject("spec");
			spec.setName(specObj.getString("name"));
			spec.setIcon(specObj.getString("icon"));
			spec.setRole(specObj.getString("role"));

			//			List<Glyph> majorGlyphs = new Vector<Glyph>();
			//			JSONArray majorData = talentData.getJSONObject("glyphs").getJSONArray("major");
			//			for(int i = 0; i < majorData.length(); i++) {
			//				majorGlyphs.add(createGlyph(majorData.getJSONObject(i)));
			//			}
			//			spec.setMajorGlyphs(majorGlyphs);
			//
			//			List<Glyph> minorGlyphs = new Vector<Glyph>();
			//			JSONArray minorData = talentData.getJSONObject("glyphs").getJSONArray("minor");
			//			for(int i = 0; i < minorData.length(); i++) {
			//				minorGlyphs.add(createGlyph(minorData.getJSONObject(i)));
			//			}
			//			spec.setMinorGlyphs(minorGlyphs);

			return spec;
		}
		return null;
	}
	@SuppressWarnings("unused")
	private Glyph createGlyph(JSONObject glyphData) throws JSONException {
		Glyph glyph = new Glyph();
		glyph.setApi(this);

		glyph.setGlyphId(glyphData.getInt("glyph"));
		glyph.setItemId(glyphData.getInt("item"));
		glyph.setName(glyphData.getString("name"));
		glyph.setIcon(glyphData.getString("icon"));

		return glyph;
	}

	private Item createItem(JSONObject itemData) throws JSONException {
		Item item = new Item();
		item.setApi(this);

		// TODO: add in other fields
		item.setId(itemData.getInt("id"));
		item.setName(itemData.getString("name"));
		item.setIcon(itemData.getString("icon"));

		return item;
	}

	private News createNews(JSONObject newsData) throws JSONException {
		News news = new News();
		news.setApi(this);

		if(newsData.has("character")) news.setCharacterName(newsData.getString("character"));
		if(newsData.has("itemId")) news.setItemId(newsData.getInt("itemId"));
		if(newsData.has("timestamp")) news.setTimestamp(newsData.getLong("timestamp"));
		if(newsData.has("type")) news.setType(newsData.getString("type"));
		if(newsData.has("achievement")) { news.setAchievement(createAchievement(newsData.getJSONObject("achievement"))); }

		return news;
	}
	private Achievement createAchievement(JSONObject achData) throws JSONException {
		Achievement ach = new Achievement();
		ach.setApi(this);

		ach.setDescription(achData.getString("description"));
		ach.setIcon(achData.getString("icon"));
		ach.setId(achData.getInt("id"));
		ach.setPoints(achData.getInt("points"));
		ach.setTitle(achData.getString("title"));

		return ach;
	}

	private String getBaseURL() {
		return (useSSL ? "https" : "http") + "://" + host + "/api/wow";
	}
	private String fetchURL(String urlString) throws IOException, FileNotFoundException {
		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();
		InputStream is = conn.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder result = new StringBuilder();
		String line = reader.readLine();
		while(line != null) {
			result.append(line);
			result.append("\n");
			line = reader.readLine();
		}
		reader.close();

		return result.toString();
	}
	private JSONObject fetchJSON(String urlString) throws IOException, JSONException, FileNotFoundException {
		return new JSONObject(fetchURL(urlString));
	}
	private Color createColor(String colorString) {
		return new Color(
				Integer.parseInt(colorString.substring(2, 4), 16),
				Integer.parseInt(colorString.substring(4, 6), 16),
				Integer.parseInt(colorString.substring(6, 8), 16),
				Integer.parseInt(colorString.substring(0, 2), 16)
		);
	}
}
