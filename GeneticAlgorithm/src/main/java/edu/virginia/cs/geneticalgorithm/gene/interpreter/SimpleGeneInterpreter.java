/*
 * Copyright (c) 2010-2011 Ashlie Benjamin Hocking. All Rights reserved.
 */
package edu.virginia.cs.geneticalgorithm.gene.interpreter;

import edu.virginia.cs.common.utils.ValueGenerator;
import edu.virginia.cs.geneticalgorithm.gene.Gene;
import edu.virginia.cs.geneticalgorithm.gene.Genotype;
import edu.virginia.cs.geneticalgorithm.gene.IntervalGene;

/**
 * GeneInterpeter that uses a ValueGenerator and a gene position to generate a value
 * @author <a href="mailto:benjamin.hocking@gmail.com">Ashlie Benjamin Hocking</a>
 * @since May 28, 2010
 */
public final class SimpleGeneInterpreter implements GeneInterpreter {

    private final int _genePos;
    private final ValueGenerator _generator;

    /**
     * Constructor
     * @param genePos Which {@link Gene} in the {@link Genotype} this interpreter is for
     * @param vg {@link ValueGenerator} to use for interpreting the specified {@link Gene}
     */
    public SimpleGeneInterpreter(final int genePos, final ValueGenerator vg) {
        _genePos = genePos;
        _generator = vg;
    }

    @Override
    public String generate(final Genotype genotype) {
        final Gene g = genotype.getGene(_genePos);
        if (!(g instanceof IntervalGene)) throw new IllegalArgumentException("Gene being matched against is not an IntervalGene");
        final IntervalGene ig = (IntervalGene) g;
        return _generator.generate(ig.getValue());
    }

    /**
     * @see edu.virginia.cs.geneticalgorithm.gene.interpreter.GeneInterpreter#invert(java.lang.String, Genotype)
     */
    @Override
    public Gene invert(final String s, final Genotype genotype) {
        return new IntervalGene(_generator.invert(s));
    }

}
