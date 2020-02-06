package com.strandls.esmodule.services.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.GeoBoundingBoxQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoHashGrid.Bucket;
import org.elasticsearch.search.aggregations.bucket.geogrid.ParsedGeoHashGrid;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.es.ElasticSearchClient;
import com.strandls.esmodule.models.MapDocument;
import com.strandls.esmodule.models.MapResponse;
import com.strandls.esmodule.services.ElasticSearchGeoService;

/**
 * Implementation of {@link ElasticSearchGeoService}
 * 
 * @author mukund
 *
 */
public class ElasticSearchGeoServiceImpl implements ElasticSearchGeoService {

	private final Logger logger = LoggerFactory.getLogger(ElasticSearchGeoServiceImpl.class);

	@Inject
	private ElasticSearchClient client;

	private MapResponse querySearch(String index, String type, QueryBuilder query) throws IOException {

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

		sourceBuilder.query(query);
		sourceBuilder.from(0);
		sourceBuilder.size(500);

		SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.types(type);
		searchRequest.source(sourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest);
		List<MapDocument> result = new ArrayList<>();

		long totalHits = searchResponse.getHits().getTotalHits();

		for (SearchHit hit : searchResponse.getHits().getHits())
			result.add(new MapDocument(hit.getSourceAsString()));

		logger.info("Search completed with total hits: {}", totalHits);

		return new MapResponse(result, totalHits, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.strandls.naksha.es.services.api.ElasticSearchGeoService#
	 * getGeoWithinDocuments(java.lang.String, java.lang.String, java.lang.String,
	 * double, double, double, double)
	 */
	@Override
	public MapResponse getGeoWithinDocuments(String index, String type, String geoField, double top, double left,
			double bottom, double right) throws IOException {

		logger.info("Geo with search, top: {}, left: {}, bottom: {}, right: {}", top, left, bottom, right);
		GeoBoundingBoxQueryBuilder query = QueryBuilders.geoBoundingBoxQuery(geoField).setCorners(top, left, bottom,
				right);

		return querySearch(index, type, query);
	}

	@Override
	public Map<String, Long> getGeoAggregation(String index, String type, String geoField, Integer precision, Double top,
			Double left, Double bottom, Double right) throws IOException {

		logger.info("Geo with search, top: {}, left: {}, bottom: {}, right: {}", top, left, bottom, right);

		AggregationBuilder aggregationBuilder = AggregationBuilders.geohashGrid("agg").field(geoField)
				.precision(precision);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		if(top != null && left != null && bottom != null && right != null)
			searchSourceBuilder.query(QueryBuilders.geoBoundingBoxQuery(geoField).setCorners(top, left, bottom, right));

		searchSourceBuilder.aggregation(aggregationBuilder);

		SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.types(type);
		searchRequest.source(searchSourceBuilder);

		try {
			SearchResponse searchResponse = client.search(searchRequest);
			Aggregations aggregations = searchResponse.getAggregations();
			ParsedGeoHashGrid geoHashGrid = aggregations.get("agg");

			Map<String, Long> hashToCount = new HashMap<String, Long>();
			for (Bucket b : geoHashGrid.getBuckets()) {
				hashToCount.put(b.getKeyAsString(), b.getDocCount());
			}
			return hashToCount;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
