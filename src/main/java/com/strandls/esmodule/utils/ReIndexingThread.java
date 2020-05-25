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
//			System.out.println("Starting Re`Indexing");
			elasticAdminSearchService.reIndexObservation(index, mapping);
//			System.out.println("Finishing Re-Indexing");
		} catch (IOException e) {
//			e.printStackTrace();
			logger.error(e.getMessage());	
		}
	}

}
