package de.l3s.sz.test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class JaccardDiversity {

	private static double jsSum(List<? extends Set<String>> docs) {
		double result = 0.0D;
		for (int i = 0; i < docs.size(); i++) {
			for (int j = i + 1; j < docs.size(); j++) {
				Double curres = Double.valueOf(jaccardSimilarity(docs.get(i), docs.get(j)));
				result += curres.doubleValue();
			}

		}

		return result;
	}

	public static double getRDJ(List<? extends Set<String>> docs) {

		return (2D * jsSum(docs)) / (double) docs.size() / (double) (docs.size() - 1);
	}
	
	public static double getRDJ(Vector<Set<String>> docs) {

		return (2D * jsSum(docs)) / (double) docs.size() / (double) (docs.size() - 1);
	}

	public static double jaccardSimilarity(Set<String> d1, Set<String> d2) {
		int overlap = 0;
		Set<String> shortd;
		Set<String> longd;
		if (d1.size() > d2.size()) {
			shortd = d2;
			longd = d1;
		} else {
			shortd = d1;
			longd = d2;
		}
		HashSet<String> union = new HashSet<String>(d1);
		union.addAll(d2);
		for (Iterator<String> iterator = shortd.iterator(); iterator.hasNext();) {
			Object d = iterator.next();
			if (longd.contains(d))
				overlap++;

		}

		return ((double) overlap * 1.0D) / union.size();
	}
}
