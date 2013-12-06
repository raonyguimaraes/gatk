/*
* Copyright (c) 2012 The Broad Institute
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
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
* OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
* HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
* WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
* THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package org.broadinstitute.sting.gatk.datasources.reads;

import net.sf.picard.util.PeekableIterator;
import org.broadinstitute.sting.utils.GenomeLocParser;

import java.util.Iterator;

/**
 * Balances maximally granular file pointers into shards of reasonable size.
 */
public abstract class ShardBalancer implements Iterable<Shard> {
    protected SAMDataSource readsDataSource;
    protected PeekableIterator<FilePointer> filePointers;
    protected GenomeLocParser parser;

    public void initialize(final SAMDataSource readsDataSource, final Iterator<FilePointer> filePointers, final GenomeLocParser parser) {
        this.readsDataSource = readsDataSource;
        this.filePointers = new PeekableIterator<FilePointer>(filePointers);
        this.parser = parser;
    }
    public void close() {
      this.filePointers.close();
    }
}
