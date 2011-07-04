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

import org.broadinstitute.sting.commandline.Argument;
import org.broadinstitute.sting.commandline.Output;
import org.broadinstitute.sting.gatk.contexts.AlignmentContext;
import org.broadinstitute.sting.gatk.contexts.ReferenceContext;
import org.broadinstitute.sting.gatk.refdata.RefMetaDataTracker;
import org.broadinstitute.sting.gatk.walkers.Requires;
import org.broadinstitute.sting.gatk.walkers.RodWalker;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

/**
 * Compares two record-oriented files, itemizing specific difference between equivalent
 * records in the two files.  Reports both itemized and summarized differences.
 * @author Mark DePristo
 * @version 0.1
 */
@Requires(value={})
public class DiffObjectsWalker extends RodWalker<Integer, Integer> {
    @Output(doc="File to which results should be written",required=true)
    protected PrintStream out;

    @Argument(fullName="maxRecords", shortName="M", doc="Max. number of records to process", required=false)
    int MAX_RECORDS = -1;

    @Argument(fullName="master", shortName="m", doc="Master file: expected results", required=true)
    File masterFile;

    @Argument(fullName="test", shortName="t", doc="Test file: new results to compare to the master file", required=true)
    File testFile;

    DiffEngine diffEngine;

    @Override
    public void initialize() {
        diffEngine = new DiffEngine(MAX_RECORDS);
    }

    @Override
    public Integer map(RefMetaDataTracker tracker, ReferenceContext ref, AlignmentContext context) {
        return 0;
    }

    @Override
    public Integer reduceInit() {
        return 0;
    }

    @Override
    public Integer reduce(Integer counter, Integer sum) {
        return counter + sum;
    }

    @Override
    public void onTraversalDone(Integer sum) {
        out.printf("Itemized results%n");
        DiffNode master = diffEngine.createDiffableFromFile(masterFile);
        out.println(master.toString());
//        DiffNode test = diffEngine.createDiffableFromFile(testFile);
//        List<Difference> diffs = diffEngine.diff(master, test);
//        out.println(diffEngine.reportItemizedDifference(diffs));
//        out.println(diffEngine.reportSummarizedDifferences(diffs));
    }
}