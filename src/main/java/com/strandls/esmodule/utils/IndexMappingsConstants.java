package com.strandls.esmodule.utils;

public enum IndexMappingsConstants {		
			mappingOnFieldNameAndCommonName(
					//"{\"settings\":{\"analysis\":{\"filter\":{\"ngram_filter\":{\"type\":\"nGram\",\"min_gram\":3,\"max_gram\":10,\"token_chars\":[\"letter\",\"digit\",\"symbol\",\"punctuation\"]}},\"analyzer\":{\"nGram_analyzer\":{\"type\":\"custom\",\"tokenizer\":\"whitespace\",\"filter\":[\"lowercase\",\"asciifolding\",\"ngram_filter\"]},\"whitespace_analyzer\":{\"type\":\"custom\",\"tokenizer\":\"whitespace\",\"filter\":[\"lowercase\",\"asciifolding\"]}},\"normalizer\":{\"case_insensitive_normalizer\":{\"type\":\"custom\",\"filter\":[\"asciifolding\",\"lowercase\"]}}}},\"mappings\":{\"extended_records\":{\"properties\":{\"name\":{\"type\":\"text\",\"analyzer\":\"nGram_analyzer\",\"search_analyzer\":\"whitespace_analyzer\",\"fields\":{\"raw\":{\"type\":\"keyword\",\"normalizer\":\"case_insensitive_normalizer\"}}},\"common_names\":{\"properties\":{\"name\":{\"type\":\"text\",\"analyzer\":\"nGram_analyzer\",\"search_analyzer\":\"whitespace_analyzer\",\"fields\":{\"raw\":{\"type\":\"keyword\",\"normalizer\":\"case_insensitive_normalizer\"}}}}}}}}}"
					"{\"settings\":{\"analysis\":{\"filter\":{\"ngram_filter\":{\"type\":\"nGram\",\"min_gram\":3,\"max_gram\":10,\"token_chars\":[\"letter\",\"digit\",\"symbol\",\"punctuation\"]}},\"analyzer\":{\"nGram_analyzer\":{\"type\":\"custom\",\"tokenizer\":\"whitespace\",\"filter\":[\"lowercase\",\"asciifolding\",\"ngram_filter\"]},\"whitespace_analyzer\":{\"type\":\"custom\",\"tokenizer\":\"whitespace\",\"filter\":[\"lowercase\",\"asciifolding\"]}},\"normalizer\":{\"case_insensitive_normalizer\":{\"type\":\"custom\",\"filter\":[\"lowercase\"]}}}},\"mappings\":{\"extended_records\":{\"properties\":{\"name\":{\"type\":\"text\",\"analyzer\":\"nGram_analyzer\",\"search_analyzer\":\"whitespace_analyzer\",\"fields\":{\"raw\":{\"type\":\"keyword\",\"normalizer\":\"case_insensitive_normalizer\"}}},\"common_names\":{\"properties\":{\"name\":{\"type\":\"text\",\"analyzer\":\"nGram_analyzer\",\"search_analyzer\":\"whitespace_analyzer\",\"fields\":{\"raw\":{\"type\":\"keyword\",\"normalizer\":\"case_insensitive_normalizer\"}}}}}}}}}"

					
					)
			;
	
	
	private String mapping;
	
	public String getMapping() {
		return this.mapping;
	}
	
	 private IndexMappingsConstants(String mapping) 
	    { 
	        this.mapping = mapping; 
	    } 
		
}
