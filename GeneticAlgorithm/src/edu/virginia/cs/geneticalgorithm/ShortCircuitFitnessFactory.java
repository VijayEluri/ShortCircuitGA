/*
 * Copyright (c) 2010-2011 Ashlie Benjamin Hocking. All Rights reserved.
 */
package edu.virginia.cs.geneticalgorithm;

import java.util.List;

/**
 * Creates fitness function using two other fitness function factories
 * @author <a href="mailto:benjamin.hocking@gmail.com">Ashlie Benjamin Hocking</a>
 * @since Nov 20, 2010
 */
public class ShortCircuitFitnessFactory implements FitnessFactory {

    private final ProxyFitnessFactory _preFit;
    private final List<Double> _preThreshold;
    private final FitnessFactory _postFit;
    private final int _postFitLen;
    private double _postScale = 1.0; // Amount to scale post fitness by
    final boolean _useThresholdAsLimit = true;

    /**
     * @param preFactory {@link FitnessFactory} that generates {@link Fitness} functions requiring less time to run than those
     * generated by postFactory and can often act as a reasonable proxy
     * @param preThreshold {@link java.util.List List} of threshold values for multi-objective fitness which must all be passed for
     * the postFit {@link Fitness} function to be evaluated
     * @param postFactory {@link FitnessFactory} that generates {@link Fitness} function to run if threshold is met
     * @param postFitLen Number of fitness values returned by the postFit {@link Fitness} function
     */
    public ShortCircuitFitnessFactory(final ProxyFitnessFactory preFactory, final List<Double> preThreshold,
                                      final FitnessFactory postFactory, final int postFitLen) {
        _preFit = preFactory;
        _preThreshold = preThreshold;
        _postFit = postFactory;
        _postFitLen = postFitLen;
        if (postFactory == null && !preFactory.generatesPostFitness())
            throw new RuntimeException("If the proxy fitness factory cannot generate a post "
                                       + "fitness function, one must be provided!");
    }

    /**
     * @param preFactory {@link FitnessFactory} that generates {@link Fitness} functions requiring less time to run than those
     * generated by postFactory and can often act as a reasonable proxy
     * @param preThreshold {@link java.util.List List} of threshold values for multi-objective fitness which must all be passed for
     * the postFit {@link Fitness} function to be evaluated
     * @param postFactory {@link FitnessFactory} that generates {@link Fitness} function to run if threshold is met
     */
    public ShortCircuitFitnessFactory(final ProxyFitnessFactory preFactory, final List<Double> preThreshold,
                                      final FitnessFactory postFactory) {
        this(preFactory, preThreshold, postFactory, 1);
    }

    /**
     * @param preFactory {@link FitnessFactory} that generates {@link Fitness} functions requiring less time to run than those
     * generated by postFactory and can often act as a reasonable proxy
     * @param preThreshold {@link java.util.List List} of threshold values for multi-objective fitness which must all be passed for
     * the postFit {@link Fitness} function to be evaluated
     */
    public ShortCircuitFitnessFactory(final ProxyFitnessFactory preFactory, final List<Double> preThreshold) {
        this(preFactory, preThreshold, null, 1);
    }

    /**
     * @param preFactory {@link FitnessFactory} that generates {@link Fitness} functions requiring less time to run than those
     * generated by postFactory and can often act as a reasonable proxy
     * @param preThreshold {@link java.util.List List} of threshold values for multi-objective fitness which must all be passed for
     * the postFit {@link Fitness} function to be evaluated
     * @param postFitLen Number of fitness values returned by the postFit {@link Fitness} function
     */
    public ShortCircuitFitnessFactory(final ProxyFitnessFactory preFactory, final List<Double> preThreshold, final int postFitLen) {
        this(preFactory, preThreshold, null, postFitLen);
    }

    /**
     * Sets the amount to scale the postFit {@link Fitness} function by. This can be used to ensure that the results of the fitness
     * function are more important than its proxy.
     * @param postScale Amount to scale the postFit {@link Fitness} function by
     */
    public void setPostScale(final double postScale) {
        _postScale = postScale;
    }

    /**
     * @see edu.virginia.cs.geneticalgorithm.FitnessFactory#createFitness(edu.virginia.cs.geneticalgorithm.Genotype)
     */
    @Override
    public Fitness createFitness(final Genotype individual) {
        final ProxyFitness preFit = _preFit.createFitness(individual);
        final Fitness postFit = preFit.generatesPostFitness() ? preFit.getPostFitness() : _postFit.createFitness(individual);
        final ShortCircuitFitness retval = new ShortCircuitFitness(preFit, _preThreshold, postFit, _postFitLen);
        retval.setPostScale(_postScale);
        return retval;
    }
}
