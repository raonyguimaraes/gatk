/*
 * Copyright (c) 2011, The Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package org.broadinstitute.sting.gatk.walkers.diffengine;

import net.sf.samtools.SAMException;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import org.broad.tribble.readers.AsciiLineReader;
import org.broad.tribble.readers.LineReader;
import org.broadinstitute.sting.utils.codecs.vcf.VCFCodec;
import org.broadinstitute.sting.utils.codecs.vcf.VCFHeader;
import org.broadinstitute.sting.utils.variantcontext.Genotype;
import org.broadinstitute.sting.utils.variantcontext.VariantContext;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;


/**
 * Created by IntelliJ IDEA.
 * User: depristo
 * Date: 7/4/11
 * Time: 1:09 PM
 *
 * Class implementing diffnode reader for VCF
 */
public class BAMDiffableReader implements DiffableReader {
    @Override
    public String getName() { return "BAM"; }

    @Override
    public DiffNode readFromFile(File file) {
        final SAMFileReader reader = new SAMFileReader(file, null); // null because we don't want it to look for the index

        DiffNode root = DiffNode.rooted(file.getName());
        SAMRecordIterator iterator = reader.iterator();

        while ( iterator.hasNext() ) {
            final SAMRecord record = iterator.next();

            // name is the read name + first of pair
            String name = record.getReadName();
            if ( record.getReadPairedFlag() ) {
                name += record.getFirstOfPairFlag() ? "_1" : "_2";
            }

            DiffNode readRoot = DiffNode.empty(name, root);

            // add fields
            readRoot.add("FLAGS", record.getFlags());
            readRoot.add("RNAME", record.getReferenceName());
            readRoot.add("POS", record.getAlignmentStart());
            readRoot.add("MAPQ", record.getMappingQuality());
            readRoot.add("CIGAR", record.getCigarString());
            readRoot.add("RNEXT", record.getMateReferenceName());
            readRoot.add("PNEXT", record.getMateAlignmentStart());
            readRoot.add("TLEN", record.getReadLength());
            readRoot.add("SEQ", record.getReadString());
            readRoot.add("QUAL", record.getBaseQualityString());

            for ( SAMRecord.SAMTagAndValue xt : record.getAttributes() ) {
                readRoot.add(xt.tag, xt.value);
            }

            // add record to root
            root.add(readRoot);
        }

        reader.close();

        return root;
    }

    @Override
    public boolean canRead(File file) {
        // todo -- we are going to have to manually look into the file and use the BAMFileReader
        // since the SAM-JDK is looking at extensions.  Need to figure out what exception is
        // getting thrown so we can catch it, and return false.
        return false;
    }
//        try {
//            final SAMFileReader inReader = new SAMFileReader(file, null); // null because we don't want it to look for the index
//            return true;
//        } catch ( SAMException e ) {
//            return false;
//        }
//    }
}
