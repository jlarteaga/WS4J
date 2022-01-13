package edu.uniba.di.lacam.kdde.ws4j.util;

import edu.uniba.di.lacam.kdde.lexical_db.ILexicalDatabase;
import edu.uniba.di.lacam.kdde.lexical_db.data.Concept;
import edu.uniba.di.lacam.kdde.lexical_db.item.POS;
import edu.uniba.di.lacam.kdde.ws4j.RelatednessCalculator;

import java.util.*;

public class MatrixCalculator {

    private static ILexicalDatabase db;

    public MatrixCalculator(ILexicalDatabase db) {
        MatrixCalculator.db = db;
    }

    public static double[][] getSimilarityMatrix(String[] words1, String[] words2, RelatednessCalculator rc) {
        double[][] result = new double[words1.length][words2.length];
        for (int i = 0; i < words1.length; i++) {
            for (int j = 0; j < words2.length; j++) {
                double score = rc.calcRelatednessOfWords(words1[i], words2[j]);
                result[i][j] = score;
            }
        }
        return result;
    }

    public static double[][] normalizeSimilarityMatrix(double[][] matrix) {
        double greatestValue = 1.0D;
        for (double[] score : matrix) {
            for (double aScore : score) {
                if (aScore > greatestValue && aScore != Double.MAX_VALUE) greatestValue = aScore;
            }
        }
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] == Double.MAX_VALUE) matrix[i][j] = 1;
                else matrix[i][j] /= greatestValue;
            }
        }
        return matrix;
    }

    public static double[][] getSimilarityMatrix(Concept[] concept1, Concept[] concepts2, RelatednessCalculator rc) {
        double[][] result = new double[concept1.length][concepts2.length];
        for (int i = 0; i < concept1.length; i++) {
            for (int j = 0; j < concepts2.length; j++) {
                double score = rc.calcRelatednessOfSynsets(concept1[i], concepts2[j]).getScore();
                result[i][j] = score;
            }
        }
        return result;
    }

    public static double[][] getNormalizedSimilarityMatrix(String[] words1, String[] words2, RelatednessCalculator rc) {
        double[][] scores = getSimilarityMatrix(words1, words2, rc);
        return normalizeSimilarityMatrix(scores);
    }

    public static double[][] getNormalizedSimilarityMatrix(Concept[] concepts1, Concept[] concepts2, RelatednessCalculator rc) {
        double[][] scores = getSimilarityMatrix(concepts1, concepts2, rc);
        return normalizeSimilarityMatrix(scores);
    }

    public static double[][] getSynonymyMatrix(String[] words1, String[] words2) {
        List<Set<String>> synonyms1 = getSynonyms(words1);
        List<Set<String>> synonyms2 = getSynonyms(words2);
        double[][] result = new double[words1.length][words2.length];
        for (int i = 0; i < words1.length; i++) {
            for (int j = 0; j < words2.length; j++) {
                String w1 = words1[i];
                String w2 = words2[j];
                if (w1.equals(w2)) {
                    result[i][j] = 1.0D;
                    continue;
                }
                Set<String> s1 = synonyms1.get(i);
                Set<String> s2 = synonyms2.get(j);
                result[i][j] = (s1.contains(w2) || s2.contains(w1)) ? 1.0D : 0.0D;
            }
        }
        return result;
    }

    private static List<Set<String>> getSynonyms(String[] words) {
        List<Set<String>> synonymsList = new ArrayList<>(words.length);
        Arrays.asList(words).forEach(aWords1 -> {
            Set<String> synonyms = new HashSet<>();
            Arrays.asList(POS.values()).forEach(pos -> db.getAllConcepts(aWords1, pos)
                    .forEach(concept -> synonyms.add(concept.getSynsetID())));
            synonymsList.add(synonyms);
        });
        return synonymsList;
    }
}
