/*
*  By downloading the PROGRAM you agree to the following terms of use:
*
*  BROAD INSTITUTE - SOFTWARE LICENSE AGREEMENT - FOR ACADEMIC NON-COMMERCIAL RESEARCH PURPOSES ONLY
*
*  This Agreement is made between the Broad Institute, Inc. with a principal address at 7 Cambridge Center, Cambridge, MA 02142 (BROAD) and the LICENSEE and is effective at the date the downloading is completed (EFFECTIVE DATE).
*
*  WHEREAS, LICENSEE desires to license the PROGRAM, as defined hereinafter, and BROAD wishes to have this PROGRAM utilized in the public interest, subject only to the royalty-free, nonexclusive, nontransferable license rights of the United States Government pursuant to 48 CFR 52.227-14; and
*  WHEREAS, LICENSEE desires to license the PROGRAM and BROAD desires to grant a license on the following terms and conditions.
*  NOW, THEREFORE, in consideration of the promises and covenants made herein, the parties hereto agree as follows:
*
*  1. DEFINITIONS
*  1.1 PROGRAM shall mean copyright in the object code and source code known as GATK2 and related documentation, if any, as they exist on the EFFECTIVE DATE and can be downloaded from http://www.broadinstitute/GATK on the EFFECTIVE DATE.
*
*  2. LICENSE
*  2.1   Grant. Subject to the terms of this Agreement, BROAD hereby grants to LICENSEE, solely for academic non-commercial research purposes, a non-exclusive, non-transferable license to: (a) download, execute and display the PROGRAM and (b) create bug fixes and modify the PROGRAM.
*  The LICENSEE may apply the PROGRAM in a pipeline to data owned by users other than the LICENSEE and provide these users the results of the PROGRAM provided LICENSEE does so for academic non-commercial purposes only.  For clarification purposes, academic sponsored research is not a commercial use under the terms of this Agreement.
*  2.2  No Sublicensing or Additional Rights. LICENSEE shall not sublicense or distribute the PROGRAM, in whole or in part, without prior written permission from BROAD.  LICENSEE shall ensure that all of its users agree to the terms of this Agreement.  LICENSEE further agrees that it shall not put the PROGRAM on a network, server, or other similar technology that may be accessed by anyone other than the LICENSEE and its employees and users who have agreed to the terms of this agreement.
*  2.3  License Limitations. Nothing in this Agreement shall be construed to confer any rights upon LICENSEE by implication, estoppel, or otherwise to any computer software, trademark, intellectual property, or patent rights of BROAD, or of any other entity, except as expressly granted herein. LICENSEE agrees that the PROGRAM, in whole or part, shall not be used for any commercial purpose, including without limitation, as the basis of a commercial software or hardware product or to provide services. LICENSEE further agrees that the PROGRAM shall not be copied or otherwise adapted in order to circumvent the need for obtaining a license for use of the PROGRAM.
*
*  3. OWNERSHIP OF INTELLECTUAL PROPERTY
*  LICENSEE acknowledges that title to the PROGRAM shall remain with BROAD. The PROGRAM is marked with the following BROAD copyright notice and notice of attribution to contributors. LICENSEE shall retain such notice on all copies.  LICENSEE agrees to include appropriate attribution if any results obtained from use of the PROGRAM are included in any publication.
*  Copyright 2012 Broad Institute, Inc.
*  Notice of attribution:  The GATK2 program was made available through the generosity of Medical and Population Genetics program at the Broad Institute, Inc.
*  LICENSEE shall not use any trademark or trade name of BROAD, or any variation, adaptation, or abbreviation, of such marks or trade names, or any names of officers, faculty, students, employees, or agents of BROAD except as states above for attribution purposes.
*
*  4. INDEMNIFICATION
*  LICENSEE shall indemnify, defend, and hold harmless BROAD, and their respective officers, faculty, students, employees, associated investigators and agents, and their respective successors, heirs and assigns, (Indemnitees), against any liability, damage, loss, or expense (including reasonable attorneys fees and expenses) incurred by or imposed upon any of the Indemnitees in connection with any claims, suits, actions, demands or judgments arising out of any theory of liability (including, without limitation, actions in the form of tort, warranty, or strict liability and regardless of whether such action has any factual basis) pursuant to any right or license granted under this Agreement.
*
*  5. NO REPRESENTATIONS OR WARRANTIES
*  THE PROGRAM IS DELIVERED AS IS.  BROAD MAKES NO REPRESENTATIONS OR WARRANTIES OF ANY KIND CONCERNING THE PROGRAM OR THE COPYRIGHT, EXPRESS OR IMPLIED, INCLUDING, WITHOUT LIMITATION, WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NONINFRINGEMENT, OR THE ABSENCE OF LATENT OR OTHER DEFECTS, WHETHER OR NOT DISCOVERABLE. BROAD EXTENDS NO WARRANTIES OF ANY KIND AS TO PROGRAM CONFORMITY WITH WHATEVER USER MANUALS OR OTHER LITERATURE MAY BE ISSUED FROM TIME TO TIME.
*  IN NO EVENT SHALL BROAD OR ITS RESPECTIVE DIRECTORS, OFFICERS, EMPLOYEES, AFFILIATED INVESTIGATORS AND AFFILIATES BE LIABLE FOR INCIDENTAL OR CONSEQUENTIAL DAMAGES OF ANY KIND, INCLUDING, WITHOUT LIMITATION, ECONOMIC DAMAGES OR INJURY TO PROPERTY AND LOST PROFITS, REGARDLESS OF WHETHER BROAD SHALL BE ADVISED, SHALL HAVE OTHER REASON TO KNOW, OR IN FACT SHALL KNOW OF THE POSSIBILITY OF THE FOREGOING.
*
*  6. ASSIGNMENT
*  This Agreement is personal to LICENSEE and any rights or obligations assigned by LICENSEE without the prior written consent of BROAD shall be null and void.
*
*  7. MISCELLANEOUS
*  7.1 Export Control. LICENSEE gives assurance that it will comply with all United States export control laws and regulations controlling the export of the PROGRAM, including, without limitation, all Export Administration Regulations of the United States Department of Commerce. Among other things, these laws and regulations prohibit, or require a license for, the export of certain types of software to specified countries.
*  7.2 Termination. LICENSEE shall have the right to terminate this Agreement for any reason upon prior written notice to BROAD. If LICENSEE breaches any provision hereunder, and fails to cure such breach within thirty (30) days, BROAD may terminate this Agreement immediately. Upon termination, LICENSEE shall provide BROAD with written assurance that the original and all copies of the PROGRAM have been destroyed, except that, upon prior written authorization from BROAD, LICENSEE may retain a copy for archive purposes.
*  7.3 Survival. The following provisions shall survive the expiration or termination of this Agreement: Articles 1, 3, 4, 5 and Sections 2.2, 2.3, 7.3, and 7.4.
*  7.4 Notice. Any notices under this Agreement shall be in writing, shall specifically refer to this Agreement, and shall be sent by hand, recognized national overnight courier, confirmed facsimile transmission, confirmed electronic mail, or registered or certified mail, postage prepaid, return receipt requested.  All notices under this Agreement shall be deemed effective upon receipt.
*  7.5 Amendment and Waiver; Entire Agreement. This Agreement may be amended, supplemented, or otherwise modified only by means of a written instrument signed by all parties. Any waiver of any rights or failure to act in a specific instance shall relate only to such instance and shall not be construed as an agreement to waive any rights or fail to act in any other instance, whether or not similar. This Agreement constitutes the entire agreement among the parties with respect to its subject matter and supersedes prior agreements or understandings between the parties relating to its subject matter.
*  7.6 Binding Effect; Headings. This Agreement shall be binding upon and inure to the benefit of the parties and their respective permitted successors and assigns. All headings are for convenience only and shall not affect the meaning of any provision of this Agreement.
*  7.7 Governing Law. This Agreement shall be construed, governed, interpreted and applied in accordance with the internal laws of the Commonwealth of Massachusetts, U.S.A., without regard to conflict of laws principles.
*/

package org.broadinstitute.sting.gatk.walkers.randomforest;

import org.broadinstitute.sting.gatk.GenomeAnalysisEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rpoplin
 * Date: 11/14/13
 */

public class RandomForestDecisionNode implements RandomForestNode {
    private final Decision decision;
    private final RandomForestNode leftNode;
    private final RandomForestNode rightNode;
    public Double accuracy = Double.NaN;
    private static final int NUM_TRIALS = 10;

    /**
     * Construct a RandomForestDecisionNode
     * @param data      the data from which to choose a random decision
     * @param depth     the current depth of the tree
     * @param maxDepth  the max depth of the tree
     */
    public RandomForestDecisionNode( final List<RandomForestDatum> data, final int depth, final int maxDepth ) {
        if( data.size() <= 0 ) { throw new IllegalArgumentException("data cannot be empty."); }

        Decision optimalDecision = null;
        double maxAccuracy = Double.MIN_VALUE;

        for( int trial = 0; trial < NUM_TRIALS; trial++ ) {
            final RandomForestDatum rfd = data.get(GenomeAnalysisEngine.getRandomGenerator().nextInt(data.size()));
            final Decision decision = new Decision(rfd);
            final double accuracy = evaluateDecision(decision, data);
            if( accuracy > maxAccuracy ) {
                maxAccuracy = accuracy;
                optimalDecision = decision;
            }
        }

        decision = optimalDecision;

        if( depth < maxDepth ) {
            final List<RandomForestDatum> dataLeft = subsetData(data, true);
            final List<RandomForestDatum> dataRight = subsetData(data, false);
            leftNode = ( dataLeft.size() > 0 ? new RandomForestDecisionNode(dataLeft, depth+1, maxDepth) : new RandomForestTerminalNode(GenomeAnalysisEngine.getRandomGenerator().nextBoolean()) );
            rightNode = ( dataRight.size() > 0 ? new RandomForestDecisionNode(dataRight, depth+1, maxDepth) : new RandomForestTerminalNode(GenomeAnalysisEngine.getRandomGenerator().nextBoolean()) );
        } else {
            final boolean leftResult = GenomeAnalysisEngine.getRandomGenerator().nextBoolean();
            leftNode = new RandomForestTerminalNode(leftResult);
            rightNode = new RandomForestTerminalNode(!leftResult);
        }
    }

    /**
     * Used to construct a tree directly for unit testing purposes
     * @param decisionDimension     the pieces that make up the Decision at this node
     * @param decisionPoint         the pieces that make up the Decision at this node
     * @param decisionDirection     the pieces that make up the Decision at this node
     * @param leftNode              what will the left node bring
     * @param rightNode             what will the right node bring
     * @param accuracy              the learned accuracy of this tree
     */
    protected RandomForestDecisionNode( final String decisionDimension, final Comparable decisionPoint, final boolean decisionDirection,
                                        final RandomForestNode leftNode, final RandomForestNode rightNode, final Double accuracy) {
        decision = new Decision(decisionDimension, decisionPoint, decisionDirection);
        this.leftNode = leftNode;
        this.rightNode = rightNode;
        this.accuracy = accuracy;
    }

    /**
     * Evaluate this decision point by count up the number of good/bad sites which are above/below the chosen cut point
     * @param decision  the decision point
     * @param data      the data to evaluate
     * @return          accuracy of this decision point
     */
    private double evaluateDecision( final Decision decision, final List<RandomForestDatum> data ) {
        int numGoodLower = 0;
        int numBadLower = 0;
        int numGoodUpper = 0;
        int numBadUpper = 0;

        for( final RandomForestDatum rfd : data ) {
            final Comparable comparable = rfd.annotations.get(decision.decisionDimension);
            if( comparable != null ) {
                final boolean upper = comparable.compareTo(decision.decisionPoint) > 0;
                if( upper ) {
                    if( rfd.isGood ) {
                        numGoodUpper++;
                    } else {
                        numBadUpper++;
                    }
                } else {
                    if( rfd.isGood ) {
                        numGoodLower++;
                    } else {
                        numBadLower++;
                    }
                }
            }
        }

        return (double) Math.max(numGoodLower + numBadUpper, numBadLower + numGoodUpper) / (double) (numGoodLower + numBadUpper + numBadLower + numGoodUpper);
    }

    @Override
    /**
     * Classify the data point using the visitor design pattern to walk down the decision tree
     */
    public boolean classifyDatum( final RandomForestDatum rfd ) {
        final Comparable comparable = rfd.annotations.get(decision.decisionDimension);
        if( comparable != null ) {
            if( comparable.compareTo(decision.decisionPoint) > 0 ) {
                return (decision.decisionDirection ? leftNode : rightNode).classifyDatum(rfd);
            } else {
                return (decision.decisionDirection ? rightNode : leftNode).classifyDatum(rfd);
            }
        } else {
            return (GenomeAnalysisEngine.getRandomGenerator().nextBoolean() ? leftNode : rightNode).classifyDatum(rfd); // TODO- better way to handle missing data?
        }
    }

    /**
     * Subset down the data to only that which passes the given decision direction
     * @param data      the input data that this node saw during training
     * @param wantLeft  do we want the left branch or the right branch
     * @return          non-null output list
     */
    private List<RandomForestDatum> subsetData( final List<RandomForestDatum> data, final boolean wantLeft ) {
        final List<RandomForestDatum> returnData = new ArrayList<>();
        for( final RandomForestDatum rfd : data ) {
            boolean inLeft;
            final Comparable comparable = rfd.annotations.get(decision.decisionDimension);
            if( comparable != null ) {
                if( comparable.compareTo(decision.decisionPoint) > 0 ) {
                    inLeft = decision.decisionDirection;
                } else {
                    inLeft = !decision.decisionDirection;
                }
            } else {
                inLeft = true; // TODO- better way to handle missing data?
            }

            if( inLeft == wantLeft ) {
                returnData.add(rfd);
            }
        }
        return returnData;
    }

    /**
     * Estimate the accuracy of this tree by classifying the test data and recording the result
     * @param data  the testing data, every datum should be label with isGood or isBad
     * @return  accuracy on a -1.0 to 1.0 scale
     */
    public double estimateAccuracy( final List<RandomForestDatum> data ) {
        int numCorrect = 0;
        for( final RandomForestDatum rfd : data ) {
            final boolean answer = classifyDatum(rfd);
            if( (answer && rfd.isGood) || (!answer && rfd.isBad) ) {
                numCorrect++;
            } else {
                numCorrect--;
            }
        }
        return (double) numCorrect / (double) data.size();
    }

    private class Decision {
        public final String decisionDimension;
        public final Comparable decisionPoint;
        public final boolean decisionDirection;

        public Decision( final RandomForestDatum rfd ) {
            decisionDimension = rfd.keys.get(GenomeAnalysisEngine.getRandomGenerator().nextInt(rfd.keys.size()));
            decisionPoint = rfd.annotations.get(decisionDimension);
            decisionDirection = GenomeAnalysisEngine.getRandomGenerator().nextBoolean();
        }

        public Decision( final String decisionDimension, final Comparable decisionPoint, final boolean decisionDirection ) {
            this.decisionDimension = decisionDimension;
            this.decisionPoint = decisionPoint;
            this.decisionDirection = decisionDirection;
        }
    }
}