package com.strandls.esmodule;

/**
 * @author Abhishek Rudra
 *
 */

public class ApiConstants {

	// versioning

	public static final String V1 = "/v1";

	// paths
	// ESController
	public static final String PING = "/ping";
	public static final String SERVICES = "/services";
	public static final String DATA = "/data";
	public static final String BULK_UPLOAD = "/bulk-upload";
	public static final String BULK_UPDATE = "/bulk-update";
	public static final String TERM_SEARCH = "/term-search";
	public static final String RANGE_SEARCH = "/range-search";
	public static final String GEOHASH_AGGREGATION = "/geohash-aggregation";
	public static final String TERMS_AGGREGATION = "/terms-aggregation";
	public static final String AGGREGATION = "/aggregation";
	public static final String SEARCH = "/search";
	public static final String DOWNLOAD = "/download";
	public static final String MAPPING = "/mapping";
	public static final String ESMAPPING = "/createindex";
	public static final String INDEX_ADMIN = "/index-admin";
	public static final String RIGHTPAN = "/rightpan";
	public static final String NEARBY = "/nearby";
	public static final String AUTOSUGGEST = "/suggest";
	public static final String MATCHPHRASE = "/exactmatch";
	public static final String AutoComplete = "/auto-complete";
	public static final String GetTopUsers="/leaderboard";
	public static final String GetUserScore="/userscore";

	// binning Controller
	public static final String BINNING = "/binning";
	public static final String SQUARE = "/square";

	// geo Controller
	public static final String GEO = "/geo";
	public static final String WITHIN = "/within";

}
