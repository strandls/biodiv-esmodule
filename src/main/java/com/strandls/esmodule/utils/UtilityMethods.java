package com.strandls.esmodule.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.strandls.esmodule.indexes.pojo.ExtendedTaxonDefinition;

public class UtilityMethods {

	public final List<ExtendedTaxonDefinition> rankDocument(List<ExtendedTaxonDefinition> records, String field,
			String fieldText) {
		int listIndex = 0;
		HashMap<Integer, Integer> indexScores = new HashMap<Integer, Integer>();

		for (ExtendedTaxonDefinition record : records) {
			int score = 0;
			String name = record.getName();
			String status = record.getStatus();
			String position = record.getPosition();
			String speciesName = record.getSpecies_title();
			score += 1000 - name.indexOf(fieldText);

			if (name.equalsIgnoreCase(fieldText)) {
				score += 100;
			}

			if (status.equalsIgnoreCase("ACCEPTED")) {
				score += 100;
			}

			if (position.equalsIgnoreCase("CLEAN")) {
				score += 300;
			}

			else if (position.equalsIgnoreCase("WORKING")) {
				score += 200;
			}

			else if (position.equalsIgnoreCase("RAW")) {
				score += 100;
			}

			if (speciesName != null) {
				score += 100;
			}
			indexScores.put(listIndex, score);
			listIndex += 1;
		}
		LinkedHashMap<Integer, Integer> rankedIndex = sortHashMaponValue(indexScores);
		ArrayList<Integer> orderedIndexes = new ArrayList<Integer>(rankedIndex.keySet());
		return orderDocuments(orderedIndexes, records);
	}

	private LinkedHashMap<Integer, Integer> sortHashMaponValue(HashMap<Integer, Integer> indexScores) {
		return indexScores.entrySet().stream().sorted(Collections.reverseOrder(Entry.comparingByValue()))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
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
}
