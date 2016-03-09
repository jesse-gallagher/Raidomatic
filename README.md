# Raidomatic

My first actual XPages app: my WoW guild's forum/raid-management app. Accordingly, it's chockablock with terrible, unnecessary ideas, but it also contains the germs of a number of ways I do things now, and a handful of interesting things of its own. It's something of an exercise in how much you can push view navigators and entry collections for relational-type lookups, with tons and tons of caching.

There are three DBs involved: "forums", the actual XPages app, "common-data", a faceless storage database for character info, and "read-marks", and even faceless-er DB for reinventing-the-wheel read marks.

It's missing a few resource files for licensing purposes, but they weren't vital for operation. The design would be severely broken if reconstituted from the ODP, though.