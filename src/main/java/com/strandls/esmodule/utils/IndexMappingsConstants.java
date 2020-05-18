package com.strandls.esmodule.utils;

public enum IndexMappingsConstants {		
			mappingOnFieldNameAndCommonName(
					//"{\"settings\":{\"analysis\":{\"filter\":{\"ngram_filter\":{\"type\":\"nGram\",\"min_gram\":3,\"max_gram\":10,\"token_chars\":[\"letter\",\"digit\",\"symbol\",\"punctuation\"]}},\"analyzer\":{\"nGram_analyzer\":{\"type\":\"custom\",\"tokenizer\":\"whitespace\",\"filter\":[\"lowercase\",\"asciifolding\",\"ngram_filter\"]},\"whitespace_analyzer\":{\"type\":\"custom\",\"tokenizer\":\"whitespace\",\"filter\":[\"lowercase\",\"asciifolding\"]}},\"normalizer\":{\"case_insensitive_normalizer\":{\"type\":\"custom\",\"filter\":[\"asciifolding\",\"lowercase\"]}}}},\"mappings\":{\"extended_records\":{\"properties\":{\"name\":{\"type\":\"text\",\"analyzer\":\"nGram_analyzer\",\"search_analyzer\":\"whitespace_analyzer\",\"fields\":{\"raw\":{\"type\":\"keyword\",\"normalizer\":\"case_insensitive_normalizer\"}}},\"common_names\":{\"properties\":{\"name\":{\"type\":\"text\",\"analyzer\":\"nGram_analyzer\",\"search_analyzer\":\"whitespace_analyzer\",\"fields\":{\"raw\":{\"type\":\"keyword\",\"normalizer\":\"case_insensitive_normalizer\"}}}}}}}}}"
					"{\"settings\":{\"analysis\":{\"filter\":"
					+ "{\"ngram_filter\":{\"type\":\"nGram\",\"min_gram\":1,\"max_gram\":30,"
					+ "\"token_chars\":[\"letter\",\"digit\",\"symbol\",\"punctuation\"]}},"
					+ "\"analyzer\":{\"nGram_analyzer\":{\"type\":\"custom\",\"tokenizer\":"
					+ "\"whitespace\",\"filter\":[\"lowercase\",\"asciifolding\",\"ngram_filter\"]},"
					+ "\"whitespace_analyzer\":{\"type\":\"custom\",\"tokenizer\":\"whitespace\","
					+ "\"filter\":[\"lowercase\",\"asciifolding\"]}},"
					+ "\"normalizer\""
					+ ":{\"case_insensitive_normalizer\":{\"type\":\"custom\",\"filter\""
					+ ":[\"lowercase\"]}}}},\"mappings\":{\"extended_records\":{\"properties\""
					+ ":{\"name\":{\"type\":\"text\",\"analyzer\":\"nGram_analyzer\","
					+ "\"search_analyzer\":\"whitespace_analyzer\","
					+ "\"fields\":{\"raw\":{\"type\":\"keyword\","
					+ "\"normalizer\":\"case_insensitive_normalizer\"}}},"
					+ "\"common_names\":{\"properties\":{\"name\":{\"type\":\"text\","
					+ "\"analyzer\":\"nGram_analyzer\",\"search_analyzer\":\"whitespace_analyzer\","
					+ "\"fields\":{\"raw\":{\"type\":\"keyword\",\"normalizer\":\"case_insensitive_normalizer\"}}}}}}}}}"
					),
			mappingOfObservationIndex("{\"settings\":{\"index.mapping.total_fields.limit\":2000,"
					+ "\"analysis\":{\"filter\":{\"ngram_filter\":{\"type\":\"nGram\","
					+ "\"min_gram\":1,\"max_gram\":30,"
					+ "\"token_chars\":[\"letter\",\"digit\","
					+ "\"symbol\",\"punctuation\"]}},"
					+ "\"analyzer\":{\"nGram_analyzer\":{\"type\":\"custom\","
					+ "\"tokenizer\":\"whitespace\","
					+ "\"filter\":[\"lowercase\","
					+ "\"asciifolding\",\"ngram_filter\"]},"
					+ "\"whitespace_analyzer\":{\"type\":\"custom\","
					+ "\"tokenizer\":\"whitespace\","
					+ "\"filter\":[\"lowercase\",\"asciifolding\"]}},"
					+ "\"normalizer\":{\"case_insensitive_normalizer\":{\"type\":\"custom\","
					+ "\"filter\":[\"lowercase\"]}}}},"
					+ "\"mappings\":{\"extended_records\":"
					+ "{\"properties\":{\"all_reco_vote.scientific_name.name\":"
					+ "{\"type\":\"text\",\"analyzer\":\"nGram_analyzer\","
					+ "\"search_analyzer\":\"whitespace_analyzer\"},"
					+ "\"location_information.tahsil\":{\"type\":\"completion\"},"
					+ "\"location_information.district\":{\"type\":\"completion\"},"
					+ "\"tags.name\":{\"type\":\"completion\"},"
					+ "\"location\":{\"type\":\"geo_point\"},"
					+ "\"date\":{\"type\":\"date\",\"format\":\"yyyy-MM-dd\"},"
					+ "\"custom_fields\":{\"type\":\"object\"}}}}}");
	
	
	private String mapping;
	
	public String getMapping() {
		return mapping;
	}
	
	IndexMappingsConstants(String mapping) 
	 { 
	        this.mapping = mapping; 
	 } 
		
}
