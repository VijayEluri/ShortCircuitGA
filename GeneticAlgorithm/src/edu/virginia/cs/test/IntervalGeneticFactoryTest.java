/*
 * Copyright (c) 2010 Ashlie Benjamin Hocking. All Rights reserved.
 */
package edu.virginia.cs.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import edu.virginia.cs.common.SingleItemList;
import edu.virginia.cs.geneticalgorithm.Fitness;
import edu.virginia.cs.geneticalgorithm.Gene;
import edu.virginia.cs.geneticalgorithm.GeneticFactory;
import edu.virginia.cs.geneticalgorithm.Genotype;
import edu.virginia.cs.geneticalgorithm.IntervalGene;
import edu.virginia.cs.geneticalgorithm.IntervalGeneticFactory;
import edu.virginia.cs.geneticalgorithm.Reproduction;

/**
 * TODO Add description
 * @author <a href="mailto:benjamin.hocking@gmail.com">Ashlie Benjamin Hocking</a>
 * @since Apr 26, 2010
 */
public final class IntervalGeneticFactoryTest {

    private final Fitness _fitFn = new TrivialIntervalFitness();
    private static int POP_SIZE = 20;
    private static int GENOTYPE_SIZE = 5;
    private static int NUM_GENERATIONS = 20;

    private class TrivialIntervalFitness implements Fitness {

        /**
         * @see edu.virginia.cs.geneticalgorithm.Fitness#fitness(edu.virginia.cs.geneticalgorithm.Genotype)
         */
        @Override
        public List<Double> fitness(final Genotype individual) {
            double retval = 0;
            for (final Gene g : individual) {
                retval += ((IntervalGene) g).getValue();
            }
            return new SingleItemList<Double>(retval);
        }

    }

    @Test
    public void mainTest() {
        final double tolerance = 1E-8;
        final long seed = 1;
        final GeneticFactory factory = new IntervalGeneticFactory(seed);
        List<Genotype> population = factory.createPopulation(POP_SIZE, GENOTYPE_SIZE);
        boolean allowDuplicates = true;
        Reproduction reproduction = new Reproduction(allowDuplicates);
        for (int i = 0; i < NUM_GENERATIONS; ++i) {
            population = reproduction.reproduce(population, _fitFn, factory.getSelectFunction(), factory.getCrossoverFunction());
        }
        Assert.assertEquals(3.793964434630042, reproduction.getBestFits().get(0), tolerance);
        // As POP_SIZE -> inf, you would expect reproduction.getMeanFits().get(0) -> GENOTYPE_SIZE / 2 (i.e., 2.5)
        Assert.assertEquals(2.4418757761918597, reproduction.getMeanFits().get(0), tolerance);
        Assert.assertEquals(4.223457345238561, reproduction.getBestFit(), tolerance);
        Assert.assertEquals(3.4821626627095483, reproduction.getMeanFit(), tolerance);
        allowDuplicates = false;
        reproduction = new Reproduction(allowDuplicates);
        for (int i = 0; i < NUM_GENERATIONS; ++i) {
            population = reproduction.reproduce(population, _fitFn, factory.getSelectFunction(), factory.getCrossoverFunction());
        }
        Assert.assertEquals(4.354381409706591, reproduction.getBestFit(), tolerance);
        Assert.assertEquals(3.6366730042344537, reproduction.getMeanFit(), tolerance);
    }
}