/*
 * Copyright (c) 2010-2011 Ashlie Benjamin Hocking. All Rights reserved.
 */
package edu.virginia.cs.geneticalgorithm.reproduction;

import edu.virginia.cs.common.utils.OrderedPair;
import edu.virginia.cs.common.utils.Pair;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Semaphore;

import edu.virginia.cs.common.utils.UnorderedPair;
import edu.virginia.cs.data.History;
import edu.virginia.cs.geneticalgorithm.crossover.Crossover;
import edu.virginia.cs.geneticalgorithm.distribution.Distribution;
import edu.virginia.cs.geneticalgorithm.distribution.DistributionMember;
import edu.virginia.cs.geneticalgorithm.fitness.Fitness;
import edu.virginia.cs.geneticalgorithm.fitness.FitnessFactory;
import edu.virginia.cs.geneticalgorithm.fitness.ShortCircuitFitness;
import edu.virginia.cs.geneticalgorithm.gene.Genotype;
import edu.virginia.cs.geneticalgorithm.select.Select;
import edu.virginia.cs.neurojet.geneticalgorithm.NeuroJetTraceFitness;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class governing how a population of individuals in a genetic algorithm reproduce. It requires the specification of a
 * {@link Fitness} class, a {@link Select} class, and a {@link Crossover} class.
 * @author <a href="mailto:benjamin.hocking@gmail.com">Ashlie Benjamin Hocking</a>
 * @since Apr 24, 2010
 */
public final class Reproduction {

    private final boolean _allowDuplicates;
    private final boolean _keepAllHistory;
    private History<Distribution> _history;
    private final List<List<Double>> _bestFits = new ArrayList<List<Double>>();
    private final List<Double> _meanFits = new ArrayList<Double>();
    private final List<Distribution> _popHist = new ArrayList<Distribution>();
    private int _numElites = 0;
    private int _currentGeneration = 0;
    private File _endPrepareAction = null;

    /**
     * DEBUG_LEVEL controls detail of messages to standard out (0 is quiet)
     */
    public static int DEBUG_LEVEL = 1;
    private static Semaphore _monitor = new Semaphore(1);

    /**
     * Sets the number of concurrent processes to allow from this class
     * @param numProcesses Number of concurrent processes to allow from this class
     */
    public static void SetNumProcesses(final int numProcesses) {
        _monitor = new Semaphore(numProcesses);
    }

    /**
     * Constructor specifying whether to allow duplicates and whether to keep all history (requires more memory)
     * @param allowDuplicates Whether to allow duplicate individuals in a population
     * @param keepAllHistory Whether to retain history from generation to generation
     */
    public Reproduction(final boolean allowDuplicates, final boolean keepAllHistory) {
        _allowDuplicates = allowDuplicates;
        _keepAllHistory = keepAllHistory;
    }

    /**
     * Uses an existing population to find the next generation of the population.
     * @param population Current generation ({@link java.util.List List} of {@link Genotype Genotypes}).
     * @param fitFactory {@link FitnessFactory} factory used to determine which members reproduce and how well
     * @param selFn {@link Select} function used for selecting individuals to reproduce
     * @param xFn {@link Crossover} function used for creating children from parent {@link Genotype Genotypes}
     * @return The new generation
     */
    public List<Genotype> reproduce(final List<Genotype> population, final FitnessFactory fitFactory, final Select selFn,
                                    final Crossover xFn) {
        return reproduce(population, population.size(), fitFactory, selFn, xFn);
    }

    /**
     * Uses an existing population to find the next generation of the population.
     * @param population Current generation ({@link java.util.List List} of {@link Genotype Genotypes}).
     * @param newPopSize Population size of the new population (allows the population to shrink or grow).
     * @param fitFactory {@link Fitness} function used to determine which members reproduce and how well
     * @param selFn {@link Select} function used for selecting individuals to reproduce
     * @param xFn {@link Crossover} function used for creating children from parent {@link Genotype Genotypes}
     * @return The new generation
     */
    public List<Genotype> reproduce(final List<Genotype> population, final int newPopSize, final FitnessFactory fitFactory,
                                    final Select selFn, final Crossover xFn) {
        ++_currentGeneration;
        double totalFit = 0;
        double bestFit = 0; // Best is maximal, and all fitness values need to be positive
        List<Double> bestFitList = new ArrayList<Double>();
        final Distribution distribution = new Distribution();
        int ctr = 0;
        String bestDesc = "";
        if (DEBUG_LEVEL == 1) System.out.print("Evaluating individual:");
        List<Pair<Genotype, Fitness>> fitnesses = new ArrayList<Pair<Genotype, Fitness>>();
        for (final Genotype i : population) {
            final Fitness fitFn = fitFactory.createFitness(i);
            fitnesses.add(new OrderedPair<Genotype, Fitness>(i, fitFn));
            fitFn.prepare();
        }
        endPrepare();
        for (final Pair<Genotype, Fitness> pair : fitnesses) {
            if (DEBUG_LEVEL > 1) System.out.println("Finding fitness of individual #" + ++ctr);
            if (DEBUG_LEVEL == 1) System.out.print(" " + ++ctr);
            try {
                _monitor.acquire();
            }
            catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
            final Genotype i = pair.getFirst();
            final Fitness fitFn = pair.getLast();
            final List<Double> fitList = fitFn.fitnessValues();
            final double fit = fitFn.totalFitness();
            // TODO: Fix this hack
            Fitness workingFitFn = fitFn;
            if (workingFitFn instanceof ShortCircuitFitness) {
                workingFitFn = ((ShortCircuitFitness) workingFitFn).getPostFitness();
            }
            if (workingFitFn instanceof NeuroJetTraceFitness) {
                try {
                    final File workingDir = ((NeuroJetTraceFitness) workingFitFn).getWorkingDir();
                    final File generationFile = new File(workingDir, "generations.dat");
                    final StringBuilder priorGenerations = new StringBuilder();
                    boolean alreadyAccountedFor = false;
                    if (generationFile.exists()) {
                        final BufferedReader reader = new BufferedReader(new FileReader(generationFile));
                        final String line = reader.readLine();
                        priorGenerations.append(line);
                        final String[] generations = priorGenerations.toString().split(" ");
                        alreadyAccountedFor = Arrays.asList(generations).contains(String.valueOf(_currentGeneration));
                        if (!alreadyAccountedFor) {
                            priorGenerations.append(" ");
                        }
                        reader.close();
                    }
                    if (!alreadyAccountedFor) {
                        priorGenerations.append(String.valueOf(_currentGeneration));
                        PrintStream out = null;
                        try {
                            out = new PrintStream(new FileOutputStream(generationFile));
                            out.println(priorGenerations.toString());
                        }
                        finally {
                            out.close();
                        }
                    }
                }
                catch (final Exception e) {
                    throw new RuntimeException("Runtime exception", e);
                }
            }
            _monitor.release();
            if (DEBUG_LEVEL > 1) System.out.println("\tfitness: " + fit);
            distribution.add(new DistributionMember(fit, fitList, i));
            totalFit += fit;
            if (fit > bestFit) {
                bestFit = fit;
                bestFitList = fitList;
                bestDesc = fitFn.toString();
            }
        }
        if (DEBUG_LEVEL == 1) System.out.println(" ... done");
        if (DEBUG_LEVEL == 1) System.out.println("\tBest = " + bestDesc);
        // Calculate statistics
        final List<Double> best = new ArrayList<Double>();
        best.add(bestFit);
        best.addAll(bestFitList);
        _bestFits.add(best);
        _meanFits.add(Double.valueOf(totalFit / population.size()));
        // TODO: Add good test for disallowing duplicates
        // TODO: Encapsulate disallowing of duplicates (see above)
        final Collection<Genotype> retval = _allowDuplicates ? new ArrayList<Genotype>() : new HashSet<Genotype>();
        // Sorts with most fit members first
        Collections.sort(distribution);
        for (int i = 0; i < _numElites; ++i) {
            retval.add(distribution.get(i).getGenotype().clone());
        }
        while (retval.size() < newPopSize) {
            final Genotype mom = selFn.select(distribution);
            final Genotype dad = selFn.select(distribution);
            final UnorderedPair<Genotype> kids = xFn.crossover(mom, dad);
            retval.add(kids.getFirst());
            if (retval.size() < newPopSize) {
                retval.add(kids.getLast());
            }
        }
        if (_keepAllHistory) {
            _popHist.add(new Distribution(distribution));
        }
        return _allowDuplicates ? (List<Genotype>) retval : new ArrayList<Genotype>(retval);
    }

    /**
     * Set the number of individuals to preserve (clone) from one generation to the next.
     * @param numElites Number of individuals to preserve (clone) from one generation to the next.
     */
    public void setNumElites(final int numElites) {
        _numElites = numElites;
    }

    /**
     * Set the number of individuals to preserve (clone) from one generation to the next.
     * @return Number of individuals to preserve (clone) from one generation to the next.
     */
    public int getNumElites() {
        return _numElites;
    }

    /**
     * @return Best fitness for the current generation
     */
    public List<Double> getBestFit() {
        return _bestFits.get(_bestFits.size() - 1);
    }

    /**
     * @return Copy of history of best fitness for each generation
     */
    public List<List<Double>> getBestFits() {
        return new ArrayList<List<Double>>(_bestFits);
    }

    /**
     * @return The entire population history, including {@link Fitness} and {@link Genotype} information
     */
    public List<Distribution> getHistory() {
        return _popHist;
    }

    /**
     * @return Mean fitness of the current generation
     */
    public double getMeanFit() {
        return _meanFits.get(_meanFits.size() - 1);
    }

    /**
     * @return Copy of history of mean fitness for each generation
     */
    public List<Double> getMeanFits() {
        return new ArrayList<Double>(_meanFits);
    }

    public void setEndPrepareAction(File PrepareAction) {
        _endPrepareAction = PrepareAction;
    }

    private void endPrepare() {
        if (_endPrepareAction != null) {
            try {
                final List<String> command = new ArrayList<String>();
                command.add(_endPrepareAction.getCanonicalPath());
                final ProcessBuilder builder = new ProcessBuilder(command);
                builder.directory(_endPrepareAction.getParentFile());
                Process process = builder.start();
                final BufferedReader out = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = out.readLine()) != null) {
                }
                final BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                while ((line = err.readLine()) != null) {
                }
            }
            catch (IOException ex) {
                Logger.getLogger(Reproduction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
