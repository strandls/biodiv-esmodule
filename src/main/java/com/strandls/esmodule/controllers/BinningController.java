package com.strandls.esmodule.controllers;

import java.io.IOException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.inject.Inject;
import com.strandls.esmodule.ApiConstants;
import com.strandls.esmodule.binning.models.GeojsonData;
import com.strandls.esmodule.binning.servicesImpl.BinningServiceImpl;
import com.strandls.esmodule.models.MapBounds;

/**
 * Controller for binning related services
 * 
 * @author mukund
 *
 */
@Path(ApiConstants.V1 + ApiConstants.BINNING)
public class BinningController {

	@Inject
	BinningServiceImpl service;

	@POST
	@Path(ApiConstants.SQUARE + "/{index}/{type}")
	@Produces(MediaType.APPLICATION_JSON)
	public GeojsonData bin(@PathParam("index") String index, @PathParam("type") String type,
			@QueryParam("geoField") String geoField, @QueryParam("cellSideKm") Double cellSideKm, MapBounds bounds) {

		try {
			return service.squareBin(index, type, geoField, bounds, cellSideKm);
		} catch (IOException e) {
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

}
