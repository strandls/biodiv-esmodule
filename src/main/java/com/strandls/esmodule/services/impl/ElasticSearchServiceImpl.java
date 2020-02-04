package com.strandls.esmodule.services.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse.ShardInfo;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoGridAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.missing.Missing;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.pipeline.ParsedSimpleValue;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregatorBuilders;
import org.elasticsearch.search.aggregations.pipeline.bucketscript.BucketScriptPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.bucketsort.BucketSortPipelineAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.strandls.es.ElasticSearchClient;
import com.strandls.esmodule.indexes.pojo.ExtendedTaxonDefinition;
import com.strandls.esmodule.models.AggregationResponse;
import com.strandls.esmodule.models.MapDocument;
import com.strandls.esmodule.models.MapQueryResponse;
import com.strandls.esmodule.models.MapQueryStatus;
import com.strandls.esmodule.models.MapResponse;
import com.strandls.esmodule.models.MapSearchParams;
import com.strandls.esmodule.models.MapSortType;
import com.strandls.esmodule.models.ObservationInfo;
import com.strandls.esmodule.models.ObservationMapInfo;
import com.strandls.esmodule.models.ObservationNearBy;
import com.strandls.esmodule.models.SimilarObservation;
import com.strandls.esmodule.models.query.MapBoolQuery;
import com.strandls.esmodule.models.query.MapRangeQuery;
import com.strandls.esmodule.models.query.MapSearchQuery;
import com.strandls.esmodule.services.ElasticSearchService;

/**
 * Implementation of {@link ElasticSearchService}
 * 
 * @author mukund
 *
 */
public class ElasticSearchServiceImpl extends ElasticSearchQueryUtil implements ElasticSearchService {

	@Inject
	private ElasticSearchClient client;

	@Inject
	private ObjectMapper objectMapper;

	private final Logger logger = LoggerFactory.getLogger(ElasticSearchServiceImpl.class);
	
	private static final Integer TotalUserUpperBound = 20000;
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.strandls.naksha.es.services.api.ElasticSearchService#create(java.lang.
	 * String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public MapQueryResponse create(String index, String type, String documentId, String document) throws IOException {

		logger.info("Trying to create index: {}, type: {} & id: {}", index, type, documentId);

		IndexRequest request = new IndexRequest(index, type, documentId);
		request.source(document, XContentType.JSON);
		// IndexResponse indexResponse = client.index(request); DEPRECATED
		IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);

		ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();

		StringBuilder failureReason = new StringBuilder();

		if (shardInfo.getFailed() > 0) {

			for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
				failureReason.append(failure.reason());
				failureReason.append(";");
			}
		}

		MapQueryStatus queryStatus = MapQueryStatus.valueOf(indexResponse.getResult().name());

		logger.info("Created index: {}, type: {} & id: {} with status {}", index, type, documentId, queryStatus);

		return new MapQueryResponse(queryStatus, failureReason.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.strandls.naksha.es.services.api.ElasticSearchService#fetch(java.lang.
	 * String, java.lang.String, java.lang.String)
	 */
	@Override
	public MapDocument fetch(String index, String type, String documentId) throws IOException {

		logger.info("Trying to fetch index: {}, type: {} & id: {}", index, type, documentId);

		GetRequest request = new GetRequest(index, type, documentId);
		// GetResponse response = client.get(request); DEPRECATED
		GetResponse response = client.get(request, RequestOptions.DEFAULT);

		logger.info("Fetched index: {}, type: {} & id: {} with status {}", index, type, documentId,
				response.isExists());

		return new MapDocument(response.getSourceAsString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.strandls.naksha.es.services.api.ElasticSearchService#update(java.lang.
	 * String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public MapQueryResponse update(String index, String type, String documentId, Map<String, Object> document)
			throws IOException {

		logger.info("Trying to update index: {}, type: {} & id: {}", index, type, documentId);

		UpdateRequest request = new UpdateRequest(index, type, documentId);
		request.doc(document);
		// UpdateResponse updateResponse = client.update(request); DEPRECATED
		UpdateResponse updateResponse = client.update(request, RequestOptions.DEFAULT);
		ShardInfo shardInfo = updateResponse.getShardInfo();

		String failureReason = "";

		if (shardInfo.getFailed() > 0) {
			for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
				failureReason = failure.reason() + ";";
			}
		}

		MapQueryStatus queryStatus = MapQueryStatus.valueOf(updateResponse.getResult().name());

		logger.info("Updated index: {}, type: {} & id: {} with status {}", index, type, documentId, queryStatus);

		return new MapQueryResponse(queryStatus, failureReason);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.strandls.naksha.es.services.api.ElasticSearchService#delete(java.lang.
	 * String, java.lang.String, java.lang.String)
	 */
	@Override
	public MapQueryResponse delete(String index, String type, String documentId) throws IOException {

		logger.info("Trying to delete index: {}, type: {} & id: {}", index, type, documentId);

		DeleteRequest request = new DeleteRequest(index, type, documentId);
		// DeleteResponse deleteResponse = client.delete(request);
		DeleteResponse deleteResponse = client.delete(request, RequestOptions.DEFAULT);
		ReplicationResponse.ShardInfo shardInfo = deleteResponse.getShardInfo();

		String failureReason = "";

		if (shardInfo.getFailed() > 0) {

			for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
				failureReason = failure.reason() + ";";
			}
		}

		MapQueryStatus queryStatus = MapQueryStatus.valueOf(deleteResponse.getResult().name());

		logger.info("Deleted index: {}, type: {} & id: {} with status {}", index, type, documentId, queryStatus);

		return new MapQueryResponse(queryStatus, failureReason);
	}

	private JsonNode[] parseJson(String jsonArray, List<MapQueryResponse> responses) throws IOException {
		ObjectMapper mapper = new ObjectMapper();

		JsonNode[] jsons = null;
		try {
			jsons = mapper.readValue(jsonArray, JsonNode[].class);
		} catch (JsonParseException e) {
			responses.add(new MapQueryResponse(MapQueryStatus.JSON_EXCEPTION, "Json Parsing Exception"));
		} catch (JsonMappingException e) {
			responses.add(new MapQueryResponse(MapQueryStatus.JSON_EXCEPTION, "Json Mapping Exception"));
		}

		if (jsons != null && !jsons[0].has("id")) {
			responses.add(new MapQueryResponse(MapQueryStatus.NO_ID, "No id field specified"));
		}

		return jsons;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.strandls.naksha.es.services.api.ElasticSearchService#bulkUpload(java.lang
	 * .String, java.lang.String, java.lang.String)
	 */
	@Override
	public List<MapQueryResponse> bulkUpload(String index, String type, String jsonArray) throws IOException {
		List<MapQueryResponse> responses = new ArrayList<>();

		logger.info("Trying to bulk upload index: {}, type: {}", index, type);

		JsonNode[] jsons = parseJson(jsonArray, responses);
		if (!responses.isEmpty()) {
			logger.error("Json exception-{}, while trying to bulk upload for index:{}, type: {}",
					responses.get(0).getMessage(), index, type);
			return responses;
		}

		BulkRequest request = new BulkRequest();

		for (JsonNode json : jsons)
			request.add(
					new IndexRequest(index, type, json.get("id").asText()).source(json.toString(), XContentType.JSON));

		// BulkResponse bulkResponse = client.bulk(request);
		BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);
		for (BulkItemResponse bulkItemResponse : bulkResponse) {

			StringBuilder failureReason = new StringBuilder();
			MapQueryStatus queryStatus;

			if (bulkItemResponse.isFailed()) {
				failureReason.append(bulkItemResponse.getFailureMessage());
				queryStatus = MapQueryStatus.ERROR;
			} else {
				IndexResponse indexResponse = bulkItemResponse.getResponse();
				ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();

				if (shardInfo.getFailed() > 0) {

					for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
						failureReason.append(failure.reason());
						failureReason.append(";");
					}
				}

				queryStatus = MapQueryStatus.valueOf(indexResponse.getResult().name());
			}

			logger.info(" For index: {}, type: {}, bulk upload id: {}, the status is {}", index, type,
					bulkItemResponse.getId(), queryStatus);

			responses.add(new MapQueryResponse(queryStatus, failureReason.toString()));
		}

		return responses;
	}

	@Override
	public List<MapQueryResponse> bulkUpdate(String index, String type, List<Map<String, Object>> updateDocs)
			throws IOException {

		logger.info("Trying to bulk update index: {}, type: {}", index, type);

		BulkRequest request = new BulkRequest();

		for (Map<String, Object> doc : updateDocs)
			request.add(new UpdateRequest(index, type, doc.get("id").toString()).doc(doc));

		BulkResponse bulkResponse = client.bulk(request);

		List<MapQueryResponse> responses = new ArrayList<>();

		for (BulkItemResponse bulkItemResponse : bulkResponse) {

			StringBuilder failureReason = new StringBuilder();
			MapQueryStatus queryStatus;

			if (bulkItemResponse.isFailed()) {
				failureReason.append(bulkItemResponse.getFailureMessage());
				queryStatus = MapQueryStatus.ERROR;
			} else {
				UpdateResponse updateResponse = bulkItemResponse.getResponse();
				ReplicationResponse.ShardInfo shardInfo = updateResponse.getShardInfo();

				if (shardInfo.getFailed() > 0) {

					for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
						failureReason.append(failure.reason());
						failureReason.append(";");
					}
				}

				queryStatus = MapQueryStatus.valueOf(updateResponse.getResult().name());
			}

			logger.info(" For index: {}, type: {}, bulk update id: {}, the status is {}", index, type,
					bulkItemResponse.getId(), queryStatus);

			responses.add(new MapQueryResponse(queryStatus, failureReason.toString()));
		}

		return responses;

	}

	private MapResponse querySearch(String index, String type, QueryBuilder query, MapSearchParams searchParams,
			String geoAggregationField, Integer geoAggegationPrecision) throws IOException {

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		if (query != null)
			sourceBuilder.query(query);
		if (searchParams.getFrom() != null)
			sourceBuilder.from(searchParams.getFrom());
		if (searchParams.getLimit() != null)
			sourceBuilder.size(searchParams.getLimit());

		if (searchParams.getSortOn() != null) {
			SortOrder sortOrder = searchParams.getSortType() != null && MapSortType.ASC == searchParams.getSortType()
					? SortOrder.ASC
					: SortOrder.DESC;
			sourceBuilder.sort(searchParams.getSortOn(), sortOrder);
		}

		if (geoAggregationField != null) {
			geoAggegationPrecision = geoAggegationPrecision != null ? geoAggegationPrecision : 1;
			sourceBuilder.aggregation(getGeoGridAggregationBuilder(geoAggregationField, geoAggegationPrecision));
		}

		SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.types(type);
		searchRequest.source(sourceBuilder);
		// SearchResponse searchResponse = client.search(searchRequest);
		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

		List<MapDocument> result = new ArrayList<>();

		long totalHits = searchResponse.getHits().getTotalHits();

		for (SearchHit hit : searchResponse.getHits().getHits())
			result.add(new MapDocument(hit.getSourceAsString()));

		logger.info("Search completed with total hits: {}", totalHits);

		String aggregationString = null;
		if (geoAggregationField != null) {
			Aggregation aggregation = searchResponse.getAggregations().asList().get(0);
			XContentBuilder builder = XContentFactory.jsonBuilder();
			builder.startObject();
			aggregation.toXContent(builder, ToXContent.EMPTY_PARAMS);
			builder.endObject();
			String result2 = Strings.toString(builder);
			aggregationString = result2;
//			aggregationString = XContentHelper.convertToJson(builder, reformatJson)
			logger.info("Aggregation search: {} completed", aggregation.getName());

		}

		return new MapResponse(result, totalHits, aggregationString);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.strandls.naksha.es.services.api.ElasticSearchService#termSearch(java.lang
	 * .String, java.lang.String, java.lang.String, java.lang.String,
	 * com.strandls.naksha.es.models.MapSearchParams, java.lang.String,
	 * java.lang.Integer)
	 */
	@Override
	public MapResponse termSearch(String index, String type, String key, String value, MapSearchParams searchParams,
			String geoAggregationField, Integer geoAggegationPrecision) throws IOException {

		logger.info("Term search for index: {}, type: {}, key: {}, value: {}", index, type, key, value);
		QueryBuilder query;
		if (value != null)
			query = QueryBuilders.termQuery(key, value);
		else
			query = QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery(key));

		return querySearch(index, type, query, searchParams, geoAggregationField, geoAggegationPrecision);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.strandls.naksha.es.services.api.ElasticSearchService#boolSearch(java.lang
	 * .String, java.lang.String, java.util.List,
	 * com.strandls.naksha.es.models.MapSearchParams, java.lang.String,
	 * java.lang.Integer)
	 */
	@Override
	public MapResponse boolSearch(String index, String type, List<MapBoolQuery> queries, MapSearchParams searchParams,
			String geoAggregationField, Integer geoAggegationPrecision) throws IOException {

		logger.info("Bool search for index: {}, type: {}", index, type);
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		for (MapBoolQuery query : queries) {
			if (query.getValues() != null)
				boolQuery.must(QueryBuilders.termsQuery(query.getKey(), query.getValues()));
			else
				boolQuery.mustNot(QueryBuilders.existsQuery(query.getKey()));
		}

		return querySearch(index, type, boolQuery, searchParams, geoAggregationField, geoAggegationPrecision);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.strandls.naksha.es.services.api.ElasticSearchService#rangeSearch(java.
	 * lang.String, java.lang.String, java.util.List,
	 * com.strandls.naksha.es.models.MapSearchParams, java.lang.String,
	 * java.lang.Integer)
	 */
	@Override
	public MapResponse rangeSearch(String index, String type, List<MapRangeQuery> queries, MapSearchParams searchParams,
			String geoAggregationField, Integer geoAggegationPrecision) throws IOException {

		logger.info("Range search for index: {}, type: {}", index, type);
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		for (MapRangeQuery query : queries) {
			boolQuery.must(QueryBuilders.rangeQuery(query.getKey()).from(query.getStart()).to(query.getEnd()));
		}

		return querySearch(index, type, boolQuery, searchParams, geoAggregationField, geoAggegationPrecision);
	}

	@Override
	public AggregationResponse aggregation(String index, String type, MapSearchQuery searchQuery,
			String geoAggregationField, String filter) throws IOException {

		logger.info("SEARCH for index: {}, type: {}", index, type);

		MapSearchParams searchParams = searchQuery.getSearchParams();
		BoolQueryBuilder masterBoolQuery = getBoolQueryBuilder(searchQuery);

		// logger.info(masterBoolQuery.toString());

		applyMapBounds(searchParams, masterBoolQuery, geoAggregationField);

		AggregationBuilder aggregation = AggregationBuilders.terms(filter).field(filter).size(1000);
		AggregationResponse aggregationResponse = new AggregationResponse();

		if (filter.equals("name") || filter.equals("status")) {
			AggregationResponse temp = null;
			aggregation = AggregationBuilders.filter("available", QueryBuilders.existsQuery(filter));
			temp = groupAggregation(index, type, aggregation, masterBoolQuery, filter);
			HashMap<Object, Long> t = new HashMap<Object, Long>();
			for (Map.Entry<Object, Long> entry : temp.getGroupAggregation().entrySet()) {
				t.put(entry.getKey(), entry.getValue());
			}
			aggregation = AggregationBuilders.missing("miss").field(filter.concat(".keyword"));
			temp = groupAggregation(index, type, aggregation, masterBoolQuery, filter);
			for (Map.Entry<Object, Long> entry : temp.getGroupAggregation().entrySet()) {
				t.put(entry.getKey(), entry.getValue());
			}
			aggregationResponse.setGroupAggregation(t);
		} else {
			aggregationResponse = groupAggregation(index, type, aggregation, masterBoolQuery, filter);

		}
		return aggregationResponse;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.strandls.naksha.es.services.api.ElasticSearchService#search(java.lang.
	 * String, java.lang.String, com.strandls.naksha.es.models.query.MapSearchQuery,
	 * java.lang.String, java.lang.Integer, java.lang.Boolean, java.lang.String)
	 */
	@Override
	public MapResponse search(String index, String type, MapSearchQuery searchQuery, String geoAggregationField,
			Integer geoAggegationPrecision, Boolean onlyFilteredAggregation, String termsAggregationField)
			throws IOException {

		logger.info("SEARCH for index: {}, type: {}", index, type);

		MapSearchParams searchParams = searchQuery.getSearchParams();
		BoolQueryBuilder masterBoolQuery = getBoolQueryBuilder(searchQuery);

		GeoGridAggregationBuilder geoGridAggregationBuilder = getGeoGridAggregationBuilder(geoAggregationField,
				geoAggegationPrecision);
		MapDocument aggregateSearch = aggregateSearch(index, type, geoGridAggregationBuilder, masterBoolQuery);
		String geohashAggregation = null;
		if (aggregateSearch != null)
			geohashAggregation = aggregateSearch.getDocument().toString();

		String termsAggregation = null;
		if (termsAggregationField != null) {
			termsAggregation = termsAggregation(index, type, termsAggregationField, null, null, geoAggregationField,
					searchQuery).getDocument().toString();
		}

		if (onlyFilteredAggregation != null && onlyFilteredAggregation) {
			applyMapBounds(searchParams, masterBoolQuery, geoAggregationField);
			aggregateSearch = aggregateSearch(index, type, geoGridAggregationBuilder, masterBoolQuery);
			if (aggregateSearch != null)
				geohashAggregation = aggregateSearch.getDocument().toString();
			return new MapResponse(new ArrayList<>(), 0, geohashAggregation, geohashAggregation, termsAggregation);
		}

		applyMapBounds(searchParams, masterBoolQuery, geoAggregationField);
		MapResponse mapResponse = querySearch(index, type, masterBoolQuery, searchParams, geoAggregationField,
				geoAggegationPrecision);
		mapResponse.setViewFilteredGeohashAggregation(mapResponse.getGeohashAggregation());
		mapResponse.setGeohashAggregation(geohashAggregation);
		mapResponse.setTermsAggregation(termsAggregation);
		
		return mapResponse;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.strandls.naksha.es.services.api.ElasticSearchService#geohashAggregation(
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.Integer)
	 */
	@Override
	public MapDocument geohashAggregation(String index, String type, String field, Integer precision)
			throws IOException {

		logger.info("GeoHash aggregation for index: {}, type: {} on field: {} with precision: {}", index, type, field,
				precision);

		return aggregateSearch(index, type, getGeoGridAggregationBuilder(field, precision), null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.strandls.naksha.es.services.api.ElasticSearchService#termsAggregation(
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.Integer, java.lang.String,
	 * com.strandls.naksha.es.models.query.MapSearchQuery)
	 */
	@Override
	public MapDocument termsAggregation(String index, String type, String field, String subField, Integer size,
			String locationField, MapSearchQuery query) throws IOException {

		if (size == null)
			size = 500;

		logger.info("Terms aggregation for index: {}, type: {} on field: {} and sub field: {} with size: {}", index,
				type, field, subField, size);

		BoolQueryBuilder boolQuery = getBoolQueryBuilder(query);
		if (query.getSearchParams() != null) {
			applyMapBounds(query.getSearchParams(), boolQuery, locationField);
		}

		return aggregateSearch(index, type, getTermsAggregationBuilder(field, subField, size), boolQuery);
	}

	private MapDocument aggregateSearch(String index, String type, AggregationBuilder aggQuery, QueryBuilder query)
			throws IOException {

		if (aggQuery == null)
			return null;

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

		if (query != null)
			sourceBuilder.query(query);
		sourceBuilder.aggregation(aggQuery);

		SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.types(type);
		searchRequest.source(sourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

		Aggregation aggregation = searchResponse.getAggregations().asList().get(0);
		XContentBuilder builder = XContentFactory.jsonBuilder();
		builder.startObject();
		aggregation.toXContent(builder, ToXContent.EMPTY_PARAMS);
//		String result2 = Strings.toString(builder);
		builder.endObject();
		String result2 = Strings.toString(builder);
		logger.info("Aggregation search: {} completed", aggregation.getName());

		// return new MapDocument(XContentHelper.toString(aggregation));
		return new MapDocument(result2);

	}

	private AggregationResponse groupAggregation(String index, String type, AggregationBuilder aggQuery,
			QueryBuilder query, String filter) throws IOException {

		if (aggQuery == null)
			return null;

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		if (query != null)
			sourceBuilder.query(query);
		sourceBuilder.aggregation(aggQuery);

		SearchRequest request = new SearchRequest(index);
		request.types(type);
		request.source(sourceBuilder);
		SearchResponse response = client.search(request,RequestOptions.DEFAULT);

		HashMap<Object, Long> groupMonth = new HashMap<Object, Long>();

		if (filter.equals("name") || filter.equals("status")) {
			Filter filterAgg = response.getAggregations().get("available");
			if (filterAgg != null) {
				groupMonth.put("available", filterAgg.getDocCount());
			}
			Missing missingAgg = response.getAggregations().get("miss");
			if (missingAgg != null) {
				groupMonth.put("missing", missingAgg.getDocCount());
			}

		} else {
			Terms frommonth = response.getAggregations().get(filter);

			for (Terms.Bucket entry : frommonth.getBuckets()) {
				groupMonth.put(entry.getKey(), entry.getDocCount());
			}
		}

		return new AggregationResponse(groupMonth);
	}

	@Override
	public ObservationInfo getObservationRightPan(String index, String type, String maxVotedRecoId) throws IOException {

		MatchPhraseQueryBuilder masterBoolQuery = getBoolQueryBuilderObservationPan(maxVotedRecoId);
		AggregationBuilder aggregation = AggregationBuilders.terms("frommonth").field("frommonth").size(1000);

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.query(masterBoolQuery);
		sourceBuilder.aggregation(aggregation);
		sourceBuilder.size(1000);
		String[] includes = { "id", "thumbnail", "name", "latitude", "longitude" };
		sourceBuilder.fetchSource(includes, null);

		SearchRequest request = new SearchRequest(index);
		request.types(type);
		request.source(sourceBuilder);

		SearchResponse response = client.search(request,RequestOptions.DEFAULT);

		List<SimilarObservation> similarObservation = new ArrayList<SimilarObservation>();
		List<ObservationMapInfo> latlon = new ArrayList<ObservationMapInfo>();

		for (SearchHit hit : response.getHits().getHits()) {

			latlon.add(new ObservationMapInfo(Long.parseLong(hit.getSourceAsMap().get("id").toString()),
					String.valueOf(hit.getSourceAsMap().get("name")),
					Double.parseDouble(hit.getSourceAsMap().get("latitude").toString()),
					Double.parseDouble(hit.getSourceAsMap().get("longitude").toString())));
			similarObservation.add(new SimilarObservation(Long.parseLong(hit.getSourceAsMap().get("id").toString()),
					String.valueOf(hit.getSourceAsMap().get("name")),
					String.valueOf(hit.getSourceAsMap().get("thumbnail"))));
		}
		HashMap<Object, Long> groupMonth = new HashMap<Object, Long>();
		Terms frommonth = response.getAggregations().get("frommonth");
		for (Terms.Bucket entry : frommonth.getBuckets()) {
			groupMonth.put(entry.getKey(), entry.getDocCount());
		}

		return new ObservationInfo(groupMonth, similarObservation, latlon);
	}

	@Override
	public List<ObservationNearBy> observationNearBy(String index, String type, Double lat, Double Lon)
			throws IOException {

		BoolQueryBuilder geoDistanceQuery = getGeoDistance(lat, Lon);

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.query(geoDistanceQuery);
		sourceBuilder.size(15);
		String[] includes = { "id", "thumbnail", "name", "latitude", "longitude" };
		sourceBuilder.fetchSource(includes, null);

		SearchRequest request = new SearchRequest(index);
		request.types(type);
		request.source(sourceBuilder);

		SearchResponse response = client.search(request,RequestOptions.DEFAULT);

		List<ObservationNearBy> nearBy = new ArrayList<ObservationNearBy>();

		Double distance = 0.0, lat2 = 0.0, lon2 = 0.0;
		for (SearchHit hit : response.getHits().getHits()) {

			lat2 = Double.parseDouble(hit.getSourceAsMap().get("latitude").toString());
			lon2 = Double.parseDouble(hit.getSourceAsMap().get("longitude").toString());
			distance = distanceCalculate(lat, Lon, lat2, lon2);

			nearBy.add(new ObservationNearBy(Long.parseLong(hit.getSourceAsMap().get("id").toString()),
					String.valueOf(hit.getSourceAsMap().get("name")),
					String.valueOf(hit.getSourceAsMap().get("thumbnail")), distance));

		}

		Collections.sort(nearBy, (obv1, obv2) -> obv1.getDistance().compareTo(obv2.getDistance()));

		return nearBy;
	}

	public Double distanceCalculate(Double lat1, Double lon1, Double lat2, Double lon2) {
		Double dist = 0.0;
		if ((lat1 == lat2) && (lon1 == lon2)) {
			return dist;
		} else {
			double theta = lon1 - lon2;
			dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
					+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
			dist = Math.acos(dist);
			dist = Math.toDegrees(dist);
			dist = dist * 60 * 1.1515; // distance in miles
			dist = dist * 1.609344; // distnace in KM
		}
		return (dist);
	}

	@Override
	public <T> List<T> autoCompletion(String index, String type, String field, String text, Class<T> classMapped) {
		// the completion method works for the mapping where edgeNGram is used
		logger.info("inside auto completion method");

		if (field.equals("common_name")) {
			field = "common_names.name";
		} else if (field.equals("scientific_name")) {
			field = "name";
		}

		List<T> matchedResults = new ArrayList<T>();
		QueryBuilder query = QueryBuilders.matchPhraseQuery(field, text);
		SearchRequest searchRequest = new SearchRequest(index);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.size(100);
		searchSourceBuilder.fetchSource(null, new String[] { "@timestamp", "@version" });
		SearchResponse searchResponse = null;
		try {
			searchSourceBuilder.query(query);
			searchRequest.types(type);
			searchRequest.source(searchSourceBuilder);
			searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		for (SearchHit hit : searchResponse.getHits().getHits()) {
			try {
				matchedResults.add(objectMapper.readValue(hit.getSourceAsString(), classMapped));
			} catch (Exception e) {
				logger.error(e.getMessage());
			}

		}
		return matchedResults;
	}

	@Override
	public <T> List<T> autoCompletion(String index, String type, String field, String text, String filterField,
			Integer filter, Class<T> classMapped) {

		if (field.equals("common_name")) {
			field = "common_names.name";
		} else if (field.equals("scientific_name")) {
			field = "name";
		}
		List<T> matchedResults = new ArrayList<T>();
		QueryBuilder query = QueryBuilders.boolQuery().must(QueryBuilders.matchPhraseQuery(field, text))
				.filter(QueryBuilders.termQuery(filterField, filter));
		SearchRequest searchRequest = new SearchRequest(index);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.size(10000);
		searchSourceBuilder.fetchSource(null, new String[] { "@timestamp", "@version" });
		SearchResponse searchResponse = null;
		try {
			searchSourceBuilder.query(query);
			searchRequest.types(type);
			searchRequest.source(searchSourceBuilder);
			// searchResponse = client.search(searchRequest);
			searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		for (SearchHit hit : searchResponse.getHits().getHits()) {
			try {
				matchedResults.add(objectMapper.readValue(hit.getSourceAsString(), classMapped));
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
		return matchedResults;
	}

	@Override
	public List<ExtendedTaxonDefinition> matchPhrase(String index, String type, String scientificName,
			String scientificText, String canonicalName, String canonicalText) {

		String scientificFieldName = "name.raw";
		String canonicalFieldName = "canonical_form";

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		boolQueryBuilder.must(QueryBuilders.matchQuery(scientificFieldName, scientificText).operator(Operator.AND));
		boolQueryBuilder.must(QueryBuilders.matchPhraseQuery(canonicalFieldName, canonicalText));

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.fetchSource(null, new String[] { "@timestamp", "@version" });
		searchSourceBuilder.size(10000);

		SearchResponse searchResponse = null;
		SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.types(type);
		try {
			searchSourceBuilder.query(boolQueryBuilder);
			searchRequest.source(searchSourceBuilder);
//			searchResponse = client.search(searchRequest); DEPRECATED
			searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		if (searchResponse.getHits().getTotalHits() == 0) {
			MatchPhraseQueryBuilder matchPhraseQueryBuilder = QueryBuilders.matchPhraseQuery(canonicalFieldName,
					canonicalText);
			try {
				searchSourceBuilder.query(matchPhraseQueryBuilder);
				searchRequest.source(searchSourceBuilder);
				searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}

		if (searchResponse.getHits().getTotalHits() == 0)
			return null;

		return processElasticResponse(searchResponse);

	}
	
	@Override
	public List<LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, String>>>> getUserScore(String index,
			String type, Integer authorId) {
		AggregationBuilder aggs = buildSortingAggregation(1, null);
		QueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.filter(QueryBuilders.termQuery("author_id", authorId));
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		aggs.subAggregation(populateDataAggregation());
		aggs.subAggregation(termsAggregation("profile_pic", "profile_pic.keyword"));
		aggs.subAggregation(termsAggregation("author_name", "name.keyword"));
		searchSourceBuilder.aggregation(aggs);
		searchSourceBuilder.query(queryBuilder);
		searchSourceBuilder.size(0);
		SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.types(type);
		SearchResponse searchResponse = null;
		searchRequest.source(searchSourceBuilder);
		try {
			searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
		return processAggregationResponse(searchResponse);
	}

	@Override
	public List<LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, String>>>> getTopUsers
	(String index,String type, String sortingValue, Integer topUser) {
		
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		AggregationBuilder aggs = buildSortingAggregation(topUser, sortingValue);
		searchSourceBuilder.aggregation(aggs);
		searchSourceBuilder.size(0);
		SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.types(type);
		searchRequest.source(searchSourceBuilder);
		
		SearchResponse searchResponse = null;
		try {
			searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		//processAggregationResponse(searchRespone
		List<Integer> topUserIds = getUserIds(searchResponse);
		QueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.filter(QueryBuilders.termsQuery("author_id", topUserIds));
		searchSourceBuilder = new SearchSourceBuilder();
		aggs.subAggregation(populateDataAggregation());
		aggs.subAggregation(termsAggregation("profile_pic", "profile_pic.keyword"));
		aggs.subAggregation(termsAggregation("author_name", "name.keyword"));
		searchSourceBuilder.aggregation(aggs);
		searchSourceBuilder.query(queryBuilder);
		searchSourceBuilder.size(0);
		searchRequest.source(searchSourceBuilder);
		
		try {
			searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			logger.info(e.getMessage());
		}
		return processAggregationResponse(searchResponse);
	}

	private AggregationBuilder buildSortingAggregation(Integer topUser, String sortingValue) {
		String sortingField = null;
		AggregationBuilder aggs = termsAggregation("group_by_author", "author_id",TotalUserUpperBound);
		aggs.subAggregation(
				filterAggregation("group_by_score_category_participate", "score_category.keyword", 
						"Participation"));
		aggs.subAggregation(filterAggregation("group_by_score_category_content", "score_category.keyword", 
				"Content"));
		if (sortingValue != null) {
			if(sortingValue.contains(".")) {
				sortingField = "module_activity_category.keyword";
			}
			else {
				sortingField = "module.keyword";
			}
			aggs.subAggregation(filterAggregation("group_by_module", sortingField, sortingValue));
		}
		
		aggs.subAggregation(getBucketScriptAggregation());
		aggs.subAggregation(getBucketSortAggregation(sortingValue, topUser));
		return aggs;
	}
	
	private AggregationBuilder populateDataAggregation()
	{
			
			AggregationBuilder aggs = termsAggregation("bucket_by_module", "module.keyword")
					.subAggregation(termsAggregation("bucket_by_activity_category", 
							"activity_category.keyword"));
			return aggs;
	}
	
	private AggregationBuilder termsAggregation(String aggregationName, String field, Integer totalBucket) {
		// List<BucketOrder> order = new ArrayList<BucketOrder>();
		AggregationBuilder aggs = AggregationBuilders.terms(aggregationName).field(field).size(totalBucket);
		return aggs;
	}

	private AggregationBuilder termsAggregation(String aggregationName, String field) {
		
		AggregationBuilder aggs = AggregationBuilders.terms(aggregationName)
				.field(field).size(100);
		return aggs;
	}
	

	private AggregationBuilder filterAggregation(String aggregationName, String field, String fieldValue) {

		AggregationBuilder aggs = AggregationBuilders.filter(aggregationName,
				QueryBuilders.termQuery(field, fieldValue));

		return aggs;
	}
	
	private BucketScriptPipelineAggregationBuilder getBucketScriptAggregation() {
		Map<String, String> bucketsPathsMap = new HashMap<>();
		bucketsPathsMap.put("participate", "group_by_score_category_participate>_count");
		bucketsPathsMap.put("content", "group_by_score_category_content>_count");
		Script script = new Script("Math.round(10*(Math.log10(params.content)+Math.log10(params.participate)))");

		BucketScriptPipelineAggregationBuilder bucketScript = 
				PipelineAggregatorBuilders.bucketScript("activity_score",bucketsPathsMap, script);
		return bucketScript;
	}

	private BucketSortPipelineAggregationBuilder getBucketSortAggregation(String sortingValue, int topUsers) {
		List<FieldSortBuilder> sortingCriteriaList = new ArrayList<FieldSortBuilder>();
		String sortOnAggregation = null;
		if (sortingValue == null) {
			sortOnAggregation = "activity_score";
		}
		else {
			sortOnAggregation = "group_by_module"+">_count";
		}
		
		FieldSortBuilder sortOn = new FieldSortBuilder(sortOnAggregation).order(SortOrder.DESC);
		sortingCriteriaList.add(sortOn);
		
		BucketSortPipelineAggregationBuilder bucketSort = PipelineAggregatorBuilders
				.bucketSort("bucket_sorting", sortingCriteriaList).size(topUsers);

		return bucketSort;
	}

	private List<ExtendedTaxonDefinition> processElasticResponse(SearchResponse searchResponse) {
		List<ExtendedTaxonDefinition> matchedResults = new ArrayList<ExtendedTaxonDefinition>();
		for (SearchHit hit : searchResponse.getHits().getHits()) {
			try {
				matchedResults.add(objectMapper.readValue(hit.getSourceAsString(), ExtendedTaxonDefinition.class));
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
		return matchedResults;
	}

	private List<Integer> getUserIds(SearchResponse searchResponse) {
		Terms authorTerms = searchResponse.getAggregations().get("group_by_author");
		Collection<? extends Bucket> authorBuckets = authorTerms.getBuckets();
		List<Integer> topAuthors = new ArrayList<Integer>();
		for(Bucket authorBucket : authorBuckets) {
			topAuthors.add(authorBucket.getKeyAsNumber().intValue());
		}
		return topAuthors;

	}

	private List<LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, String>>>> processAggregationResponse(
			SearchResponse searchResponse) {
		List<LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, String>>>> records = new ArrayList<LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, String>>>>();
		
		Terms authorTerms = searchResponse.getAggregations().get("group_by_author");
		Collection<? extends Bucket> authorBuckets = authorTerms.getBuckets();

		for (Bucket authorBucket : authorBuckets) {
			LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, String>>> userRecord = new LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, String>>>();
			
			Terms moduleTerms = authorBucket.getAggregations().get("bucket_by_module");
			Collection<? extends Bucket> moduleBuckets = moduleTerms.getBuckets();
			LinkedHashMap<String, LinkedHashMap<String, String>> moduleRecords = new LinkedHashMap<String, LinkedHashMap<String, String>>();

			for (Bucket moduleBucket : moduleBuckets) {
				Terms activityTerms = moduleBucket.getAggregations().get("bucket_by_activity_category");
				Collection<? extends Bucket> activityBuckets = activityTerms.getBuckets();
				LinkedHashMap<String, String> activities = new LinkedHashMap<String, String>();

				for (Bucket activityBucket : activityBuckets) {
					activities.put(activityBucket.getKeyAsString(), String.valueOf(activityBucket.getDocCount()));
				}
				moduleRecords.put(moduleBucket.getKeyAsString(), activities);
			}
			LinkedHashMap<String, String> userDetails = new LinkedHashMap<String, String>();
			
			Terms detailTerms = authorBucket.getAggregations().get("author_name");
			for(Bucket bucket : detailTerms.getBuckets()) {
				userDetails.put("authorName", bucket.getKeyAsString());
			}
			
			detailTerms = authorBucket.getAggregations().get("profile_pic");
			for(Bucket bucket : detailTerms.getBuckets()) {
				userDetails.put("profilePic", bucket.getKeyAsString());
			}
			ParsedSimpleValue activityScoreTerms = authorBucket.getAggregations().get("activity_score");
			userDetails.put(activityScoreTerms.getName(), activityScoreTerms.getValueAsString());
			
			moduleRecords.put("details", userDetails);
			userRecord.put(authorBucket.getKeyAsString(), moduleRecords);
			records.add(userRecord);
		}
		return records;
	}
	
}
