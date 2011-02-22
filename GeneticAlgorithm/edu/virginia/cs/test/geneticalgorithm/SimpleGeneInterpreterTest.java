/*
 * Copyright (c) 2011 Ashlie Benjamin Hocking. All Rights reserved.
 */
package edu.virginia.cs.test.geneticalgorithm;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.virginia.cs.common.utils.IntegerValueGenerator;
import edu.virginia.cs.geneticalgorithm.Genotype;
import edu.virginia.cs.geneticalgorithm.IntervalGene;
import edu.virginia.cs.geneticalgorithm.SimpleGeneInterpreter;
import edu.virginia.cs.test.common.utils.IntegerValueGeneratorTest;

/**
 * Test harness for SimpleGeneInterpreter
 * @author <a href="mailto:benjaminhocking@gmail.com">Ashlie Benjamin Hocking</a>
 * @since Feb 20, 2011
 */
public class SimpleGeneInterpreterTest {

    /**
     * Test method for
     * {@link edu.virginia.cs.geneticalgorithm.SimpleGeneInterpreter#generate(edu.virginia.cs.geneticalgorithm.Genotype)}.
     */
    @Test
    public final void testGenerate() {
        final IntegerValueGenerator gen = IntegerValueGeneratorTest.generateIntegerValueGenerator();
        final SimpleGeneInterpreter interp = new SimpleGeneInterpreter(0, gen);
        Genotype g = StandardGenotypeTest.createStandardIntervalGenotype(10, 0.5);
        assertEquals("6", interp.generate(g));
        g = StandardGenotypeTest.createStandardGenotype(10);
        try {
            interp.generate(g);
            fail("Cannot interpret using standard genes");
        }
        catch (final IllegalArgumentException e) {
            assertEquals("Gene being matched against is not an IntervalGene", e.getMessage());
        }
    }

    /**
     * Test method for
     * {@link edu.virginia.cs.geneticalgorithm.SimpleGeneInterpreter#invert(java.lang.String, edu.virginia.cs.geneticalgorithm.Genotype)}
     * .
     */
    @Test
    public final void testInvert() {
        final IntegerValueGenerator gen = IntegerValueGeneratorTest.generateIntegerValueGenerator();
        final SimpleGeneInterpreter interp = new SimpleGeneInterpreter(0, gen);
        final Genotype g = StandardGenotypeTest.createStandardIntervalGenotype(10, 0.5);
        assertEquals(new IntervalGene(0.55), interp.invert("6", g));
    }

}