package com.strandls.esmodule.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.strandls.esmodule.ApiConstants;
import com.strandls.esmodule.models.AggregationResponse;
import com.strandls.esmodule.models.MapBoundParams;
import com.strandls.esmodule.models.MapBounds;
import com.strandls.esmodule.models.MapDocument;
import com.strandls.esmodule.models.MapQueryResponse;
import com.strandls.esmodule.models.MapResponse;
import com.strandls.esmodule.models.MapSearchParams;
import com.strandls.esmodule.models.MapSortType;
import com.strandls.esmodule.models.ObservationInfo;
import com.strandls.esmodule.models.ObservationNearBy;
import com.strandls.esmodule.models.query.MapBoolQuery;
import com.strandls.esmodule.models.query.MapRangeQuery;
import com.strandls.esmodule.models.query.MapSearchQuery;
import com.strandls.esmodule.services.ElasticAdminSearchService;
import com.strandls.esmodule.services.ElasticSearchDownloadService;
import com.strandls.esmodule.services.ElasticSearchService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author Abhishek Rudra
 *
 */
@Api("ES services")
@Path(ApiConstants.V1 + ApiConstants.SERVICES)
public class ESController {

	private final Logger logger = LoggerFactory.getLogger(ESController.class);

	@Inject
	public ElasticSearchService elasticSearchService;

	@Inject
	public ElasticAdminSearchService elasticAdminSearchService;

	@Inject
	public ElasticSearchDownloadService elasticSearchDownloadService;

	@GET
	@Path(ApiConstants.PING)
	@Produces(MediaType.TEXT_PLAIN)
	public String ping() {
		return "PONG";
	}

	@POST
	@Path(ApiConstants.DATA + "/{index}/{type}/{documentId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Create Document", notes = "Returns succuess failure", response = MapQueryResponse.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Exception", response = String.class),
			@ApiResponse(code = 500, message = "ERROR", response = String.class) })

	public MapQueryResponse create(@PathParam("index") String index, @PathParam("type") String type,
			@PathParam("documentId") String documentId, @ApiParam(value = "document") MapDocument document) {

		String docString = String.valueOf(document.getDocument());
		try {
			new ObjectMapper().readValue(docString, Map.class);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build());
		}

		try {
			return elasticSearchService.create(index, type, documentId, docString);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@GET
	@Path(ApiConstants.DATA + "/{index}/{type}/{documentId}")
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Fetch Document", notes = "Returns Document", response = MapDocument.class)
	@ApiResponses(value = { @ApiResponse(code = 500, message = "ERROR", response = String.class) })

	public MapDocument fetch(@PathParam("index") String index, @PathParam("type") String type,
			@PathParam("documentId") String documentId) {

		try {
			return elasticSearchService.fetch(index, type, documentId);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@PUT
	@Path(ApiConstants.DATA + "/{index}/{type}/{documentId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Update Document", notes = "Returns Success Failur", response = MapQueryResponse.class)
	@ApiResponses(value = { @ApiResponse(code = 500, message = "ERROR", response = String.class) })

	public MapQueryResponse update(@PathParam("index") String index, @PathParam("type") String type,
			@PathParam("documentId") String documentId, @ApiParam(value = "document") Map<String, Object> document) {

		try {
			return elasticSearchService.update(index, type, documentId, document);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@DELETE
	@Path(ApiConstants.DATA + "/{index}/{type}/{documentId}")
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Delete Document", notes = "Returns Success Failure", response = MapQueryResponse.class)
	@ApiResponses(value = { @ApiResponse(code = 500, message = "ERROR", response = String.class) })

	public MapQueryResponse delete(@PathParam("index") String index, @PathParam("type") String type,
			@PathParam("documentId") String documentId) {

		try {
			return elasticSearchService.delete(index, type, documentId);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}

	}

	@POST
	@Path(ApiConstants.BULK_UPLOAD + "/{index}/{type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Bulk Upload Create Document", notes = "Returns Success Failure", response = MapQueryResponse.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 500, message = "ERROR", response = String.class) })

	public List<MapQueryResponse> bulkUpload(@PathParam("index") String index, @PathParam("type") String type,
			@ApiParam(value = "jsonArray") String jsonArray) {

		try {
			return elasticSearchService.bulkUpload(index, type, jsonArray);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@PUT
	@Path(ApiConstants.BULK_UPDATE + "/{index}/{type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Bulk Upload Update Document", notes = "Returns Success Failure", response = MapQueryResponse.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "No Documents to update", response = String.class),
			@ApiResponse(code = 500, message = "ERROR", response = String.class) })

	public List<MapQueryResponse> bulkUpdate(@PathParam("index") String index, @PathParam("type") String type,
			@ApiParam(value = "updateDocs") List<Map<String, Object>> updateDocs) {

		if (updateDocs == null)
			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST).entity("No documents to update").build());

		for (Map<String, Object> doc : updateDocs) {
			if (!doc.containsKey("id"))
				throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
						.entity("Id not present of the document to be updated").build());
		}

		try {
			return elasticSearchService.bulkUpdate(index, type, updateDocs);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@POST
	@Path(ApiConstants.TERM_SEARCH + "/{index}/{type}")
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Search a Document", notes = "Returns Document", response = MapResponse.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "key or value not specified", response = String.class),
			@ApiResponse(code = 500, message = "ERROR", response = String.class) })

	public MapResponse search(@PathParam("index") String index, @PathParam("type") String type,
			@QueryParam("key") String key, @QueryParam("value") String value,
			@QueryParam("geoAggregationField") String geoAggregationField,
			@QueryParam("geoAggegationPrecision") Integer geoAggegationPrecision,
			@ApiParam(value = "searchParam") MapSearchParams searchParams) {

		if (key == null || value == null)
			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST).entity("key or value not specified").build());

		try {
			return elasticSearchService.termSearch(index, type, key, value, searchParams, geoAggregationField,
					geoAggegationPrecision);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@POST
	@Path(ApiConstants.TERM_SEARCH + "/{index}/{type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Bool Search a Document", notes = "Returns Document", response = MapResponse.class)
	@ApiResponses(value = { @ApiResponse(code = 500, message = "ERROR", response = String.class) })

	public MapResponse boolSearch(@PathParam("index") String index, @PathParam("type") String type,
			@QueryParam("from") Integer from, @QueryParam("limit") Integer limit, @QueryParam("sortOn") String sortOn,
			@QueryParam("sortType") MapSortType sortType, @QueryParam("geoAggregationField") String geoAggregationField,
			@QueryParam("geoAggegationPrecision") Integer geoAggegationPrecision, @ApiParam(value = "query") List<MapBoolQuery> query) {

		try {
			MapSearchParams searchParams = new MapSearchParams(from, limit, sortOn, sortType);
			return elasticSearchService.boolSearch(index, type, query, searchParams, geoAggregationField,
					geoAggegationPrecision);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@POST
	@Path(ApiConstants.RANGE_SEARCH + "/{index}/{type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Range Search a Document", notes = "Returns Document", response = MapResponse.class)
	@ApiResponses(value = { @ApiResponse(code = 500, message = "ERROR", response = String.class) })

	public MapResponse rangeSearch(@PathParam("index") String index, @PathParam("type") String type,
			@QueryParam("from") Integer from, @QueryParam("limit") Integer limit, @QueryParam("sortOn") String sortOn,
			@QueryParam("sortType") MapSortType sortType, @QueryParam("geoAggregationField") String geoAggregationField,
			@QueryParam("geoAggegationPrecision") Integer geoAggegationPrecision, @ApiParam(value = "query") List<MapRangeQuery> query) {

		try {
			MapSearchParams searchParams = new MapSearchParams(from, limit, sortOn, sortType);
			return elasticSearchService.rangeSearch(index, type, query, searchParams, geoAggregationField,
					geoAggegationPrecision);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@GET
	@Path(ApiConstants.GEOHASH_AGGREGATION + "/{index}/{type}")
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Geo Hash Aggregation", notes = "Returns Document", response = MapDocument.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "ERROR", response = String.class) })

	public MapDocument geohashAggregation(@PathParam("index") String index, @PathParam("type") String type,
			@QueryParam("geoAggregationField") String field, @QueryParam("geoAggegationPrecision") Integer precision) {

		if (field == null || precision == null)
			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST).entity("Field or precision not specified").build());
		if (precision < 1 || precision > 12)
			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST).entity("Precision value must be between 1 and 12").build());

		try {
			return elasticSearchService.geohashAggregation(index, type, field, precision);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@POST
	@Path(ApiConstants.TERMS_AGGREGATION + "/{index}/{type}")
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Terms Aggregation", notes = "Returns Document", response = MapDocument.class)
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Incomplete map bounds request", response = String.class),
			@ApiResponse(code = 500, message = "ERROR", response = String.class) })

	public MapDocument termsAggregation(@PathParam("index") String index, @PathParam("type") String type,
			@QueryParam("field") String field, @QueryParam("subField") String subField,
			@QueryParam("size") Integer size, @QueryParam("locationField") String locationField,
			@ApiParam(value = "query") MapSearchQuery query) {

		if (field == null)
			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST).entity("Aggregation field cannot be empty").build());

		MapSearchParams searchParams = query.getSearchParams();
		MapBoundParams boundParams = searchParams.getMapBoundParams();
		MapBounds mapBounds = null;
		if (boundParams != null)
			mapBounds = boundParams.getBounds();

		if ((locationField != null && mapBounds == null) || (locationField == null && mapBounds != null))
			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST).entity("Incomplete map bounds request").build());

		try {
			return elasticSearchService.termsAggregation(index, type, field, subField, size, locationField, query);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@POST
	@Path(ApiConstants.AGGREGATION + "/{index}/{type}/{filter}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Aggregation for List Page", notes = "Returns Aggregated values", response = AggregationResponse.class)
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Location field not specified for bounds", response = String.class),
			@ApiResponse(code = 500, message = "ERROR", response = String.class) })

	public AggregationResponse getAggregation(@PathParam("index") String index, @PathParam("type") String type,
			@PathParam("filter") String filter, @QueryParam("geoAggregationField") String geoAggregationField,
			@ApiParam(value = "query") MapSearchQuery query) throws IOException {
		MapSearchParams searchParams = query.getSearchParams();
		MapBoundParams boundParams = searchParams.getMapBoundParams();
		MapBounds bounds = null;
		if (boundParams != null)
			bounds = boundParams.getBounds();

		if (bounds != null && geoAggregationField == null)
			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST).entity("Location field not specified for bounds").build());

		try {
			return elasticSearchService.aggregation(index, type, query, geoAggregationField, filter);

		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}

	}

	@POST
	@Path(ApiConstants.RIGHTPAN + "/{index}/{type}/{speciesName}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Aggregation for List Page", notes = "Returns Aggregated values", response = ObservationInfo.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Inappropriate Data", response = String.class) })

	public Response getObservationInfo(@PathParam("index") String index, @PathParam("type") String type,
			@PathParam("speciesName") String speciesName) throws IOException {
		try {
			ObservationInfo info = elasticSearchService.getObservationRightPan(index, type, speciesName);
			return Response.status(Status.OK).entity(info).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@POST
	@Path(ApiConstants.NEARBY + "/{index}/{type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNearByObservation(@PathParam("index") String index, @PathParam("type") String type,
			@QueryParam("lat") String lat, @QueryParam("lon") String lon) {
		try {
			Double latitude = Double.parseDouble(lat);
			Double longitude = Double.parseDouble(lon);
			List<ObservationNearBy> result = elasticSearchService.observationNearBy(index, type, latitude, longitude);

			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@POST
	@Path(ApiConstants.SEARCH + "/{index}/{type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Search for List Page", notes = "Returns List of Document", response = MapResponse.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Inappropriate Bounds Data", response = String.class),
			@ApiResponse(code = 500, message = "ERROR", response = String.class) })

	public MapResponse search(@PathParam("index") String index, @PathParam("type") String type,
			@QueryParam("geoAggregationField") String geoAggregationField,
			@QueryParam("geoAggegationPrecision") Integer geoAggegationPrecision,
			@QueryParam("onlyFilteredAggregation") Boolean onlyFilteredAggregation,
			@QueryParam("termsAggregationField") String termsAggregationField, @ApiParam(value = "query") MapSearchQuery query) {

		MapSearchParams searchParams = query.getSearchParams();
		MapBoundParams boundParams = searchParams.getMapBoundParams();
		MapBounds bounds = null;
		if (boundParams != null)
			bounds = boundParams.getBounds();

		if (onlyFilteredAggregation != null && onlyFilteredAggregation && bounds == null)
			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST).entity("Bounds not specified for filtering").build());

		if (bounds != null && geoAggregationField == null)
			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST).entity("Location field not specified for bounds").build());

		try {
			return elasticSearchService.search(index, type, query, geoAggregationField, geoAggegationPrecision,
					onlyFilteredAggregation, termsAggregationField);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@POST
	@Path(ApiConstants.DOWNLOAD + "/{index}/{type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Download of Document", notes = "Returns path of Document", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 500, message = "ERROR", response = String.class) })

	public String download(@PathParam("index") String index, @PathParam("type") String type,
			@QueryParam("geoField") String geoField, @QueryParam("filePath") String filePath,
			@QueryParam("fileType") String fileType, @ApiParam(value = "query") MapSearchQuery query) {
		try {
			return elasticSearchDownloadService.downloadSearch(index, type, query, geoField, filePath, fileType);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	// ---------- Admin Services -------------

	@GET
	@Path(ApiConstants.MAPPING + "/{index}")
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Mapping of Document", notes = "Returns Document", response = MapDocument.class)
	@ApiResponses(value = { @ApiResponse(code = 500, message = "ERROR", response = String.class) })

	public MapDocument getMapping(@PathParam("index") String index) {

		try {
			return elasticAdminSearchService.getMapping(index);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@POST
	@Path(ApiConstants.MAPPING + "/{index}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Post Mapping of Document", notes = "Returns Success Failure", response = MapQueryResponse.class)
	@ApiResponses(value = { @ApiResponse(code = 500, message = "ERROR", response = String.class) })

	public MapQueryResponse postMapping(@PathParam("index") String index, @ApiParam(value = "mapping") MapDocument mapping) {

		String docString = String.valueOf(mapping.getDocument());

		try {
			return elasticAdminSearchService.postMapping(index, docString);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@POST
	@Path(ApiConstants.INDEX_ADMIN + "/{index}/{type}")
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Create Index", notes = "Returns Success Failure", response = MapQueryResponse.class)
	@ApiResponses(value = { @ApiResponse(code = 500, message = "ERROR", response = String.class) })

	public MapQueryResponse createIndex(@PathParam("index") String index, @PathParam("type") String type) {

		try {
			return elasticAdminSearchService.createIndex(index, type);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

}
