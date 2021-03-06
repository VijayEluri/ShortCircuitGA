/*
 * Copyright (c) 2011 Ashlie Benjamin Hocking. All Rights reserved.
 */
package edu.virginia.cs.neurojet.geneticalgorithm;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test harness for NeuroJetQuickFitnessFactory
 * @author <a href="mailto:benjaminhocking@gmail.com">Ashlie Benjamin Hocking</a>
 * @since Feb 20, 2011
 */
public class NeuroJetQuickFitnessFactoryTest {

    /**
     * Test method for constructors
     */
    @Test
    public final void testNeuroJetQuickFitnessFactory() {
        NeuroJetQuickFitnessFactory f = new NeuroJetQuickFitnessFactory();
        assertEquals(false, f.generatesPostFitness());
        f = new NeuroJetQuickFitnessFactory(f);
        assertEquals(true, f.generatesPostFitness());
    }

}
