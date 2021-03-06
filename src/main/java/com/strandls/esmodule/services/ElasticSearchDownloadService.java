package com.strandls.esmodule.services;

import java.io.IOException;

import com.strandls.esmodule.models.query.MapSearchQuery;


/**
 * Download functionality
 * 
 * @author mukund
 *
 */
public interface ElasticSearchDownloadService {

	/**
	 * Download the result of search in a file
	 *
	 * @param index
	 *            the index in which to search
	 * @param type
	 *            the type in which to search
	 * @param query
	 *            the query
	 * @param geoField
	 *            the field which stores location
	 * @param filePath
	 *            the filePath where the file needs to be downloaded
	 * @param fileType
	 *            the file type. Can be CSV/TSV. Default is CSV.
	 * @return Raw path of file
	 * @throws IOException
	 *             throws {@link IOException}
	 */
	String downloadSearch(String index, String type, MapSearchQuery query, String geoField, String filePath,
			String fileType) throws IOException;
}
