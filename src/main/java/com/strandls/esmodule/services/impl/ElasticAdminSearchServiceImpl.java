package com.strandls.esmodule.services.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.ws.rs.core.Response.Status;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import com.strandls.es.ElasticSearchClient;
import com.strandls.esmodule.models.MapDocument;
import com.strandls.esmodule.models.MapQueryResponse;
import com.strandls.esmodule.models.MapQueryStatus;
import com.strandls.esmodule.services.ElasticAdminSearchService;

/**
 * Implementation of {@link ElasticAdminSearchService}
 * 
 * @author mukund
 *
 */
public class ElasticAdminSearchServiceImpl implements ElasticAdminSearchService {

	private final RestClient client;

	private final Logger logger = LoggerFactory.getLogger(ElasticAdminSearchServiceImpl.class);

	@Inject
	public ElasticAdminSearchServiceImpl(ElasticSearchClient client) {
		this.client = client.getLowLevelClient();
	}

	/*
	 * (non-Javadoc)
	 * @see com.strandls.naksha.es.services.api.ElasticAdminSearchService#postMapping(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public MapQueryResponse postMapping(String index, String mapping) throws IOException {

		logger.info("Trying to add mapping to index: {}", index);

		StringEntity entity = null;
		if (!Strings.isNullOrEmpty(mapping)) {
			entity = new StringEntity(mapping, ContentType.APPLICATION_JSON);
		}

		Response response = client.performRequest("PUT", index + "/_mapping", new HashMap<>(), entity);
		String status = response.getStatusLine().getReasonPhrase();

		logger.info("Added mapping to index: {} with status: {}", index, status);

		return new MapQueryResponse(MapQueryStatus.UNKNOWN, status);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.strandls.naksha.es.services.api.ElasticAdminSearchService#getMapping(java.lang.String)
	 */
	@Override
	public MapDocument getMapping(String index) throws IOException {

		logger.info("Trying to get mapping for index: {}", index);

		Response response = client.performRequest("GET", index + "/_mapping");
		String status = response.getStatusLine().getReasonPhrase();

		logger.info("Retrieved mapping for index: {} with status: {}", index, status);

		return new MapDocument(EntityUtils.toString(response.getEntity()));
	}

	/*
	 * (non-Javadoc)
	 * @see com.strandls.naksha.es.services.api.ElasticAdminSearchService#createIndex(java.lang.String, java.lang.String)
	 */
	@Override
	public MapQueryResponse createIndex(String index, String type) throws IOException {

		logger.info("Trying to create index: {}", index);

		Response response = client.performRequest("PUT", "/" + index);
		String status = response.getStatusLine().getReasonPhrase();

		logger.info("Created index: {} with status: {}", index, status);

		return new MapQueryResponse(MapQueryStatus.UNKNOWN, status);
	}

	@Override
	public MapQueryResponse esPostMapping(String index,String mapping) throws IOException {
		logger.info("Trying to add mapping to index: {}", index);
		
		StringEntity entity = null;
		if (!Strings.isNullOrEmpty(mapping)) {
			entity = new StringEntity(mapping, ContentType.APPLICATION_JSON);
		}
		Response response = client.performRequest("PUT", index+"/", new HashMap<>(), entity);
		
		String status = response.getStatusLine().getReasonPhrase();
		
		logger.info("Added mapping to index: {} with status: {}", index, status);
		return new MapQueryResponse(MapQueryStatus.UNKNOWN, status);
	}

	@Override
	public MapQueryResponse reIndexObservation() throws IOException {
		String filePath = "/app/configurations/scripts/";
		String esUpdateScript = "refreshIndex.sh";
		String viewUpdateScript = "refreshview.sh";
		try {
			Process process = Runtime.getRuntime().exec("sh " + viewUpdateScript, null, new File(filePath));
			int exitCode = process.waitFor();
			if(exitCode == 0)
			{
				process = Runtime.getRuntime().exec("sh " + esUpdateScript, null, new File(filePath));
				exitCode = process.waitFor();
				if(exitCode == 0) {
					return new MapQueryResponse(MapQueryStatus.UPDATED, "re-indexing successful");					
				}
			}
		}
		catch (Exception e) {
			return new MapQueryResponse(MapQueryStatus.ERROR,e.getMessage()); 
		}
		return new MapQueryResponse(MapQueryStatus.UNKNOWN,"re-indexing failure"); 
	}
}
