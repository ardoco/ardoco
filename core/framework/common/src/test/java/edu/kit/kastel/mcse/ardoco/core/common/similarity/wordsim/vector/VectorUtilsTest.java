/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.vector;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class VectorUtilsTest {

    record Example(float[] firstVec, float[] secondVec, double expectedCosineSimilarity) {
    }

    private static final List<Example> EXAMPLES = List.of(new Example(new float[0], new float[0], 1.0), new Example(new float[] { 0.0f, 0.0f }, new float[] {
            0.0f, 0.0f }, 1.0), new Example(new float[] { 0.0f, 0.0f }, new float[] { 1.0f, 0.0f }, 0.0), new Example(new float[] { 0.0f, 0.0f }, new float[] {
                    0.0f, 1.0f }, 0.0), new Example(new float[] { 1.0f, 2.0f }, new float[] { 3.0f, 4.0f }, 0.9838699), new Example(new float[] { 345.5452f,
                            759.4141f }, new float[] { 9563.325f, 43562.53f }, 0.9778399523249), new Example(new float[] { 0.321f, 0.05f }, new float[] {
                                    331.32f, 7.343f }, 0.99125289), new Example(new float[] { 1.0f, 0.0f }, new float[] { 0.0f, 1.0f }, 0.0), new Example(
                                            new float[] { 1.0f, 0.5f }, new float[] { 0.0f, 1.0f }, 0.44721359));

    @Test
    void testFloatCosineSimilarity() {
        assertThrows(NullPointerException.class, () -> VectorUtils.cosineSimilarity(null, new double[1]));
        assertThrows(NullPointerException.class, () -> VectorUtils.cosineSimilarity(new double[1], null));
        assertThrows(IllegalArgumentException.class, () -> VectorUtils.cosineSimilarity(new double[1], new double[2]));

        for (Example example : EXAMPLES) {
            double result = VectorUtils.cosineSimilarity(example.firstVec, example.secondVec);
            assertEquals(example.expectedCosineSimilarity, result, 0.005);
        }
    }

    @Test
    void testDoubleCosineSimilarity() {
        assertThrows(NullPointerException.class, () -> VectorUtils.cosineSimilarity(null, new float[1]));
        assertThrows(NullPointerException.class, () -> VectorUtils.cosineSimilarity(new float[1], null));
        assertThrows(IllegalArgumentException.class, () -> VectorUtils.cosineSimilarity(new float[1], new float[2]));

        for (Example example : EXAMPLES) {
            double[] firstVecDouble = new double[example.firstVec.length];
            double[] secondVecDouble = new double[example.secondVec.length];

            for (int i = 0; i < example.firstVec.length; i++) {
                firstVecDouble[i] = example.firstVec[i];
                secondVecDouble[i] = example.secondVec[i];
            }

            double result = VectorUtils.cosineSimilarity(firstVecDouble, secondVecDouble);
            assertEquals(example.expectedCosineSimilarity, result, 0.005);
        }
    }

    @Test
    void testIsZero() {
        assertThrows(NullPointerException.class, () -> VectorUtils.isZero((float[]) null));
        assertThrows(NullPointerException.class, () -> VectorUtils.isZero((double[]) null));

        assertTrue(VectorUtils.isZero(new float[0]));
        assertTrue(VectorUtils.isZero(new double[0]));
        assertTrue(VectorUtils.isZero(new float[] { 0.0f }));
        assertTrue(VectorUtils.isZero(new float[] { 0.0f, 0.0f, 0.0f }));
        assertTrue(VectorUtils.isZero(new double[] { 0.0 }));
        assertTrue(VectorUtils.isZero(new double[] { 0.0, 0.0, 0.0 }));
        assertFalse(VectorUtils.isZero(new float[] { 1.0f, 3.0f }));
        assertFalse(VectorUtils.isZero(new double[] { 1.0, 3.0 }));
    }

    @Test
    void testAdd() {
        assertThrows(NullPointerException.class, () -> VectorUtils.add(null, new double[0]));
        assertThrows(NullPointerException.class, () -> VectorUtils.add(new double[0], null));
        assertThrows(IllegalArgumentException.class, () -> VectorUtils.add(new double[1], new double[2]));

        double[] result = { 1.0, 2.0 };
        VectorUtils.add(result, new double[] { 3.0, 4.0 });
        assertArrayEquals(new double[] { 4.0, 6.0 }, result);
    }

    @Test
    void testScale() {
        assertThrows(NullPointerException.class, () -> VectorUtils.scale(null, 1.0));

        double[] result = { 1.0, 2.0 };
        VectorUtils.scale(result, 2.0);
        assertArrayEquals(new double[] { 2.0, 4.0 }, result);
    }

}
