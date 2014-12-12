package edu.ncsu.mas.ada;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Table;

import edu.ncsu.mas.ada.SimilarityMeasurer.SimilarityMeasure;

public class SimilarityMeasurerTest {

  private SimilarityMeasurer simMeasurer;

  @Before
  public void setUpClass() {
    try {
      URL url = this.getClass().getResource("/user_grid.csv");
      File file = new File(url.getFile());
      if (!file.exists()) {
        fail("File not found");
      }

      simMeasurer = new SimilarityMeasurer(file);
    } catch (IOException e) {
      fail("File reading failed");
    }
  }

  @Test
  public void testCosine() {
    Table<String, String, Double> simTable = simMeasurer.getAllPairSim(SimilarityMeasure.COSINE);

    assertEquals(1.0, simTable.get("u1", "u1"), SimilarityMeasurer.DELTA);
    assertEquals(1.0, simTable.get("u1", "u6"), SimilarityMeasurer.DELTA);
    assertEquals(0.0, simTable.get("u3", "u5"), SimilarityMeasurer.DELTA);
    assertEquals(-1.0, simTable.get("u3", "u4"), SimilarityMeasurer.DELTA);
    assertEquals(1 / Math.sqrt(2), simTable.get("u7", "u8"), SimilarityMeasurer.DELTA);
    assertEquals(simTable.get("u1", "u2"), simTable.get("u2", "u1"), SimilarityMeasurer.DELTA);
  }

  @Test
  public void testExtendedJaccard() {
    Table<String, String, Double> simTable = simMeasurer
        .getAllPairSim(SimilarityMeasure.EXTENDED_JACCARD);

    assertEquals(1.0, simTable.get("u1", "u1"), SimilarityMeasurer.DELTA);
    assertEquals(1.0, simTable.get("u1", "u6"), SimilarityMeasurer.DELTA);
    assertEquals(0.0, simTable.get("u3", "u5"), SimilarityMeasurer.DELTA);
    assertEquals(-1.0 / 3.0, simTable.get("u3", "u4"), SimilarityMeasurer.DELTA);
    assertEquals(0.5, simTable.get("u7", "u8"), SimilarityMeasurer.DELTA);
    assertEquals(simTable.get("u1", "u2"), simTable.get("u2", "u1"), SimilarityMeasurer.DELTA);
  }

}
