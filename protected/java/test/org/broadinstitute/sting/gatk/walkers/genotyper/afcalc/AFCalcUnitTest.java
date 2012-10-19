package org.broadinstitute.sting.gatk.walkers.genotyper.afcalc;

import org.broadinstitute.sting.BaseTest;
import org.broadinstitute.sting.gatk.walkers.genotyper.UnifiedGenotyperEngine;
import org.broadinstitute.sting.utils.MathUtils;
import org.broadinstitute.sting.utils.QualityUtils;
import org.broadinstitute.sting.utils.Utils;
import org.broadinstitute.sting.utils.variantcontext.*;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;


public class AFCalcUnitTest extends BaseTest {
    static Allele A = Allele.create("A", true);
    static Allele C = Allele.create("C");
    static Allele G = Allele.create("G");

    static int sampleNameCounter = 0;
    static Genotype AA1, AB1, BB1, NON_INFORMATIVE1;
    static Genotype AA2, AB2, AC2, BB2, BC2, CC2, NON_INFORMATIVE2;
    final double[] FLAT_3SAMPLE_PRIORS = MathUtils.normalizeFromLog10(new double[2*3+1], true);  // flat priors

    final private static boolean INCLUDE_BIALLELIC = true;
    final private static boolean INCLUDE_TRIALLELIC = true;
    final private static boolean Guillermo_FIXME = false; // TODO -- can only be enabled when GdA fixes bug
    final private static boolean DEBUG_ONLY = false;

    @BeforeSuite
    public void before() {
        AA1 = makePL(Arrays.asList(A, A), 0, 20, 20);
        AB1 = makePL(Arrays.asList(A, C), 20, 0, 20);
        BB1 = makePL(Arrays.asList(C, C), 20, 20, 0);
        NON_INFORMATIVE1 = makePL(Arrays.asList(Allele.NO_CALL, Allele.NO_CALL), 0, 0, 0);

        AA2 = makePL(Arrays.asList(A, A), 0, 20, 20, 20, 20, 20);
        AB2 = makePL(Arrays.asList(A, C), 20, 0, 20, 20, 20, 20);
        BB2 = makePL(Arrays.asList(C, C), 20, 20, 0, 20, 20, 20);
        AC2 = makePL(Arrays.asList(A, G), 20, 20, 20, 0, 20, 20);
        BC2 = makePL(Arrays.asList(C, G), 20, 20, 20, 20, 0, 20);
        CC2 = makePL(Arrays.asList(G, G), 20, 20, 20, 20, 20, 0);
        NON_INFORMATIVE2 = makePL(Arrays.asList(Allele.NO_CALL, Allele.NO_CALL), 0, 0, 0, 0, 0, 0);
    }

    protected static Genotype makePL(final List<Allele> expectedGT, int ... pls) {
        GenotypeBuilder gb = new GenotypeBuilder("sample" + sampleNameCounter++);
        gb.alleles(expectedGT);
        gb.PL(pls);
        return gb.make();
    }

    private class GetGLsTest extends TestDataProvider {
        GenotypesContext GLs;
        int numAltAlleles;
        final AFCalc calc;
        final int[] expectedACs;
        final double[] priors;
        final String priorName;

        private GetGLsTest(final AFCalc calc, int numAltAlleles, List<Genotype> arg, final double[] priors, final String priorName) {
            super(GetGLsTest.class);
            GLs = GenotypesContext.create(new ArrayList<Genotype>(arg));
            this.numAltAlleles = numAltAlleles;
            this.calc = calc;
            this.priors = priors;
            this.priorName = priorName;

            expectedACs = new int[numAltAlleles+1];
            for ( int alleleI = 0; alleleI < expectedACs.length; alleleI++ ) {
                expectedACs[alleleI] = 0;
                final Allele allele = getAlleles().get(alleleI);
                for ( Genotype g : arg ) {
                    expectedACs[alleleI] += Collections.frequency(g.getAlleles(), allele);
                }
            }
        }

        public AFCalcResult execute() {
            return getCalc().getLog10PNonRef(getVC(), getPriors());
        }

        public AFCalcResult executeRef() {
            final AFCalc ref = AFCalcFactory.createAFCalc(AFCalcFactory.Calculation.EXACT_REFERENCE, getCalc().nSamples, getCalc().getMaxAltAlleles());
            return ref.getLog10PNonRef(getVC(), getPriors());
        }

        public double[] getPriors() {
            return priors;
        }

        public AFCalc getCalc() {
            return calc;
        }

        public VariantContext getVC() {
            VariantContextBuilder builder = new VariantContextBuilder("test", "1", 1, 1, getAlleles());
            builder.genotypes(GLs);
            return builder.make();
        }

        public List<Allele> getAlleles() {
            return Arrays.asList(Allele.create("A", true),
                    Allele.create("C"),
                    Allele.create("G"),
                    Allele.create("T")).subList(0, numAltAlleles+1);
        }

        public int getExpectedAltAC(final int alleleI) {
            return expectedACs[alleleI+1];
        }

        public String toString() {
            return String.format("%s model=%s prior=%s input=%s", super.toString(), calc.getClass().getSimpleName(),
                    priorName, GLs.size() > 5 ? String.format("%d samples", GLs.size()) : GLs);
        }
    }

    @DataProvider(name = "wellFormedGLs")
    public Object[][] createSimpleGLsData() {
        final List<Genotype> biAllelicSamples = Arrays.asList(AA1, AB1, BB1);
        final List<Genotype> triAllelicSamples = Arrays.asList(AA2, AB2, BB2, AC2, BC2, CC2);

        for ( final int nSamples : Arrays.asList(1, 2, 3, 4) ) {
            List<AFCalc> calcs = AFCalcFactory.createAFCalcs(
                    Arrays.asList(
                            AFCalcFactory.Calculation.EXACT_REFERENCE,
                            AFCalcFactory.Calculation.EXACT_INDEPENDENT,
                            AFCalcFactory.Calculation.EXACT_GENERAL_PLOIDY
                    ), 4, 2, 2, 2);

            final int nPriorValues = 2*nSamples+1;
            final double[] flatPriors = MathUtils.normalizeFromLog10(new double[nPriorValues], true);  // flat priors
            final double[] humanPriors = new double[nPriorValues];
            UnifiedGenotyperEngine.computeAlleleFrequencyPriors(nPriorValues - 1, humanPriors, 0.001);

            for ( final double[] priors : Arrays.asList(flatPriors, humanPriors) ) { // , humanPriors) ) {
                for ( AFCalc model : calcs ) {
                    final String priorName = priors == humanPriors ? "human" : "flat";

                    // bi-allelic
                    if ( INCLUDE_BIALLELIC && nSamples <= biAllelicSamples.size() )
                        for ( List<Genotype> genotypes : Utils.makePermutations(biAllelicSamples, nSamples, true) )
                            new GetGLsTest(model, 1, genotypes, priors, priorName);

                    // tri-allelic
                    if ( INCLUDE_TRIALLELIC && ( ! priorName.equals("human") || Guillermo_FIXME ) ) // || model != generalCalc ) )
                        for ( List<Genotype> genotypes : Utils.makePermutations(triAllelicSamples, nSamples, true) )
                            new GetGLsTest(model, 2, genotypes, priors, priorName);
                }
            }
        }

        return GetGLsTest.getTests(GetGLsTest.class);
    }

    @DataProvider(name = "badGLs")
    public Object[][] createBadGLs() {
        final List<Genotype> genotypes = Arrays.asList(AB2, BB2, CC2, CC2);
        final int nSamples = genotypes.size();

        final AFCalc indCalc = AFCalcFactory.createAFCalc(AFCalcFactory.Calculation.EXACT_INDEPENDENT, nSamples, 4);

        final int nPriorValues = 2*nSamples+1;
        final double[] priors = MathUtils.normalizeFromLog10(new double[nPriorValues], true);  // flat priors
        for ( AFCalc model : Arrays.asList(indCalc) ) {
            final String priorName = "flat";
            new GetGLsTest(model, 2, genotypes, priors, priorName);
        }

        return GetGLsTest.getTests(GetGLsTest.class);
    }

    @Test(enabled = true && ! DEBUG_ONLY, dataProvider = "wellFormedGLs")
    public void testBiallelicGLs(GetGLsTest cfg) {
        if ( cfg.getAlleles().size() == 2 )
            testResultSimple(cfg);
    }

    @Test(enabled = true && ! DEBUG_ONLY, dataProvider = "wellFormedGLs")
    public void testTriallelicGLs(GetGLsTest cfg) {
        if ( cfg.getAlleles().size() > 2 )
            testResultSimple(cfg);
    }

    @Test(enabled = true && !DEBUG_ONLY, dataProvider = "badGLs")
    public void testBadGLs(GetGLsTest cfg) {
        testResultSimple(cfg);
    }

    private static class NonInformativeData {
        final Genotype nonInformative;
        final List<Genotype> called;
        final int nAltAlleles;

        private NonInformativeData(List<Genotype> called, Genotype nonInformative, int nAltAlleles) {
            this.called = called;
            this.nonInformative = nonInformative;
            this.nAltAlleles = nAltAlleles;
        }
    }

    @DataProvider(name = "GLsWithNonInformative")
    public Object[][] makeGLsWithNonInformative() {
        List<Object[]> tests = new ArrayList<Object[]>();

        final List<NonInformativeData> nonInformativeTests = new LinkedList<NonInformativeData>();
        nonInformativeTests.add(new NonInformativeData(Arrays.asList(AB1), NON_INFORMATIVE1, 1));
        nonInformativeTests.add(new NonInformativeData(Arrays.asList(AB2), NON_INFORMATIVE2, 2));
        nonInformativeTests.add(new NonInformativeData(Arrays.asList(AB2, BC2), NON_INFORMATIVE2, 2));

        for ( final int nNonInformative : Arrays.asList(1, 10, 100) ) {
            for ( final NonInformativeData testData : nonInformativeTests ) {
                final List<Genotype> samples = new ArrayList<Genotype>();
                samples.addAll(testData.called);
                samples.addAll(Collections.nCopies(nNonInformative, testData.nonInformative));

                final int nSamples = samples.size();
                List<AFCalc> calcs = AFCalcFactory.createAFCalcs(
                        Arrays.asList(
                                AFCalcFactory.Calculation.EXACT_REFERENCE,
                                AFCalcFactory.Calculation.EXACT_INDEPENDENT,
                                AFCalcFactory.Calculation.EXACT_GENERAL_PLOIDY
                        ), 4, 2, 2, 2);

                final double[] priors = MathUtils.normalizeFromLog10(new double[2*nSamples+1], true);  // flat priors

                for ( AFCalc model : calcs ) {
                    final GetGLsTest onlyInformative = new GetGLsTest(model, testData.nAltAlleles, testData.called, priors, "flat");

                    for ( int rotation = 0; rotation < nSamples; rotation++ ) {
                        Collections.rotate(samples, 1);
                        final GetGLsTest withNonInformative = new GetGLsTest(model, testData.nAltAlleles, samples, priors, "flat");
                        tests.add(new Object[]{onlyInformative, withNonInformative});
                    }
                }
            }
        }

        return tests.toArray(new Object[][]{});
    }

    @Test(enabled = true && ! DEBUG_ONLY, dataProvider = "GLsWithNonInformative", dependsOnMethods = {"testBiallelicGLs", "testTriallelicGLs"})
    public void testGLsWithNonInformative(GetGLsTest onlyInformative, GetGLsTest withNonInformative) {
        final AFCalcResult expected = onlyInformative.execute();
        final AFCalcResult actual = withNonInformative.execute();

        testResultSimple(withNonInformative);
        compareAFCalcResults(actual, expected, onlyInformative.getCalc(), true);
    }

    private void testResultSimple(final GetGLsTest cfg) {
        final AFCalcResult refResultTracker = cfg.executeRef();
        final AFCalcResult resultTracker = cfg.execute();

        compareAFCalcResults(resultTracker, refResultTracker, cfg.getCalc(), true);

        Assert.assertNotNull(resultTracker.getAllelesUsedInGenotyping());
        Assert.assertTrue(cfg.getAlleles().containsAll(resultTracker.getAllelesUsedInGenotyping()), "Result object has alleles not in our initial allele list");

        for ( int altAlleleI = 0; altAlleleI < cfg.numAltAlleles; altAlleleI++ ) {
            int expectedAlleleCount = cfg.getExpectedAltAC(altAlleleI);
            int calcAC_MLE = resultTracker.getAlleleCountsOfMLE()[altAlleleI];

            final Allele allele = cfg.getAlleles().get(altAlleleI+1);
            Assert.assertEquals(calcAC_MLE, expectedAlleleCount, "MLE AC not equal to expected AC for allele " + allele);
        }
    }

    private void compareAFCalcResults(final AFCalcResult actual, final AFCalcResult expected, final AFCalc calc, final boolean onlyPosteriorsShouldBeEqual) {
        // note we cannot really test the multi-allelic case because we actually meaningfully differ among the models here
        final double TOLERANCE = calc.getMaxAltAlleles() > 1 ? 1000 : 0.1; // much tighter constraints on bi-allelic results

        if ( ! onlyPosteriorsShouldBeEqual ) {
            Assert.assertEquals(actual.getLog10PriorOfAFEq0(), expected.getLog10PriorOfAFEq0(), TOLERANCE, "Priors AF == 0");
            Assert.assertEquals(actual.getLog10PriorOfAFGT0(), expected.getLog10PriorOfAFGT0(), TOLERANCE, "Priors AF > 0");
            Assert.assertEquals(actual.getLog10LikelihoodOfAFEq0(), expected.getLog10LikelihoodOfAFEq0(), TOLERANCE, "Likelihoods AF == 0");
            Assert.assertEquals(actual.getLog10LikelihoodOfAFGT0(), expected.getLog10LikelihoodOfAFGT0(), TOLERANCE, "Likelihoods AF > 0");
        }
        Assert.assertEquals(actual.getLog10PosteriorOfAFEq0(), expected.getLog10PosteriorOfAFEq0(), TOLERANCE, "Posteriors AF == 0");
        Assert.assertEquals(actual.getLog10PosteriorOfAFGT0(), expected.getLog10PosteriorOfAFGT0(), TOLERANCE, "Posteriors AF > 0");
        Assert.assertEquals(actual.getAlleleCountsOfMLE(), expected.getAlleleCountsOfMLE(), "MLE ACs");
        Assert.assertEquals(actual.getAllelesUsedInGenotyping(), expected.getAllelesUsedInGenotyping(), "Alleles used in genotyping");

        for ( final Allele a : expected.getAllelesUsedInGenotyping() ) {
            if ( ! a.isReference() ) {
                Assert.assertEquals(actual.getAlleleCountAtMLE(a), expected.getAlleleCountAtMLE(a), "MLE AC for allele " + a);
                // TODO -- enable me when IndependentAllelesDiploidExactAFCalc works properly
//                if ( ! ( calc instanceof GeneralPloidyExactAFCalc ) )
//                    // TODO -- delete when general ploidy works properly with multi-allelics
//                    Assert.assertEquals(actual.isPolymorphic(a, 0.0), expected.isPolymorphic(a, 0.0), "isPolymorphic with thread 0.0 for allele " + a);
            }
        }
    }

    @Test(enabled = true && ! DEBUG_ONLY, dataProvider = "Models")
    public void testLargeGLs(final ExactAFCalc calc) {
        final Genotype BB = makePL(Arrays.asList(C, C), 20000000, 20000000, 0);
        GetGLsTest cfg = new GetGLsTest(calc, 1, Arrays.asList(BB, BB, BB), FLAT_3SAMPLE_PRIORS, "flat");

        final AFCalcResult resultTracker = cfg.execute();

        int calculatedAlleleCount = resultTracker.getAlleleCountsOfMLE()[0];
        Assert.assertEquals(calculatedAlleleCount, 6);
    }

    @Test(enabled = true && ! DEBUG_ONLY, dataProvider = "Models")
    public void testMismatchedGLs(final ExactAFCalc calc) {
        final Genotype AB = makePL(Arrays.asList(A, C), 2000, 0, 2000, 2000, 2000, 2000);
        final Genotype AC = makePL(Arrays.asList(A, G), 100, 100, 100, 0, 100, 100);
        GetGLsTest cfg = new GetGLsTest(calc, 2, Arrays.asList(AB, AC), FLAT_3SAMPLE_PRIORS, "flat");

        final AFCalcResult resultTracker = cfg.execute();

        Assert.assertEquals(resultTracker.getAlleleCountsOfMLE()[0], 1);
        Assert.assertEquals(resultTracker.getAlleleCountsOfMLE()[1], 1);
    }

    // --------------------------------------------------------------------------------
    //
    // Code to test that the pNonRef value is meaningful
    //
    // --------------------------------------------------------------------------------

    private static class PNonRefData {
        final Genotype g;
        final double pNonRef, tolerance;
        final boolean canScale;
        final List<AFCalcFactory.Calculation> badModels;
        final VariantContext vc;

        private PNonRefData(final VariantContext vc, Genotype g, double pNonRef, double tolerance, final boolean canScale) {
            this(vc, g, pNonRef, tolerance, canScale, Collections.<AFCalcFactory.Calculation>emptyList());
        }

        private PNonRefData(final VariantContext vc, Genotype g, double pNonRef, double tolerance, final boolean canScale, final List<AFCalcFactory.Calculation> badModels) {
            this.g = g;
            this.pNonRef = pNonRef;
            this.tolerance = tolerance;
            this.canScale = canScale;
            this.badModels = badModels;
            this.vc = vc;
        }

        public PNonRefData scale(final int scaleFactor) {
            if ( canScale ) {
                final int[] PLs = new int[g.getPL().length];
                for ( int i = 0; i < PLs.length; i++ ) PLs[i] = g.getPL()[i] * ((int)Math.log10(scaleFactor)+1);
                final Genotype scaledG = new GenotypeBuilder(g).PL(PLs).make();
                final double scaledPNonRef = pNonRef < 0.5 ? pNonRef / scaleFactor : 1 - ((1-pNonRef) / scaleFactor);
                return new PNonRefData(vc, scaledG, scaledPNonRef, tolerance, true);
            } else {
                return this;
            }
        }
    }

    @DataProvider(name = "PNonRef")
    public Object[][] makePNonRefTest() {
        List<Object[]> tests = new ArrayList<Object[]>();

        final List<Allele> AA = Arrays.asList(A, A);
        final List<Allele> AC = Arrays.asList(A, C);
        final List<Allele> CC = Arrays.asList(C, C);
        final List<Allele> AG = Arrays.asList(A, G);
        final List<Allele> GG = Arrays.asList(G, G);
        final List<Allele> CG = Arrays.asList(C, G);

        final VariantContext vc2 = new VariantContextBuilder("x","1", 1, 1, Arrays.asList(A, C)).make();
        final VariantContext vc3 = new VariantContextBuilder("x","1", 1, 1, Arrays.asList(A, C, G)).make();
        final AFCalcTestBuilder.PriorType priorType = AFCalcTestBuilder.PriorType.flat;

        final double TOLERANCE = 0.5;

        final List<PNonRefData> initialPNonRefData = Arrays.asList(
                // bi-allelic sites
                new PNonRefData(vc2, makePL(AA, 0, 10, 10), 0.1666667, TOLERANCE, true),
                new PNonRefData(vc2, makePL(AA, 0,  1, 10), 0.4721084, TOLERANCE, false),
                new PNonRefData(vc2, makePL(AA, 0,  1,  1), 0.6136992, TOLERANCE, false),
                new PNonRefData(vc2, makePL(AA, 0,  5,  5), 0.3874259, TOLERANCE, false),
                new PNonRefData(vc2, makePL(AC, 10, 0, 10), 0.9166667, TOLERANCE, true),
                new PNonRefData(vc2, makePL(CC, 10, 10, 0), 0.9166667, TOLERANCE, true),

                // tri-allelic sites -- cannot scale because of the naivety of our scaling estimator
                new PNonRefData(vc3, makePL(AA, 0, 10, 10, 10, 10, 10), 0.3023255813953489, TOLERANCE * 2, false), // more tolerance because constrained model is a bit inaccurate
                new PNonRefData(vc3, makePL(AC, 10, 0, 10, 10, 10, 10), 0.9166667, TOLERANCE, false),
                new PNonRefData(vc3, makePL(CC, 10, 10, 0, 10, 10, 10), 0.9166667, TOLERANCE, false),
                new PNonRefData(vc3, makePL(AG, 10, 10, 10, 0, 10, 10), 0.9166667, TOLERANCE, false),
                new PNonRefData(vc3, makePL(CG, 10, 10, 10, 10, 0, 10), 0.80, TOLERANCE, false),
                new PNonRefData(vc3, makePL(GG, 10, 10, 10, 10, 10, 0), 0.9166667, TOLERANCE, false)
        );

        for ( AFCalcFactory.Calculation modelType : Arrays.asList(AFCalcFactory.Calculation.EXACT_REFERENCE, AFCalcFactory.Calculation.EXACT_INDEPENDENT) ) {
            for ( int nNonInformative = 0; nNonInformative < 3; nNonInformative++ ) {
                for ( final PNonRefData rootData : initialPNonRefData ) {
                    for ( int plScale = 1; plScale <= 100000; plScale *= 10 ) {
                        if ( ! rootData.badModels.contains(modelType) && (plScale == 1 || rootData.canScale) ) {
                            final PNonRefData data = rootData.scale(plScale);
                            tests.add(new Object[]{data.vc, modelType, priorType, Arrays.asList(data.g), data.pNonRef, data.tolerance, nNonInformative});
                        }
                    }
                }
            }
        }

        return tests.toArray(new Object[][]{});
    }

    @Test(enabled = true && ! DEBUG_ONLY, dataProvider = "PNonRef")
    private void testPNonRef(final VariantContext vcRoot,
                             AFCalcFactory.Calculation modelType,
                             AFCalcTestBuilder.PriorType priorType,
                             final List<Genotype> genotypes,
                             final double expectedPNonRef,
                             final double tolerance,
                             final int nNonInformative) {
        final AFCalcTestBuilder testBuilder
                = new AFCalcTestBuilder(1, vcRoot.getNAlleles()-1, modelType, priorType);

        final VariantContextBuilder vcb = new VariantContextBuilder(vcRoot);
        vcb.genotypes(genotypes);

        final AFCalcResult resultTracker = testBuilder.makeModel().getLog10PNonRef(vcb.make(), testBuilder.makePriors());

        Assert.assertEquals(resultTracker.getLog10PosteriorOfAFGT0(), Math.log10(expectedPNonRef), tolerance,
                "Actual pNonRef not within tolerance " + tolerance + " of expected");
    }

    // --------------------------------------------------------------------------------
    //
    // Test priors
    //
    // --------------------------------------------------------------------------------

    @DataProvider(name = "Models")
    public Object[][] makeModels() {
        List<Object[]> tests = new ArrayList<Object[]>();

        for ( final AFCalcFactory.Calculation calc : AFCalcFactory.Calculation.values() ) {
            if ( calc.usableForParams(2, 4) )
                tests.add(new Object[]{AFCalcFactory.createAFCalc(calc, 2, 4)});
        }

        return tests.toArray(new Object[][]{});
    }

    @Test(enabled = true && !DEBUG_ONLY, dataProvider = "Models")
    public void testBiallelicPriors(final AFCalc model) {

        for ( int REF_PL = 10; REF_PL <= 20; REF_PL += 10 ) {
            final Genotype AB = makePL(Arrays.asList(A,C), REF_PL, 0, 10000);

            for ( int log10NonRefPrior = 1; log10NonRefPrior < 10*REF_PL; log10NonRefPrior += 1 ) {
                final double refPrior = 1 - QualityUtils.qualToErrorProb(log10NonRefPrior);
                final double nonRefPrior = (1-refPrior) / 2;
                final double[] priors = MathUtils.normalizeFromLog10(MathUtils.toLog10(new double[]{refPrior, nonRefPrior, nonRefPrior}), true);
                if ( ! Double.isInfinite(priors[1]) ) {
                    GetGLsTest cfg = new GetGLsTest(model, 1, Arrays.asList(AB), priors, "pNonRef" + log10NonRefPrior);
                    final AFCalcResult resultTracker = cfg.execute();
                    final int actualAC = resultTracker.getAlleleCountsOfMLE()[0];

                    final double pRefWithPrior = AB.getLikelihoods().getAsVector()[0] + priors[0];
                    final double pHetWithPrior = AB.getLikelihoods().getAsVector()[1] + priors[1] - Math.log10(0.5);
                    final double nonRefPost = Math.pow(10, pHetWithPrior) / (Math.pow(10, pRefWithPrior) + Math.pow(10, pHetWithPrior));
                    final double log10NonRefPost = Math.log10(nonRefPost);

                    if ( ! Double.isInfinite(log10NonRefPost) )
                        Assert.assertEquals(resultTracker.getLog10PosteriorOfAFGT0(), log10NonRefPost, 1e-2);

                    if ( nonRefPost >= 0.9 )
                        Assert.assertTrue(resultTracker.isPolymorphic(C, -1));

                    final int expectedMLEAC = 1; // the MLE is independent of the prior
                    Assert.assertEquals(actualAC, expectedMLEAC,
                            "actual AC with priors " + log10NonRefPrior + " not expected "
                                    + expectedMLEAC + " priors " + Utils.join(",", priors));
                }
            }
        }
    }

    @Test(enabled = true && ! DEBUG_ONLY, dataProvider = "Models")

    // --------------------------------------------------------------------------------
    //
    // Test that polymorphic sites (bi and tri) are properly called
    //
    // --------------------------------------------------------------------------------

    @DataProvider(name = "polyTestProvider")
    public Object[][] makePolyTestProvider() {
        List<Object[]> tests = new ArrayList<Object[]>();

        // list of all high-quality models in the system
        final List<AFCalcFactory.Calculation> models = Arrays.asList(
                AFCalcFactory.Calculation.EXACT,
                AFCalcFactory.Calculation.EXACT_REFERENCE,
                AFCalcFactory.Calculation.EXACT_INDEPENDENT);

        // note that we cannot use small PLs here or the thresholds are hard to set
        for ( final int nonTypePLs : Arrays.asList(100, 1000) ) {
            for ( final AFCalcFactory.Calculation model : models ) {
                for ( final int allele1AC : Arrays.asList(0, 1, 2, 10, 100, 1000, 10000) ) {
                    for ( final int nSamples : Arrays.asList(1, 10, 100, 1000, 10000) ) {
//        for ( final int nonTypePLs : Arrays.asList(10) ) {
//            for ( final AFCalcFactory.Calculation model : models ) {
//                for ( final int allele1AC : Arrays.asList(100) ) {
//                    for ( final int nSamples : Arrays.asList(1000) ) {
                        if ( nSamples < allele1AC ) continue;

                        final double pPerSample = Math.pow(10, nonTypePLs / -10.0);
                        final double errorFreq = pPerSample * nSamples;
                        final boolean poly1 = allele1AC > errorFreq && (nonTypePLs * allele1AC) > 30;

                        // bi-allelic tests
                        {
                            final AFCalcTestBuilder testBuilder
                                    = new AFCalcTestBuilder(nSamples, 1, model, AFCalcTestBuilder.PriorType.human);
                            final List<Integer> ACs = Arrays.asList(allele1AC);
                            tests.add(new Object[]{testBuilder, ACs, nonTypePLs, Arrays.asList(poly1)});
                        }

                        // multi-allelic tests
                        for ( final int allele2AC : Arrays.asList(0, 1, 2, 10, 20, 50) ) {
                            if ( nSamples < allele2AC || allele1AC + allele2AC > nSamples || nSamples > 100 || nSamples == 1)
                                continue;

                            final AFCalcTestBuilder testBuilder
                                    = new AFCalcTestBuilder(nSamples, 2, model, AFCalcTestBuilder.PriorType.human);
                            final List<Integer> ACs = Arrays.asList(allele1AC, allele2AC);
                            final boolean poly2 = allele2AC > errorFreq && (nonTypePLs * allele2AC) > 90;
                            tests.add(new Object[]{testBuilder, ACs, nonTypePLs, Arrays.asList(poly1, poly2)});
                        }
                    }
                }
            }
        }

        return tests.toArray(new Object[][]{});
    }

    @Test(enabled = true && ! DEBUG_ONLY, dataProvider = "polyTestProvider")
    public void testCallingGeneral(final AFCalcTestBuilder testBuilder, final List<Integer> ACs, final int nonTypePL, final List<Boolean> expectedPoly ) {
        testCalling(testBuilder, ACs, nonTypePL, expectedPoly);
    }

    @DataProvider(name = "polyTestProviderLotsOfAlleles")
    public Object[][] makepolyTestProviderLotsOfAlleles() {
        List<Object[]> tests = new ArrayList<Object[]>();

        // list of all high-quality models in the system
        final List<AFCalcFactory.Calculation> models = Arrays.asList(AFCalcFactory.Calculation.EXACT_INDEPENDENT);

        final List<Integer> alleleCounts = Arrays.asList(0, 1, 2, 3, 4, 5, 10, 20);

        final int nonTypePLs = 1000;
        final int nAlleles = 4;
        for ( final AFCalcFactory.Calculation model : models ) {
            for ( final List<Integer> ACs : Utils.makePermutations(alleleCounts, nAlleles, true) ) {
                final List<Boolean> isPoly = new ArrayList<Boolean>(ACs.size());
                for ( final int ac : ACs ) isPoly.add(ac > 0);

                final double acSum = MathUtils.sum(ACs);
                for ( final int nSamples : Arrays.asList(1, 10, 100) ) {
                    if ( nSamples < acSum ) continue;
                    final AFCalcTestBuilder testBuilder
                            = new AFCalcTestBuilder(nSamples, nAlleles, model, AFCalcTestBuilder.PriorType.human);
                    tests.add(new Object[]{testBuilder, ACs, nonTypePLs, isPoly});
                }
            }
        }

        return tests.toArray(new Object[][]{});
    }

    @Test(enabled = true && ! DEBUG_ONLY, dataProvider = "polyTestProviderLotsOfAlleles")
    public void testCallingLotsOfAlleles(final AFCalcTestBuilder testBuilder, final List<Integer> ACs, final int nonTypePL, final List<Boolean> expectedPoly ) {
        testCalling(testBuilder, ACs, nonTypePL, expectedPoly);
    }

    private void testCalling(final AFCalcTestBuilder testBuilder, final List<Integer> ACs, final int nonTypePL, final List<Boolean> expectedPoly) {
        final AFCalc calc = testBuilder.makeModel();
        final double[] priors = testBuilder.makePriors();
        final VariantContext vc = testBuilder.makeACTest(ACs, 0, nonTypePL);
        final AFCalcResult result = calc.getLog10PNonRef(vc, priors);

        boolean anyPoly = false;
        for ( final boolean onePoly : expectedPoly ) anyPoly = anyPoly || onePoly;

        if ( anyPoly )
            Assert.assertTrue(result.getLog10PosteriorOfAFGT0() > -1);

        for ( int altI = 1; altI < result.getAllelesUsedInGenotyping().size(); altI++ ) {
            final int i = altI - 1;
            final Allele alt = result.getAllelesUsedInGenotyping().get(altI);

            // must be getCalledChrCount because we cannot ensure that the VC made has our desired ACs
            Assert.assertEquals(result.getAlleleCountAtMLE(alt), vc.getCalledChrCount(alt));
            Assert.assertEquals(result.isPolymorphic(alt, -1), (boolean)expectedPoly.get(i), "isPolymorphic for allele " + alt + " " + result.getLog10PosteriorOfAFGt0ForAllele(alt));
        }
    }
}