package org.broadinstitute.sting.gatk.walkers.bqsr;

import org.broadinstitute.sting.WalkerTest;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * @author carneiro
 * @since 3/27/12
 */
public class BQSRIntegrationTest extends WalkerTest {
    @Test(enabled = true)
    public void recalibrateTest() {
        String REF = "public/testdata/exampleFASTA.fasta";
        String BAM = "public/testdata/exampleBAM.bam";
        String DBSNP = "public/testdata/exampleDBSNP.vcf";
        String base = String.format("-T BaseQualityScoreRecalibrator -R %s -I %s -knownSites %s", REF, BAM, DBSNP) + " -o %s ";
        WalkerTestSpec spec = new WalkerTestSpec(base, Arrays.asList("a0de2fb6f457a66513bda5fe15775c04"));
        executeTest("recalibrateTest", spec);
    }

    @Test(enabled = true)
    public void onTheFlyRecalibrationTest() {
        String REF = "public/testdata/exampleFASTA.fasta";
        String BAM = "public/testdata/exampleBAM.bam";
        String GRP = "public/testdata/exampleGRP.grp";
        String base = String.format("-T PrintReads -R %s -I %s -BQSR %s", REF, BAM, GRP) + " -o %s ";
        WalkerTestSpec spec = new WalkerTestSpec(base, Arrays.asList("690c7ab414bd39de72a635aedc1fcd66"));
        executeTest("onTheFlyRecalibrationTest", spec);
    }

}
