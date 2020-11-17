/**
 * 
 */
package com.strandls.esmodule.utils;

import java.io.IOException;

import org.slf4j.Logger;

import com.strandls.esmodule.services.ElasticAdminSearchService;

/**
 * @author ashish
 *
 */
public class ReIndexingThread implements Runnable {

	private ElasticAdminSearchService elasticAdminSearchService;
	private String index;
	private String mapping;
	private Logger logger;
	
	
	
	public ReIndexingThread() {
		super();
	}



	public ReIndexingThread(ElasticAdminSearchService elasticAdminSearchService, String index, 
			String mapping, Logger logger) {
		super();
		this.elasticAdminSearchService = elasticAdminSearchService;
		this.index = index;
		this.mapping = mapping;
		this.logger = logger;
	}



	@Override
	public void run() {
		try {
			elasticAdminSearchService.reIndex(index, mapping);
		} catch (IOException e) {
			logger.error(e.getMessage());	
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
			Thread.currentThread().interrupt();
		}
	}

}
