package com.strandls.esmodule.utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.strandls.esmodule.indexes.pojo.CommonName;
import com.strandls.esmodule.indexes.pojo.ExtendedTaxonDefinition;

public class UtilityMethods {
	
	public String getAggregationScoreScript(String score) {
		switch(score) {
		case "A": return Scripts.ActivityScore.getScript();
		case "C" : return Scripts.ContentScore.getScript();
		case "P" : return Scripts.ParticipateScore.getScript();
		default : return null;
		}
	}
	
	public String getEsIndexConstants(String index) {
		return esIndexConstants.get(index);
	}
	
	public String getEsIndexTypeConstant(String type) {
		return esIndexTypeConstant.get(type);
	}
	
	public List<String> getEsindexWithMapping(String index) {
		String indexMapping = null;
		if(index.equalsIgnoreCase("etd")) {
			indexMapping = IndexMappingsConstants.valueOf("mappingOnFieldNameAndCommonName").getMapping();
		}
		else if(index.equalsIgnoreCase("eo")) {
			indexMapping = IndexMappingsConstants.mappingOfObservationIndex.getMapping();
		}
		return new ArrayList<String>(Arrays.asList(esIndexConstants.get(index),indexMapping));
	}
	
	@SuppressWarnings("rawtypes")
	public final Class getClass(String index) {
		switch (index) {
			case "etdi": return ExtendedTaxonDefinition.class;
			default: return null;
		}
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public final List<ExtendedTaxonDefinition> rankDocument(List<ExtendedTaxonDefinition> records, String field,
			String fieldText) {
		int listIndex = 0;
		HashMap<Integer, Integer> indexScores = new HashMap<Integer, Integer>();
		List<Integer> negativeScoreIndexes = new ArrayList<Integer>();
		boolean isCanonical = false;
		if(field.equalsIgnoreCase("canonical_form"))
			isCanonical = true;
		
		for (ExtendedTaxonDefinition record : records) {
			
			int score = 0;
			boolean patternMatched = isCanonical;
			String name = record.getName();
			String status = record.getStatus();
			String position = record.getPosition();
			String speciesName = record.getSpecies_title();
			
			ArrayList<String> fieldTextSpaceSplit = new ArrayList<String>(Arrays.asList(fieldText.toLowerCase().split(" ")));
	        ArrayList<String> nameListSpaceSplit = new ArrayList<>(Arrays.asList(name.toLowerCase().split(" ")));
			
			fieldTextSpaceSplit.retainAll(nameListSpaceSplit);
			
			if(fieldTextSpaceSplit.size() >0 ) {
				
				score += fieldTextSpaceSplit.size()*5;
				patternMatched = true;
			}
			
			if (name.equalsIgnoreCase(fieldText)) {
				score += 4000;
				patternMatched = true;
			}
			
			if(name.toLowerCase().startsWith(fieldText.toLowerCase())){
				score += 2500;
				patternMatched = true;
			}
			

			if (status.equalsIgnoreCase("ACCEPTED")) {
				score += 1000;
			}

			if (position.equalsIgnoreCase("CLEAN")) {
				score += 299;
			}

			else if (position.equalsIgnoreCase("WORKING")) {
				score += 199;
			}

			else if (position.equalsIgnoreCase("RAW")) {
				score += 99;
			}
			
			if (speciesName != null) {
				score += 100;
			}
			
			indexScores.put(listIndex, score);
			if(patternMatched != true) {
				negativeScoreIndexes.add(listIndex);
				}
			listIndex ++;
		}
		
		
		indexScores.keySet().removeAll(negativeScoreIndexes);
		records.removeAll(negativeScoreIndexes);
		
		LinkedHashMap<Integer, Integer> rankedIndex = sortHashMaponValue(indexScores);
		ArrayList<Integer> orderedIndexes = new ArrayList<Integer>(rankedIndex.keySet());
		return orderDocuments(orderedIndexes, records);
	}

	
	public final List<ExtendedTaxonDefinition> rankDocumentBasedOnCommonName(List<ExtendedTaxonDefinition>records, String field, 
			String fieldText){
		
		int listIndex = 0;
		HashMap<Integer, Integer> indexScores = new HashMap<Integer, Integer>();
		
		for (ExtendedTaxonDefinition record : records) {
			int score = 0;
			int commonNameIndex = 0;
			List<CommonName> commonNames = record.getCommon_names();
			List<CommonName> matchedCommonRecords = new ArrayList<CommonName>();
			HashMap<Integer, Integer> commonNameIndexScores = new HashMap<Integer, Integer>();
			
			for (CommonName commonName :commonNames ) {
				int cscore = 0;
				 String cname = commonName.getName();
				 if(cname.equalsIgnoreCase(fieldText)) {
					 cscore +=500;
					 
				 }
				 if(cname.toLowerCase().startsWith(fieldText.toLowerCase())) {
					 cscore += 100;
				 }
				 
				 if(cname.toLowerCase().matches(".* "+fieldText+" .*")) {
					 cscore +=10;
				 }
				 
				 if(cname.toLowerCase().contains(fieldText.toLowerCase())) {
					 cscore +=1;
				 }
				 
				 if(cscore > 0) {
					 matchedCommonRecords.add(commonName);
					 commonNameIndexScores.put(commonNameIndex, cscore);
					 score += cscore;
				 }
				 commonNameIndex++; 
			}
			
			LinkedHashMap<Integer, Integer> rankedCommonIndex = sortHashMaponValue(commonNameIndexScores);
			ArrayList<Integer> orderedIndexes = new ArrayList<Integer>(rankedCommonIndex.keySet());
			commonNames = orderCommonNameRecords(orderedIndexes, commonNames);
			record.setCommon_names(commonNames);
			
			String status = record.getStatus();
			String position = record.getPosition();
			String speciesName = record.getSpecies_title();
			
			if (status.equalsIgnoreCase("ACCEPTED")) {
				score += 1000;
			}

			if (position.equalsIgnoreCase("CLEAN")) {
				score += 299;
			}

			else if (position.equalsIgnoreCase("WORKING")) {
				score += 199;
			}

			else if (position.equalsIgnoreCase("RAW")) {
				score += 99;
			}
			
			if (speciesName != null) {
				score += 100;
			}
			
			indexScores.put(listIndex, score);
			listIndex++;
		}
		
		LinkedHashMap<Integer, Integer> rankedIndex = sortHashMaponValue(indexScores);
		ArrayList<Integer> orderedIndexes = new ArrayList<Integer>(rankedIndex.keySet());
		return orderDocuments(orderedIndexes, records);
		
	}
	
	public final String getTimeWindow(String filterType) {
		LocalDate now = LocalDate.now();
		filterType = filterType.toLowerCase();
		switch(filterType) {
		case "today": // today
			return now.toString();
		case "week": //past week
			return now.minusDays(7).toString();
		case "month": // last month
			return now.minusMonths(1).toString();
		case "3month": // last three month
				return now.minusMonths(3).toString();
		case "year": // past year
			return now.minusYears(1).toString();
		}
		return null;
	}
	
	private LinkedHashMap<Integer, Integer> sortHashMaponValue(HashMap<Integer, Integer> indexScores) {
		return indexScores.entrySet().stream().sorted(Collections.reverseOrder(Entry.comparingByValue()))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}
	
	
	private List<CommonName> orderCommonNameRecords(ArrayList<Integer> orderedIndexes,
			List<CommonName> records) {
		
		List<CommonName> orderedDocuments = new ArrayList<CommonName>();
		for (Integer i : orderedIndexes) {
			CommonName commonName = records.get(i);
			orderedDocuments.add(commonName);
		}
		return orderedDocuments;
	}
	
	
	private List<ExtendedTaxonDefinition> orderDocuments(ArrayList<Integer> orderedIndexes,
			List<ExtendedTaxonDefinition> records) {

		List<ExtendedTaxonDefinition> orderedDocuments = new ArrayList<ExtendedTaxonDefinition>();
		for (Integer i : orderedIndexes) {
			ExtendedTaxonDefinition extendedTaxonDefinition = records.get(i);
			orderedDocuments.add(extendedTaxonDefinition);
		}
		return orderedDocuments;
	}
	

	
	@SuppressWarnings("serial")
	private static final HashMap<String, String> esIndexTypeConstant = new HashMap<String, String>(){
		{
			put("er","extended_records");	
		}
	};
	
	
	@SuppressWarnings("serial")
	private static final HashMap<String, String>esIndexConstants = new HashMap<String, String>(){
		{
			put("etdi", "extended_taxon_definition");
			put("eo","extended_observation");
			put("eaf","extended_activity_feed");
		}
	};
}
