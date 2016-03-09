package com.raidomatic.wowhead;

import java.io.*;

import com.sun.syndication.feed.synd.*;
import com.sun.syndication.fetcher.*;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.sun.syndication.io.FeedException;

import java.net.*;
import java.util.*;

public class News {
	List<SyndEntry> cache;
	Date lastUpdated;
	
	@SuppressWarnings("unchecked")
	public List<SyndEntry> getNews() throws IllegalArgumentException, MalformedURLException, IOException, FeedException, FetcherException {
		// Only fetch the feed at most once/hour
		if(cache == null || lastUpdated == null || (new Date().getTime() - lastUpdated.getTime() > 1000 * 60 * 60)) {
			FeedFetcher fetcher = new HttpURLFeedFetcher();
			SyndFeed feed = fetcher.retrieveFeed(new URL("http://www.wowheadnews.com/blog&rss"));
			cache = feed.getEntries();
			lastUpdated = new Date();
		}
		return cache;
	}
}
