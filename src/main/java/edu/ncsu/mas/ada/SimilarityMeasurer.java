package edu.ncsu.mas.ada;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class SimilarityMeasurer {
  
  private final Table<String, String, Double> dataTable = HashBasedTable.create();
  
  public static enum SimilarityMeasure {
    COSINE,
    EXTENDED_JACCARD
  }
  
  public static final double DELTA = 0.00001;
  
  /**
   * Input file must be comma (or semicolon) separated and of the following
   * format: (1) First value in each row is the row ID. (2) Following values in
   * each row are in pairs (column ID, column value).
   * 
   * @param file
   *          a csv file in a certain format (described above)
   * @throws IOException 
   */
  public SimilarityMeasurer(File fileName) throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
      String line = br.readLine();

      while (line != null) {
        String[] lineParts = line.split(",|;");
        String rowId = lineParts[0];
        for (int i = 1; i < lineParts.length; i += 2) {
          dataTable.put(rowId, lineParts[i], Double.parseDouble(lineParts[i + 1]));
        }
        line = br.readLine();
      }
    }
  }
  
  public Table<String, String, Double> getAllPairSim(SimilarityMeasure measure) {
    Set<String> rowKeys = dataTable.rowKeySet();
    
    Table<String, String, Double> similarityTable = HashBasedTable.create(rowKeys.size(),
        rowKeys.size());
    
    for (String rowKey1 : rowKeys) {
      for (String rowKey2 : rowKeys) {
        if (rowKey1.equals(rowKey2)) {
          similarityTable.put(rowKey1, rowKey2, 1.0);
          similarityTable.put(rowKey2, rowKey1, 1.0);
        } else {
          Double similarity = similarityTable.get(rowKey1, rowKey2);
          if (similarity == null) {
            Map<String, Double> row1Map = dataTable.row(rowKey1);
            Map<String, Double> row2Map = dataTable.row(rowKey2);
            
            switch (measure) {
            case COSINE:
              similarity = findCosineSimilarity(row1Map, row2Map);
              break;
            case EXTENDED_JACCARD:
              similarity = findExtendedJaccardSimilarity(row1Map, row2Map);
              break;
            default:
              throw new UnsupportedOperationException("This similarity measure is not supported");
            }
            
            similarityTable.put(rowKey1, rowKey2, similarity);
            similarityTable.put(rowKey2, rowKey1, similarity);
          }
        }
      }
    }

    return similarityTable;
  }

  private Double findCosineSimilarity( Map<String, Double> row1Map,  Map<String, Double> row2Map) {
    Double similarity = 0.0;
    
    Double dotProduct = findDotProduct(row1Map, row2Map);
    if (Math.abs(dotProduct) <= DELTA) {
      similarity = 0.0;
    } else {
      Double row1L2Norm = findL2Norm(row1Map);
      Double row2L2Norm = findL2Norm(row2Map);
      similarity = dotProduct / (row1L2Norm * row2L2Norm);
    }
    
    return similarity;
  }
  
  private Double findExtendedJaccardSimilarity(Map<String, Double> row1Map,
      Map<String, Double> row2Map) {
    Double similarity = 0.0;

    Double dotProduct = findDotProduct(row1Map, row2Map);
    if (Math.abs(dotProduct) <= DELTA) {
      similarity = 0.0;
    } else {
      Double row1L2Norm = findL2Norm(row1Map);
      Double row2L2Norm = findL2Norm(row2Map);
      similarity = dotProduct
          / ((row1L2Norm * row1L2Norm) + (row2L2Norm * row2L2Norm) - dotProduct);
    }

    return similarity;
  }
  
  private double findL2Norm(Map<String, Double> rowMap) {
    Double l2NormSquared = 0.0;
    for (Double val : rowMap.values()) {
      l2NormSquared += val * val;
    }
    return Math.sqrt(l2NormSquared);
  }
  
  private Double findDotProduct(Map<String, Double> row1Map, Map<String, Double> row2Map) {
    Double dotProduct = 0.0;
    for (String row1Col : row1Map.keySet()) {
      Double row2ColVal = row2Map.get(row1Col);
      if (row2ColVal != null) {
        dotProduct += row1Map.get(row1Col) * row2Map.get(row1Col);
      }
    }
    return dotProduct;
  }
}
