package com.strandls.esmodule.controllers;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * @author Abhishek Rudra
 *
 */

public class ESControllerModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ESController.class).in(Scopes.SINGLETON);
		bind(GeoController.class).in(Scopes.SINGLETON);
		bind(BinningController.class).in(Scopes.SINGLETON);
	}
}
