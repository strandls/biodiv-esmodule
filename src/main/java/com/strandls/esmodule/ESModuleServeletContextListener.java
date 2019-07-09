package com.strandls.esmodule;

import java.io.IOException;

import javax.servlet.ServletContextEvent;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.strandls.es.ElasticSearchClient;
import com.strandls.esmodule.binning.servicesImpl.BinningModule;
import com.strandls.esmodule.controllers.ESControllerModule;
import com.strandls.esmodule.services.impl.ESServiceImplModule;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

/**
 * @author Abhishek Rudra
 *
 */

public class ESModuleServeletContextListener extends GuiceServletContextListener {

	private final Logger logger = LoggerFactory.getLogger(ESModuleServeletContextListener.class);

	@Override
	protected Injector getInjector() {
		Injector injector = Guice.createInjector(new JerseyServletModule() {
			@Override
			protected void configureServlets() {

				ElasticSearchClient esClient = new ElasticSearchClient(
						RestClient.builder(HttpHost.create(ESmoduleConfig.getString("es.url"))));
				bind(ElasticSearchClient.class).toInstance(esClient);

				serve("/*").with(GuiceContainer.class);
			}
		}, new ESControllerModule(), new ESServiceImplModule(), new BinningModule());

		return injector;

	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		Injector injector = (Injector) sce.getServletContext().getAttribute(Injector.class.getName());

		ElasticSearchClient elasticSearchClient = injector.getInstance(ElasticSearchClient.class);
		if (elasticSearchClient != null) {
			try {
				elasticSearchClient.close();
			} catch (IOException e) {
				logger.error("Error closing elasticsearch client. ", e);
			}
		}

		super.contextDestroyed(sce);
	}

}
