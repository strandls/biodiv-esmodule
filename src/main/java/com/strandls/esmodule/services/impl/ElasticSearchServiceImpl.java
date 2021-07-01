package com.strandls.esmodule.services.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse.ShardInfo;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoValidationMethod;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.PipelineAggregatorBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoGrid;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoGridAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.geogrid.ParsedGeoHashGrid;
import org.elasticsearch.search.aggregations.bucket.missing.Missing;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.pipeline.BucketScriptPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.BucketSortPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.ParsedSimpleValue;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.Suggest.Suggestion;
import org.elasticsearch.search.suggest.Suggest.Suggestion.Entry;
import org.elasticsearch.search.suggest.Suggest.Suggestion.Entry.Option;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.strandls.es.ElasticSearchClient;
import com.strandls.esmodule.Constants;
import com.strandls.esmodule.indexes.pojo.ExtendedTaxonDefinition;
import com.strandls.esmodule.models.AggregationResponse;
import com.strandls.esmodule.models.AuthorUploadedObservationInfo;
import com.strandls.esmodule.models.CustomFieldValues;
import com.strandls.esmodule.models.CustomFields;
import com.strandls.esmodule.models.FilterPanelData;
import com.strandls.esmodule.models.GeoHashAggregationData;
import com.strandls.esmodule.models.IdentifiersInfo;
import com.strandls.esmodule.models.Location;
import com.strandls.esmodule.models.MapDocument;
import com.strandls.esmodule.models.MapQueryResponse;
import com.strandls.esmodule.models.MapQueryStatus;
import com.strandls.esmodule.models.MapResponse;
import com.strandls.esmodule.models.MapSearchParams;
import com.strandls.esmodule.models.MapSortType;
import com.strandls.esmodule.models.MaxVotedReco;
import com.strandls.esmodule.models.MaxVotedRecoFreq;
import com.strandls.esmodule.models.ObservationInfo;
import com.strandls.esmodule.models.ObservationLatLon;
import com.strandls.esmodule.models.ObservationMapInfo;
import com.strandls.esmodule.models.ObservationNearBy;
import com.strandls.esmodule.models.SimilarObservation;
import com.strandls.esmodule.models.SpeciesGroup;
import com.strandls.esmodule.models.TraitValue;
import com.strandls.esmodule.models.Traits;
import com.strandls.esmodule.models.UploadersInfo;
import com.strandls.esmodule.models.UserGroup;
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

	private static final Integer TOTAL_USER_UPPER_BOUND = 20000;

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

		IndexRequest request = new IndexRequest(index);
		request.id(documentId);
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

		GetRequest request = new GetRequest(index, documentId);
		GetResponse response = client.get(request, RequestOptions.DEFAULT);

		logger.info("Fetched index: {}, type: {} & id: {} with status {}", index, type, documentId,
				response.isExists());

		return new MapDocument(response.getSourceAsString());
	}
	
	public List<MapDocument> bulkFetch(String index, String type, List<String> documentIds) {
		List<MapDocument>responses=new ArrayList<>();
		MultiGetRequest request = new MultiGetRequest();
		for(String docId:documentIds) {
			request.add(new MultiGetRequest.Item(index,docId)); 
		}
		MultiGetResponse response;
		try {
			response = client.mget(request, RequestOptions.DEFAULT);
			for(MultiGetItemResponse i:response.getResponses()) {
				responses.add(new MapDocument(i.getResponse().getSourceAsString()));
			}
			return responses;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//MultiGetItemResponse item = response.getResponses();
		return null;
		
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

		UpdateRequest request = new UpdateRequest(index, documentId);
		request.doc(document);
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

		DeleteRequest request = new DeleteRequest(index, documentId);
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

		for (JsonNode json : jsons) {
			IndexRequest ir = new IndexRequest(index);
			ir.id(json.get("id").asText());
			ir.source(json.toString(), XContentType.JSON);
			request.add(ir);

		}
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
			request.add(new UpdateRequest(index, doc.get("id").toString()).doc(doc));

		BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);

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

		sourceBuilder.trackTotalHits(true);
		SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.source(sourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

		List<MapDocument> result = new ArrayList<>();

		long totalHits = searchResponse.getHits().getTotalHits().value;

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

		applyMapBounds(searchParams, masterBoolQuery, geoAggregationField);

		AggregationBuilder aggregation;

		if (filter.equals(Constants.MVR_SCIENTIFIC_NAME)) {
			aggregation = AggregationBuilders.terms(filter).field(filter).size(50000);
		} else if (filter.equals(Constants.AUTHOR_ID) || filter.equals(Constants.IDENTIFIER_ID)) {
			aggregation = AggregationBuilders.terms(filter).field(filter).size(20000).order(BucketOrder.count(false));
		} else {
			aggregation = AggregationBuilders.terms(filter).field(filter).size(1000);
		}

		AggregationResponse aggregationResponse = new AggregationResponse();

		if (filter.equals(Constants.MAX_VOTED_RECO) || filter.equals(Constants.MVR_TAXON_STATUS)) {
			AggregationResponse temp = null;
			aggregation = AggregationBuilders.filter(Constants.AVAILABLE, QueryBuilders.existsQuery(filter));
			temp = groupAggregation(index, type, aggregation, masterBoolQuery, filter);
			HashMap<Object, Long> t = new HashMap<>();
			if (temp != null) {
				for (Map.Entry<Object, Long> entry : temp.getGroupAggregation().entrySet()) {
					t.put(entry.getKey(), entry.getValue());
				}
			}
			if (filter.equals(Constants.MAX_VOTED_RECO))
				aggregation = AggregationBuilders.missing("miss").field(filter.concat(".id"));
			if (filter.equals(Constants.MVR_TAXON_STATUS))
				aggregation = AggregationBuilders.missing("miss").field(filter.concat(".keyword"));
			temp = groupAggregation(index, type, aggregation, masterBoolQuery, filter);
			if (temp != null) {
				for (Map.Entry<Object, Long> entry : temp.getGroupAggregation().entrySet()) {
					t.put(entry.getKey(), entry.getValue());
				}
			}
			aggregationResponse.setGroupAggregation(t);

		} else {
			aggregationResponse = groupAggregation(index, type, aggregation, masterBoolQuery, filter);

		}
		return aggregationResponse;
	}

	public List<IdentifiersInfo> identifierInfo(String index, String userIds) {
		List<String> l = Arrays.asList(userIds.split(","));
		List<IdentifiersInfo> result = new ArrayList<>();

		for (int i = 0; i < l.size(); i++) {
			String id = l.get(i);
			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
					.must(QueryBuilders.termQuery("all_reco_vote.authors_voted.id", id));

			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
			sourceBuilder.query(boolQueryBuilder);
			sourceBuilder.size(1);

			SearchRequest request = new SearchRequest(index);
			request.source(sourceBuilder);
			SearchResponse response;
			try {
				response = client.search(request, RequestOptions.DEFAULT);
				for (SearchHit hit : response.getHits().getHits()) {
					Map<String, Object> sourceMap = hit.getSourceAsMap();
					List<Object> allRecoVote = new ArrayList<>((List<Object>) sourceMap.get("all_reco_vote"));

					for (int n = 0; n < allRecoVote.size(); n++) {
						Map<String, Object> identificationObject = new HashMap<>(
								(Map<String, Object>) allRecoVote.get(n));
						List<Object> authorsVoted = new ArrayList<>(
								(List<Object>) identificationObject.get("authors_voted"));

						for (int k = 0; k < authorsVoted.size(); k++) {
							Map<String, Object> identifier = new HashMap<>((Map<String, Object>) authorsVoted.get(k));
							String authorId = String.valueOf(identifier.get("id"));
							if (authorId.equals(id)) {
								String name = String.valueOf(identifier.get("name"));
								String pic = String.valueOf(identifier.get("profile_pic"));
								Long identifierId = Long.parseLong(String.valueOf(identifier.get("id")));
								IdentifiersInfo identifierInfo = new IdentifiersInfo(name, pic, identifierId);
								result.add(identifierInfo);
								break;
							}
						}
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
		return (result);
	}

	public List<UploadersInfo> uploaderInfo(String index, String userIds) {
		List<String> l = Arrays.asList(userIds.split(","));
		List<UploadersInfo> result = new ArrayList<>();

		for (int i = 0; i < l.size(); i++) {
			String id = l.get(i);
			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
					.must(QueryBuilders.termQuery("author_id", id));
			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

			sourceBuilder.query(boolQueryBuilder);
			sourceBuilder.size(1);
			SearchRequest request = new SearchRequest(index);
			request.source(sourceBuilder);
			SearchResponse response;
			try {
				response = client.search(request, RequestOptions.DEFAULT);
				for (SearchHit hit : response.getHits().getHits()) {
					Map<String, Object> sourceMap = hit.getSourceAsMap();
					String name = String.valueOf(sourceMap.get("created_by"));
					String pic = String.valueOf(sourceMap.get("profile_pic"));
					Long authorId = Long.parseLong(String.valueOf(sourceMap.get("author_id")));
					UploadersInfo uploaderInfo = new UploadersInfo(name, pic, authorId);
					result.add(uploaderInfo);
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
		return (result);
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
			Integer geoAggegationPrecision, Boolean onlyFilteredAggregation, String termsAggregationField,
			String geoShapeFilterField) throws IOException {

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

		if (geoShapeFilterField != null) {
			applyShapeFilter(searchParams, masterBoolQuery, geoShapeFilterField);
		}
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
		searchRequest.source(sourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

		Aggregation aggregation = searchResponse.getAggregations().asList().get(0);
		XContentBuilder builder = XContentFactory.jsonBuilder();
		builder.startObject();
		aggregation.toXContent(builder, ToXContent.EMPTY_PARAMS);
		builder.endObject();
		String result2 = Strings.toString(builder);
		logger.info("Aggregation search: {} completed", aggregation.getName());

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
		request.source(sourceBuilder);
		SearchResponse response = client.search(request, RequestOptions.DEFAULT);

		Map<Object, Long> groupMonth;

		if (filter.equals(Constants.MVR_SCIENTIFIC_NAME) || filter.equals(Constants.AUTHOR_ID)
				|| filter.equals(Constants.IDENTIFIER_ID)) {

			groupMonth = new LinkedHashMap<Object, Long>();
		} else {
			groupMonth = new HashMap<Object, Long>();
		}

		if (filter.equals(Constants.MVR_TAXON_STATUS) || filter.equals(Constants.MAX_VOTED_RECO)) {
			Filter filterAgg = response.getAggregations().get(Constants.AVAILABLE);
			if (filterAgg != null) {
				groupMonth.put(Constants.AVAILABLE, filterAgg.getDocCount());
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
	public ObservationInfo getObservationRightPan(String index, String type, String id, Boolean isMaxVotedRecoId)
			throws IOException {

		MatchPhraseQueryBuilder masterBoolQuery = getBoolQueryBuilderObservationPan(id, isMaxVotedRecoId);
		AggregationBuilder aggregation = AggregationBuilders.terms("observed_in_month")
				.field("observed_in_month.keyword").size(1000);

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.query(masterBoolQuery);
		sourceBuilder.aggregation(aggregation);
		sourceBuilder.size(1000);
		String[] includes = { Constants.OBSERVATION_ID, Constants.REPR_IMAGE_URL, Constants.MAX_VOTED_RECO,
				Constants.LOCATION };
		sourceBuilder.fetchSource(includes, null);
		sourceBuilder.sort("created_on", SortOrder.DESC);

		SearchRequest request = new SearchRequest(index);
		request.source(sourceBuilder);

		SearchResponse response = client.search(request, RequestOptions.DEFAULT);

		List<SimilarObservation> similarObservation = new ArrayList<SimilarObservation>();
		List<ObservationMapInfo> latlon = new ArrayList<ObservationMapInfo>();

		for (SearchHit hit : response.getHits().getHits()) {

			Location loc = objectMapper.readValue(
					objectMapper.writeValueAsString(hit.getSourceAsMap().get(Constants.LOCATION)), Location.class);
			MaxVotedReco maxVotedReco = objectMapper.readValue(
					objectMapper.writeValueAsString(hit.getSourceAsMap().get(Constants.MAX_VOTED_RECO)),
					MaxVotedReco.class);

			if (maxVotedReco == null)
				maxVotedReco = new MaxVotedReco();

			latlon.add(new ObservationMapInfo(
					Long.parseLong(hit.getSourceAsMap().get(Constants.OBSERVATION_ID).toString()),
					maxVotedReco.getScientific_name(), loc.getLat(), loc.getLon()));

			similarObservation.add(new SimilarObservation(
					Long.parseLong(hit.getSourceAsMap().get(Constants.OBSERVATION_ID).toString()),
					maxVotedReco.getScientific_name(),
					String.valueOf(hit.getSourceAsMap().get(Constants.REPR_IMAGE_URL))));
		}
		HashMap<Object, Long> groupMonth = new HashMap<Object, Long>();
		Terms frommonth = response.getAggregations().get("observed_in_month");
		for (Terms.Bucket entry : frommonth.getBuckets()) {
			groupMonth.put(entry.getKey(), entry.getDocCount());
		}

		return new ObservationInfo(groupMonth, similarObservation, latlon);
	}

	@Override
	public List<ObservationNearBy> observationNearBy(String index, String type, Double lat, Double Lon)
			throws IOException {

		GeoDistanceSortBuilder sortBuilder = SortBuilders.geoDistanceSort(Constants.LOCATION, lat, Lon);
		sortBuilder.order(SortOrder.ASC);
		sortBuilder.unit(DistanceUnit.KILOMETERS);
		sortBuilder.geoDistance(GeoDistance.PLANE);
		sortBuilder.validation(GeoValidationMethod.STRICT);
		sortBuilder.ignoreUnmapped(true);

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.sort(sortBuilder);
		sourceBuilder.size(15);
		String[] includes = { Constants.OBSERVATION_ID, Constants.REPR_IMAGE_URL, Constants.MAX_VOTED_RECO,
				Constants.LOCATION, "group_name" };
		sourceBuilder.fetchSource(includes, null);

		SearchRequest request = new SearchRequest(index);
		request.source(sourceBuilder);

		SearchResponse response = client.search(request, RequestOptions.DEFAULT);

		List<ObservationNearBy> nearBy = new ArrayList<ObservationNearBy>();

		Double distance;
		Double lat2;
		Double lon2;
		for (SearchHit hit : response.getHits().getHits()) {

			Location loc = objectMapper.readValue(
					objectMapper.writeValueAsString(hit.getSourceAsMap().get(Constants.LOCATION)), Location.class);

			MaxVotedReco maxVotedReco = objectMapper.readValue(
					objectMapper.writeValueAsString(hit.getSourceAsMap().get(Constants.MAX_VOTED_RECO)),
					MaxVotedReco.class);
			if (maxVotedReco == null)
				maxVotedReco = new MaxVotedReco();

			lat2 = loc.getLat();
			lon2 = loc.getLon();
			distance = distanceCalculate(lat, Lon, lat2, lon2);

			nearBy.add(
					new ObservationNearBy(Long.parseLong(hit.getSourceAsMap().get(Constants.OBSERVATION_ID).toString()),
							maxVotedReco.getScientific_name(),
							String.valueOf(hit.getSourceAsMap().get(Constants.REPR_IMAGE_URL)), distance,
							hit.getSourceAsMap().get("group_name").toString()));

		}

		Collections.sort(nearBy, (obv1, obv2) -> obv1.getDistance().compareTo(obv2.getDistance()));

		return nearBy;
	}

	public Double distanceCalculate(Double lat1, Double lon1, Double lat2, Double lon2) {
		Double dist = 0.0;
		if ((lat1.equals(lat2)) && (lon1.equals(lon2))) {
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
		searchSourceBuilder.fetchSource(null, new String[] { Constants.TIMESTAMP, Constants.VERSION });
		SearchResponse searchResponse = null;
		try {
			searchSourceBuilder.query(query);
			searchRequest.source(searchSourceBuilder);
			searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

			for (SearchHit hit : searchResponse.getHits().getHits()) {
				try {
					matchedResults.add(objectMapper.readValue(hit.getSourceAsString(), classMapped));
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
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
		searchSourceBuilder.fetchSource(null, new String[] { Constants.TIMESTAMP, Constants.VERSION });
		SearchResponse searchResponse = null;
		try {
			searchSourceBuilder.query(query);
			searchRequest.source(searchSourceBuilder);
			searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

			for (SearchHit hit : searchResponse.getHits().getHits()) {
				try {
					matchedResults.add(objectMapper.readValue(hit.getSourceAsString(), classMapped));
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return matchedResults;
	}

	@Override
	public List<ExtendedTaxonDefinition> matchPhrase(String index, String type, String scientificName,
			String scientificText, String canonicalName, String canonicalText, Boolean checkOnAllParam) {

		String scientificFieldName = "name.raw";
		String canonicalFieldName = "canonical_form";

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		if (checkOnAllParam == true) {
			boolQueryBuilder.must(QueryBuilders.matchQuery(scientificFieldName, scientificText).operator(Operator.AND));
		}
		boolQueryBuilder.must(QueryBuilders.matchPhraseQuery(canonicalFieldName, canonicalText));

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.fetchSource(null, new String[] { Constants.TIMESTAMP, Constants.VERSION });
		searchSourceBuilder.size(10000);

		SearchResponse searchResponse = null;
		SearchRequest searchRequest = new SearchRequest(index);
		try {
			searchSourceBuilder.query(boolQueryBuilder);
			searchRequest.source(searchSourceBuilder);
//			searchResponse = client.search(searchRequest); DEPRECATED
			searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		if (searchResponse != null && searchResponse.getHits().getTotalHits().value == 0) {
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

		if (searchResponse != null && searchResponse.getHits().getTotalHits().value == 0)
			return new ArrayList<>();

		return processElasticResponse(searchResponse);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<String> getListPageFilterValue(String index, String type, String filterOn, String text) {

		List<String> results = new ArrayList<String>();
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		SearchResponse searchResponse = null;
		SearchRequest searchRequest = new SearchRequest(index);

		if (filterOn.equalsIgnoreCase("district") || filterOn.equalsIgnoreCase("tahsil")
				|| filterOn.equalsIgnoreCase("tags")) {
			String prefixPath = null;
			if (filterOn.equalsIgnoreCase("tags")) {
				prefixPath = "tags" + ".";
				filterOn = "name";
			} else {
				prefixPath = "location_information" + ".";
			}
			CompletionSuggestionBuilder completionSuggestor = SuggestBuilders
					.completionSuggestion(prefixPath + filterOn).prefix(text).skipDuplicates(true).size(100);
			SuggestBuilder suggestBuilder = new SuggestBuilder();
			suggestBuilder.addSuggestion(filterOn, completionSuggestor);
			searchSourceBuilder.suggest(suggestBuilder).fetchSource(false);
			searchRequest.source(searchSourceBuilder);
			try {
				searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
				Suggestion<? extends Entry<? extends Option>> suggestions = searchResponse.getSuggest()
						.getSuggestion(filterOn);
				List<? extends Entry<? extends Option>> entries = suggestions.getEntries();

				for (Entry<? extends Option> entry : entries) {
					List<Suggest.Suggestion.Entry.Option> options = (List<Option>) entry.getOptions();
					for (Suggest.Suggestion.Entry.Option option : options) {
						results.add(option.getText().toString());
					}
				}

			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		} else if (filterOn.equalsIgnoreCase("reconame")) {
			String field = "all_reco_vote.scientific_name.name";
			searchSourceBuilder.fetchSource(field, null);
			searchSourceBuilder.size(100);
			QueryBuilder queryBuilder = QueryBuilders.matchPhraseQuery(field, text);
			searchSourceBuilder.query(queryBuilder);
			searchRequest.source(searchSourceBuilder);
			try {
				searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
				for (SearchHit hit : searchResponse.getHits().getHits()) {
					Collection<Object> s = hit.getSourceAsMap().values();
					results.add(s.toString().replaceAll("[\\[\\]{}]", "").split("=")[2]);
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
			}

		}
		results = (List<String>) new HashSet(results).stream().sorted().collect(Collectors.toList());
		return results;
	}

	@Override
	public List<LinkedHashMap<String, LinkedHashMap<String, String>>> getUserScore(String index, String type,
			Integer authorId, String timeFilter) {
		try {
			AggregationBuilder aggs = buildSortingAggregation(1, null);

			QueryBuilder rangeFilter = new RangeQueryBuilder("created_on").gte(timeFilter);
			QueryBuilder queryBuilder = QueryBuilders.boolQuery()
					.filter(QueryBuilders.termQuery(Constants.AUTHOR_ID, authorId))
					.must(QueryBuilders.boolQuery().filter(rangeFilter));
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			aggs.subAggregation(populateDataAggregation());
			aggs.subAggregation(termsAggregation(Constants.PROFILE_PIC, "profile_pic.keyword"));
			aggs.subAggregation(termsAggregation(Constants.AUTHOR_NAME, "name.keyword"));
			searchSourceBuilder.aggregation(aggs);
			searchSourceBuilder.query(queryBuilder);
			searchSourceBuilder.size(0);
			SearchRequest searchRequest = new SearchRequest(index);
			SearchResponse searchResponse = null;
			searchRequest.source(searchSourceBuilder);
			try {
				searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
			return processAggregationResponse(searchResponse);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;

	}

	@Override
	public List<LinkedHashMap<String, LinkedHashMap<String, String>>> getTopUsers(String index, String type,
			String sortingValue, Integer topUser, String timeFilter) {
		// For Filtering the records based on the time frame
		QueryBuilder rangeFilter = new RangeQueryBuilder("created_on").gte(timeFilter);
		QueryBuilder filterByDate = QueryBuilders.boolQuery().must(QueryBuilders.boolQuery().filter(rangeFilter));

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		AggregationBuilder aggs = buildSortingAggregation(topUser, sortingValue);
		searchSourceBuilder.aggregation(aggs);
		if (timeFilter != null)
			searchSourceBuilder.query(filterByDate);
		searchSourceBuilder.size(0);
		SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.source(searchSourceBuilder);

		SearchResponse searchResponse = null;
		try {
			searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		// processAggregationResponse(searchRespone)
		List<Integer> topUserIds = new ArrayList<>();
		if (searchResponse != null) {
			topUserIds.addAll(getUserIds(searchResponse));
		}

		// added the must part in the previous below query builder
		QueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.filter(QueryBuilders.termsQuery(Constants.AUTHOR_ID, topUserIds))
				.must(QueryBuilders.boolQuery().filter(rangeFilter));

		searchSourceBuilder = new SearchSourceBuilder();
		aggs.subAggregation(populateDataAggregation());
		aggs.subAggregation(termsAggregation(Constants.PROFILE_PIC, "profile_pic.keyword"));
		aggs.subAggregation(termsAggregation(Constants.AUTHOR_NAME, "name.keyword"));
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
		AggregationBuilder aggs = termsAggregation(Constants.GROUP_BY_AUTHOR, Constants.AUTHOR_ID,
				TOTAL_USER_UPPER_BOUND);
		aggs.subAggregation(
				filterAggregation("group_by_score_category_engagement", "score_category.keyword", "Engagement"));
		aggs.subAggregation(filterAggregation("group_by_score_category_content", "score_category.keyword", "Content"));
		if (sortingValue != null) {
			if (sortingValue.contains(".")) {
				sortingField = "module_activity_category.keyword";
			} else {
				sortingField = "module.keyword";
			}
			aggs.subAggregation(filterAggregation("group_by_module", sortingField, sortingValue));
		}

		aggs.subAggregation(getBucketScriptAggregation());
		aggs.subAggregation(getBucketSortAggregation(sortingValue, topUser));
		return aggs;
	}

	private AggregationBuilder populateDataAggregation() {

		AggregationBuilder aggs = termsAggregation("bucket_by_module", "module.keyword")
				.subAggregation(termsAggregation("bucket_by_activity_category", "activity_category.keyword"));
		return aggs;
	}

	private AggregationBuilder termsAggregation(String aggregationName, String field, Integer totalBucket) {
		AggregationBuilder aggs = AggregationBuilders.terms(aggregationName).field(field).size(totalBucket);
		return aggs;
	}

	private AggregationBuilder termsAggregation(String aggregationName, String field) {

		AggregationBuilder aggs = AggregationBuilders.terms(aggregationName).field(field).size(100);
		return aggs;
	}

	private AggregationBuilder filterAggregation(String aggregationName, String field, String fieldValue) {

		AggregationBuilder aggs = AggregationBuilders.filter(aggregationName,
				QueryBuilders.termQuery(field, fieldValue));

		return aggs;
	}

	private BucketScriptPipelineAggregationBuilder getBucketScriptAggregation() {
		Map<String, String> bucketsPathsMap = new HashMap<>();
		bucketsPathsMap.put("engagement", "group_by_score_category_engagement>_count");
		bucketsPathsMap.put("content", "group_by_score_category_content>_count");
		Script script = new Script("Math.round(10*(Math.log10(params.content)+Math.log10(params.engagement)))");

		BucketScriptPipelineAggregationBuilder bucketScript = PipelineAggregatorBuilders
				.bucketScript(Constants.ACTIVITY_SCORE, bucketsPathsMap, script);
		return bucketScript;
	}

	private BucketSortPipelineAggregationBuilder getBucketSortAggregation(String sortingValue, int topUsers) {
		List<FieldSortBuilder> sortingCriteriaList = new ArrayList<FieldSortBuilder>();
		String sortOnAggregation = null;
		if (sortingValue == null) {
			sortOnAggregation = Constants.ACTIVITY_SCORE;
		} else {
			sortOnAggregation = "group_by_module" + ">_count";
		}

		FieldSortBuilder sortOn = new FieldSortBuilder(sortOnAggregation).order(SortOrder.DESC);
		sortingCriteriaList.add(sortOn);

		BucketSortPipelineAggregationBuilder bucketSort = PipelineAggregatorBuilders
				.bucketSort("bucket_sorting", sortingCriteriaList).size(topUsers);

		return bucketSort;
	}

	private List<ExtendedTaxonDefinition> processElasticResponse(SearchResponse searchResponse) {
		List<ExtendedTaxonDefinition> matchedResults = new ArrayList<ExtendedTaxonDefinition>();
		if (searchResponse == null) {
			return matchedResults;
		}
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
		Terms authorTerms = searchResponse.getAggregations().get(Constants.GROUP_BY_AUTHOR);
		Collection<? extends Bucket> authorBuckets = authorTerms.getBuckets();
		List<Integer> topAuthors = new ArrayList<Integer>();
		for (Bucket authorBucket : authorBuckets) {
			topAuthors.add(authorBucket.getKeyAsNumber().intValue());
		}
		return topAuthors;

	}

	private List<LinkedHashMap<String, LinkedHashMap<String, String>>> processAggregationResponse(
			SearchResponse searchResponse) {
		List<LinkedHashMap<String, LinkedHashMap<String, String>>> records = new ArrayList<LinkedHashMap<String, LinkedHashMap<String, String>>>();

		Terms authorTerms = searchResponse.getAggregations().get(Constants.GROUP_BY_AUTHOR);
		Collection<? extends Bucket> authorBuckets = authorTerms.getBuckets();

		for (Bucket authorBucket : authorBuckets) {

			Terms moduleTerms = authorBucket.getAggregations().get("bucket_by_module");
			Collection<? extends Bucket> moduleBuckets = moduleTerms.getBuckets();
			LinkedHashMap<String, LinkedHashMap<String, String>> moduleRecords = new LinkedHashMap<String, LinkedHashMap<String, String>>();

			for (Bucket moduleBucket : moduleBuckets) {
				Terms activityTerms = moduleBucket.getAggregations().get("bucket_by_activity_category");
				Collection<? extends Bucket> activityBuckets = activityTerms.getBuckets();
				LinkedHashMap<String, String> activities = new LinkedHashMap<String, String>();

				for (Bucket activityBucket : activityBuckets) {
					activities.put(activityBucket.getKeyAsString().toLowerCase(),
							String.valueOf(activityBucket.getDocCount()));
				}
				moduleRecords.put(moduleBucket.getKeyAsString().toLowerCase(), activities);
			}
			LinkedHashMap<String, String> userDetails = new LinkedHashMap<String, String>();

			Terms detailTerms = authorBucket.getAggregations().get(Constants.AUTHOR_NAME);
			for (Bucket bucket : detailTerms.getBuckets()) {
				userDetails.put("authorName", bucket.getKeyAsString());
			}
			userDetails.put(Constants.AUTHOR_ID, authorBucket.getKeyAsString());
			detailTerms = authorBucket.getAggregations().get(Constants.PROFILE_PIC);
			for (Bucket bucket : detailTerms.getBuckets()) {
				userDetails.put("profilePic", bucket.getKeyAsString());
			}
			ParsedSimpleValue activityScoreTerms = authorBucket.getAggregations().get(Constants.ACTIVITY_SCORE);
			if (Double.parseDouble(activityScoreTerms.getValueAsString()) >= 0.0d) {
				userDetails.put(activityScoreTerms.getName(), activityScoreTerms.getValueAsString());
			} else {
				userDetails.put(activityScoreTerms.getName(), "0.0");
			}
			moduleRecords.put("details", userDetails);
			records.add(moduleRecords);
		}
		return records;
	}

	@Override
	public GeoHashAggregationData getNewGeoAggregation(String index, String type, MapSearchQuery searchQuery,
			String geoAggregationField, Integer geoAggegationPrecision) {

		GeoHashAggregationData geoHashAggData = null;

		try {
			MapSearchParams searchParams = searchQuery.getSearchParams();
			BoolQueryBuilder masterBoolQuery = getBoolQueryBuilder(searchQuery);

			GeoGridAggregationBuilder geoGridAggregationBuilder = getGeoGridAggregationBuilder(geoAggregationField,
					geoAggegationPrecision);

			applyMapBounds(searchParams, masterBoolQuery, geoAggregationField);

			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
			if (masterBoolQuery != null)
				sourceBuilder.query(masterBoolQuery);
			sourceBuilder.aggregation(geoGridAggregationBuilder);
			sourceBuilder.trackTotalHits(true);

			SearchRequest searchRequest = new SearchRequest(index);
			searchRequest.source(sourceBuilder);
			SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
			Long totalCount = searchResponse.getHits().getTotalHits().value;
			Map<String, Long> geoHashData = new HashMap<String, Long>();

			Aggregations aggregations = searchResponse.getAggregations();
			ParsedGeoHashGrid geoHashGrid = aggregations.get(geoAggregationField + "-" + geoAggegationPrecision);

			for (GeoGrid.Bucket b : geoHashGrid.getBuckets()) {
				geoHashData.put(b.getKeyAsString(), b.getDocCount());
			}

			geoHashAggData = new GeoHashAggregationData(geoHashData, totalCount);

		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		return geoHashAggData;
	}

	@Override
	public FilterPanelData getListPanel(String index, String type) {
		try {

			AggregationBuilder speciesGroupAggregation = AggregationBuilders.terms("speciesGroup")
					.field("sgroup_filter.keyword").size(100).order(BucketOrder.key(true));
			AggregationBuilder userGroupAgregation = AggregationBuilders.terms("userGroup")
					.field("user_group_observations.ug_filter.keyword").size(100).order(BucketOrder.count(false));
			AggregationBuilder stateAggregation = AggregationBuilders.terms("state")
					.field("location_information.state.raw").size(100).order(BucketOrder.key(true));
			AggregationBuilder traitAggregation = AggregationBuilders.terms("trait")
					.field("facts.trait_value.trait_filter.keyword").size(1000).order(BucketOrder.key(true));
			AggregationBuilder cfAggregation = AggregationBuilders.terms("customField")
					.field("custom_fields.custom_field.custom_field_values.custom_field_filter.keyword").size(1000)
					.order(BucketOrder.key(true));

			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
			sourceBuilder.aggregation(speciesGroupAggregation);
			sourceBuilder.aggregation(userGroupAgregation);
			sourceBuilder.aggregation(traitAggregation);
			sourceBuilder.aggregation(stateAggregation);
			sourceBuilder.aggregation(cfAggregation);

			SearchRequest request = new SearchRequest(index);
			request.source(sourceBuilder);

			SearchResponse response = client.search(request, RequestOptions.DEFAULT);
			FilterPanelData filterPanel = new FilterPanelData();

			filterPanel.setSpeciesGroup(getAggregationSpeciesGroup(response.getAggregations().get("speciesGroup")));
			filterPanel.setStates(getAggregationList(response.getAggregations().get("state")));
			filterPanel.setUserGroup(getAggregationUserGroup(response.getAggregations().get("userGroup")));
			filterPanel.setTraits(getTraits(response.getAggregations().get("trait")));
			filterPanel.setCustomFields(getCustomFields(response.getAggregations().get("customField")));
			return filterPanel;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;

	}

	private List<String> getAggregationList(Terms terms) {
		List<String> resultList = new ArrayList<String>();
		for (Terms.Bucket b : terms.getBuckets()) {
			resultList.add(b.getKeyAsString());
		}
		return resultList;
	}

	private List<SpeciesGroup> getAggregationSpeciesGroup(Terms terms) {
		List<SpeciesGroup> sGroup = new ArrayList<SpeciesGroup>();
		for (Terms.Bucket b : terms.getBuckets()) {
//			pattern = sgroupId | sgroupName | sGroupOrder
			String[] sGroupArray = b.getKeyAsString().split("\\|");
			sGroup.add(
					new SpeciesGroup(Long.parseLong(sGroupArray[0]), sGroupArray[1], Integer.parseInt(sGroupArray[2])));
		}
		return sGroup;
	}

	private List<UserGroup> getAggregationUserGroup(Terms terms) {
		List<UserGroup> userGroup = new ArrayList<UserGroup>();
		for (Terms.Bucket b : terms.getBuckets()) {
//			pattern = usergroupId | userGroupName |domain name | webaddress
			String[] ugArray = b.getKeyAsString().split("\\|");
			String webAddress = "";
			if (ugArray[2].length() != 0)
				webAddress = ugArray[2];
			else
				webAddress = "/group/" + ugArray[3];
			userGroup.add(new UserGroup(Long.parseLong(ugArray[0]), ugArray[1], webAddress));
		}
		return userGroup;
	}

	private List<Traits> getTraits(Terms terms) {
		Map<Long, Traits> traitMap = new TreeMap<Long, Traits>();
		List<Traits> traits = new ArrayList<Traits>();
		for (Terms.Bucket b : terms.getBuckets()) {
			String[] traitArray = b.getKeyAsString().split("\\|");
//			pattern = traitID | traitName | traitType | traitValue | TraitValueIconURL

			if (traitMap.containsKey(Long.parseLong(traitArray[0]))) {
				Traits traitMapped = traitMap.get(Long.parseLong(traitArray[0]));
				List<TraitValue> valueList = traitMapped.getTraitValues();
				String capitalizeWord = toTitleCase(traitArray[3]);
				valueList.add(new TraitValue(capitalizeWord, traitArray[4]));
				traitMapped.setTraitValues(valueList);
				traitMap.put(Long.parseLong(traitArray[0]), traitMapped);
			} else {
				List<TraitValue> valueList = new ArrayList<TraitValue>();
				String capitalizeWord = toTitleCase(traitArray[3]);
				valueList.add(new TraitValue(capitalizeWord, traitArray[4]));
				Traits traitsMapped = new Traits(Long.parseLong(traitArray[0]), traitArray[1], traitArray[2],
						valueList);

				traitMap.put(Long.parseLong(traitArray[0]), traitsMapped);
			}

		}
		for (java.util.Map.Entry<Long, Traits> entry : traitMap.entrySet()) {
			traits.add(entry.getValue());
		}
		return traits;
	}

	private String toTitleCase(String input) {
		StringBuilder titleCase = new StringBuilder(input.length());
		boolean nextTitleCase = true;

		for (char c : input.toCharArray()) {
			if (Character.isSpaceChar(c)) {
				nextTitleCase = true;
			} else if (nextTitleCase) {
				c = Character.toTitleCase(c);
				nextTitleCase = false;
			}

			titleCase.append(c);
		}

		return titleCase.toString();
	}

	private List<CustomFields> getCustomFields(Terms terms) {
		Map<Long, CustomFields> customFieldMap = new TreeMap<Long, CustomFields>();
		List<CustomFields> customFieldList = new ArrayList<CustomFields>();

		for (Terms.Bucket b : terms.getBuckets()) {
			String customFieldArray[] = b.getKeyAsString().split("\\|");
//			pattern  = cfId | cfName | cfFieldType | cfDataType | cfValueIcon |cfValue

			if (customFieldMap.containsKey(Long.parseLong(customFieldArray[0]))) {

				CustomFields customFieldMapped = customFieldMap.get(Long.parseLong(customFieldArray[0]));
				List<CustomFieldValues> valueList = customFieldMapped.getValues();
				if (!customFieldArray[2].equalsIgnoreCase("FIELD TEXT")) {
					valueList.add(new CustomFieldValues(customFieldArray[5], customFieldArray[4]));
				}
				customFieldMapped.setValues(valueList);
				customFieldMap.put(Long.parseLong(customFieldArray[0]), customFieldMapped);

			} else {
				List<CustomFieldValues> values = null;
				if (!customFieldArray[2].equalsIgnoreCase("FIELD TEXT")) {
					values = new ArrayList<CustomFieldValues>();
					values.add(new CustomFieldValues(customFieldArray[5], customFieldArray[4]));
				}
				CustomFields customFieldMapped = new CustomFields(Long.parseLong(customFieldArray[0]),
						customFieldArray[1], customFieldArray[2], customFieldArray[3], values);
				customFieldMap.put(Long.parseLong(customFieldArray[0]), customFieldMapped);
			}
		}
		for (java.util.Map.Entry<Long, CustomFields> entry : customFieldMap.entrySet()) {
			customFieldList.add(entry.getValue());
		}

		return customFieldList;
	}

	@Override
	public List<ObservationLatLon> getSpeciesCoordinates(String index, String type, String speciesId) {

		try {
			BoolQueryBuilder query = QueryBuilders.boolQuery()
					.must(QueryBuilders.termsQuery("max_voted_reco.species_id", speciesId));

			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
			sourceBuilder.query(query);
			sourceBuilder.size(10000);
			String[] includes = { Constants.OBSERVATION_ID, Constants.LOCATION };
			sourceBuilder.fetchSource(includes, null);
			SearchRequest request = new SearchRequest(index);
			request.source(sourceBuilder);
			SearchResponse response = client.search(request, RequestOptions.DEFAULT);
			List<ObservationLatLon> obvList = new ArrayList<ObservationLatLon>();

			for (SearchHit hit : response.getHits().getHits()) {

				Location loc = objectMapper.readValue(
						objectMapper.writeValueAsString(hit.getSourceAsMap().get(Constants.LOCATION)), Location.class);

				obvList.add(new ObservationLatLon(
						Long.parseLong(hit.getSourceAsMap().get(Constants.OBSERVATION_ID).toString()), loc.getLat(),
						loc.getLon()));

			}
			return obvList;

		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return new ArrayList<>();
	}

	@Override
	public String forceUpdateIndexField(String index, String type, String field, String value,
			List<String> documentIds) {
		UpdateByQueryRequest updateRequest = new UpdateByQueryRequest(index);
		updateRequest.setConflicts("proceed");
		updateRequest.setQuery(new TermsQueryBuilder("_id", documentIds));
		updateRequest.setScript(new Script(ScriptType.INLINE, "painless", "ctx._source." + field + "=" + value,
				Collections.emptyMap()));
		updateRequest.setRefresh(true);
		try {
			BulkByScrollResponse response = client.updateByQuery(updateRequest, RequestOptions.DEFAULT);
			if (response.getUpdated() == response.getTotal()) {
				logger.info("update status - {}", response.getBulkFailures());
				return "Documents Sent - " + documentIds.size() + "\nDocument found in index - " + response.getTotal()
						+ "\nDocument Updated - " + response.getUpdated();
			}
		} catch (IOException e) {
			logger.error("Force Update Exception - {}", e.getMessage());
		}
		return null;
	}

	@Override
	public String fetchIndex() {
		Map<String, Set<String>> indexOuterLevelProperties = new HashMap<String, Set<String>>();
		try {
			GetMappingsRequest mappingsRequest = new GetMappingsRequest();
			mappingsRequest.indices();
			mappingsRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
			GetMappingsResponse getMappingResponse = client.indices().getMapping(mappingsRequest,
					RequestOptions.DEFAULT);
			Map<String, MappingMetadata> allMappings = getMappingResponse.mappings();

			for (Map.Entry<String, MappingMetadata> index : allMappings.entrySet()) {
				if (!(index.getKey().startsWith("."))) {
					MappingMetadata indexMapping = index.getValue();
					Map<String, Object> mapping = indexMapping.sourceAsMap();
					LinkedHashMap<String, Object> properties = (LinkedHashMap<String, Object>) mapping
							.get("properties");
					if (properties != null) {
						indexOuterLevelProperties.put(index.getKey(), properties.keySet());
					}
				}
			}
			return objectMapper.writeValueAsString(indexOuterLevelProperties);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	@Override
	public AuthorUploadedObservationInfo getUserData(String index, String type, Long userId, Integer size, Long sGroup,
			Boolean hasMedia) {

		try {

			List<MaxVotedRecoFreq> maxVotedRecoFreqs = new ArrayList<MaxVotedRecoFreq>();

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
					.must(QueryBuilders.termQuery("author_id", userId));

			if (sGroup != null) {
				boolQueryBuilder.must(QueryBuilders.termQuery("group_id", sGroup));
			}

			if (hasMedia) {
				boolQueryBuilder.must(QueryBuilders.termQuery("no_media", 0));
			}

			AggregationBuilder uploadUniqueSpecies = AggregationBuilders.terms("uploadUniqueSpecies")
					.field("max_voted_reco.id").size(50000).order(BucketOrder.count(false));

			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
			sourceBuilder.query(boolQueryBuilder);
			sourceBuilder.aggregation(uploadUniqueSpecies);

			SearchRequest request = new SearchRequest(index);
			request.source(sourceBuilder);

			SearchResponse response = client.search(request, RequestOptions.DEFAULT);
			Terms terms = response.getAggregations().get("uploadUniqueSpecies");
			int count = 1;
			for (Terms.Bucket b : terms.getBuckets()) {
				if (count <= (size - 10))
					count++;
				else {
					if (count > size)
						break;
					System.out.println(count);
					maxVotedRecoFreqs.add(new MaxVotedRecoFreq(Long.parseLong(b.getKeyAsString()), b.getDocCount()));
					count++;
				}

			}
			Long total = Long.parseLong(String.valueOf(terms.getBuckets().size()));
			AuthorUploadedObservationInfo result = new AuthorUploadedObservationInfo(total, maxVotedRecoFreqs);
			return result;

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;

	}
}
