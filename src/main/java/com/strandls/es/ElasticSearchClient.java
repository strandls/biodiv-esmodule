package com.strandls.es;

import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * @author Abhishek Rudra
 *
 */

public class ElasticSearchClient extends RestHighLevelClient {

	public ElasticSearchClient(RestClientBuilder restClientBuilder) {
		super(restClientBuilder);
	}

}