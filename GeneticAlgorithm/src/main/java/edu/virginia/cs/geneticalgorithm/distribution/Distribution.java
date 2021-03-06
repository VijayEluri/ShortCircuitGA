/*
 * Copyright (c) 2010-2011 Ashlie Benjamin Hocking. All Rights reserved.
 */
package edu.virginia.cs.geneticalgorithm.distribution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.virginia.cs.geneticalgorithm.fitness.Fitness;
import edu.virginia.cs.geneticalgorithm.gene.Genotype;

/**
 * Population of {@link Genotype Genotypes} with their corresponding {@link Fitness} information.
 * @see DistributionMember
 * @author <a href="mailto:benjamin.hocking@gmail.com">Ashlie Benjamin Hocking</a>
 * @since Apr 24, 2010
 */
public class Distribution extends ArrayList<DistributionMember> {

    /**
     * Default constructor
     */
    public Distribution() {
        super();
    }

    /**
     * Copy constructor with deep copy
     * @param distribution Distribution to copy
     */
    public Distribution(final Distribution distribution) {
        super();
        for (final DistributionMember m : distribution) {
            add(new DistributionMember(m));
        }
    }

    /**
     * For finding the last member of a Distribution.
     * @return Last member of the Distribution
     */
    public DistributionMember getLast() {
        return get(size() - 1);
    }

    /**
     * Remove all but one of a group of DistributionMembers with the same {@link Genotype}.
     */
    public void removeDuplicates() {
        final List<Boolean> duplicates = new ArrayList<Boolean>(Collections.nCopies(size(), (Boolean) null));
        for (int i = 0; i < size(); ++i) {
            if (duplicates.get(i) == null) {
                duplicates.set(i, false);
                final DistributionMember cf = get(i);
                for (int j = i + 1; j < size(); ++j) {
                    if (cf.getGenotype().equals(get(j).getGenotype())) {
                        duplicates.set(j, true);
                    }
                }
            }
        }
        for (int i = size() - 1; i >= 0; --i) {
            if (duplicates.get(i)) {
                remove(i);
            }
        }
    }

    /**
     * Figures out whether all fitness values have been calculated.
     * @return Whether all fitness values have been calculated.
     */
    public boolean hasValues() {
        for (final DistributionMember m : this) {
            if (m.getValue() == null) return false;
        }
        return true;
    }

    /**
     * Adjusts the fitness values such that they add to one.
     */
    public void normalize() {
        if (!hasValues()) return; // Not ready to normalize yet
        double sum = 0.0;
        for (final DistributionMember m : this) {
            sum += m.getValue();
        }
        // Make sure it hasn't already been normalized (and that we don't divide by zero)
        if (sum > 0 && Math.abs(sum - 1) > 0.0001) {
            final Distribution copy = new Distribution(this);
            clear();
            for (final DistributionMember m : copy) {
                add(new DistributionMember(m.getValue() / sum, m.getFitnessValues(), m.getGenotype()));
            }
        }
    }
}
