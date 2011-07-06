package org.broadinstitute.sting.oneoffprojects.walkers.newassociation;

import net.sf.picard.reference.ReferenceSequenceFileFactory;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMSequenceDictionary;
import org.broadinstitute.sting.alignment.Alignment;
import org.broadinstitute.sting.alignment.bwa.BWAConfiguration;
import org.broadinstitute.sting.alignment.bwa.BWTFiles;
import org.broadinstitute.sting.alignment.bwa.c.BWACAligner;
import org.broadinstitute.sting.commandline.Argument;
import org.broadinstitute.sting.commandline.Output;
import org.broadinstitute.sting.gatk.contexts.ReferenceContext;
import org.broadinstitute.sting.gatk.refdata.ReadMetaDataTracker;
import org.broadinstitute.sting.gatk.walkers.ReadWalker;

import java.io.File;
import java.io.PrintStream;

/**
 * Created by IntelliJ IDEA.
 * User: chartl
 * Date: 7/1/11
 * Time: 2:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class BWAAlignmentInfoWalker extends ReadWalker<Integer,Integer> {

    @Argument(fullName="target_reference",shortName="target_ref",doc="The reference to which reads in the source file should be aligned.  Alongside this reference should sit index files " +
                                                                     "generated by bwa index -d bwtsw.  If unspecified, will default " +
                                                                     "to the reference specified via the -R argument.",required=false)
    private File targetReferenceFile = null;

    @Output
    PrintStream out;

    BWACAligner aligner = null;

    private SAMFileHeader header = null;

    public void initialize() {
        if(targetReferenceFile == null)
            targetReferenceFile = getToolkit().getArguments().referenceFile;
        BWTFiles bwtFiles = new BWTFiles(targetReferenceFile.getAbsolutePath());
        BWAConfiguration configuration = new BWAConfiguration();
        aligner = new BWACAligner(bwtFiles,configuration);
            header = getToolkit().getSAMFileHeader().clone();
        SAMSequenceDictionary referenceDictionary =
                ReferenceSequenceFileFactory.getReferenceSequenceFile(targetReferenceFile).getSequenceDictionary();
        header.setSequenceDictionary(referenceDictionary);
        header.setSortOrder(SAMFileHeader.SortOrder.unsorted);
    }

    public Integer reduce( Integer a, Integer b) { return 0; }

    public boolean filter(ReferenceContext ref, SAMRecord read) {
        return read.getAttribute("AI") == null || read.getAttribute("AI").equals(0);
    }

    public Integer map(ReferenceContext ref, SAMRecord read, ReadMetaDataTracker tracker) {

        Iterable<SAMRecord[]> bwaAln = aligner.alignAll(read,header);
        out.printf("# Alignments for read: %s @ %s:%d-%d / %s %d%n",read.getReadName(),read.getReferenceName(),read.getAlignmentStart(),read.getAlignmentEnd(),read.getCigarString(),read.getMappingQuality());
        for ( SAMRecord[] readBatch : bwaAln ) {
            for ( SAMRecord alnR : readBatch ) {
                out.printf("%s\t%d\t%d\t%d\t%s%n",alnR.getReferenceName(),alnR.getAlignmentStart(),alnR.getAlignmentEnd(),alnR.getMappingQuality(),alnR.getCigarString());
            }
        }

        return 1;
    }

    public Integer reduceInit() {
        return 0;
    }
}
