package org.xbib.morph.fsa;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Compute additional information about an FSA: number of arcs, nodes, etc.
 */
public final class FSAInfo {
    /**
     * Computes the exact number of states and nodes by recursively traversing
     * the FSA.
     */
    private static class NodeVisitor {
        final BitSet visitedArcs = new BitSet();
        final BitSet visitedNodes = new BitSet();

        int nodes;
        int arcs;
        int totalArcs;

        private final FSA fsa;

        NodeVisitor(FSA fsa) {
            this.fsa = fsa;
        }

        public void visitNode(final int node) {
            if (visitedNodes.get(node)) {
                return;
            }
            visitedNodes.set(node);

            nodes++;
            for (int arc = fsa.getFirstArc(node); arc != 0; arc = fsa
                    .getNextArc(arc)) {
                if (!visitedArcs.get(arc)) {
                    arcs++;
                }
                totalArcs++;
                visitedArcs.set(arc);

                if (!fsa.isArcTerminal(arc)) {
                    visitNode(fsa.getEndNode(arc));
                }
            }
        }
    }

    /**
     * Computes the exact number of final states.
     */
    private static class FinalStateVisitor {
        final Map<Integer, Integer> visitedNodes = new HashMap<Integer, Integer>();

        private final FSA fsa;

        FinalStateVisitor(FSA fsa) {
            this.fsa = fsa;
        }

        public int visitNode(int node) {
            if (visitedNodes.containsKey(node)) {
                return visitedNodes.get(node);
            }

            int fromHere = 0;
            for (int arc = fsa.getFirstArc(node);
                 arc != 0; arc = fsa.getNextArc(arc)) {
                if (fsa.isArcFinal(arc)) {
                    fromHere++;
                }

                if (!fsa.isArcTerminal(arc)) {
                    fromHere += visitNode(fsa.getEndNode(arc));
                }
            }
            visitedNodes.put(node, fromHere);
            return fromHere;
        }
    }

    /**
     * Number of nodes in the automaton.
     */
    public final int nodeCount;

    /**
     * Number of arcs in the automaton, excluding an arcs from the zero node
     * (initial) and an arc from the start node to the root node.
     */
    public final int arcsCount;

    /**
     * Total number of arcs, counting arcs that physically overlap due to
     * merging.
     */
    public final int arcsCountTotal;

    /**
     * Number of final states (number of input sequences stored in the automaton).
     */
    public final int finalStatesCount;

    /**
     * Arcs size (in serialized form).
     */
    public final int size;

    /*
     *
     */
    public FSAInfo(FSA fsa) {
        final NodeVisitor w = new NodeVisitor(fsa);
        int root = fsa.getRootNode();
        if (root > 0) {
            w.visitNode(root);
        }
        this.nodeCount = 1 + w.nodes;
        this.arcsCount = 1 + w.arcs;
        this.arcsCountTotal = 1 + w.totalArcs;
        this.finalStatesCount = new FinalStateVisitor(fsa).visitNode(fsa.getRootNode());
        this.size = fsa instanceof FSA5 ? ((FSA5) fsa).arcs.length : 0;
    }

    @Override
    public String toString() {
        return "Nodes: " + nodeCount
                + ", arcs visited: " + arcsCount
                + ", arcs total: " + arcsCountTotal
                + ", final states: " + finalStatesCount
                + ", size: " + size;
    }
}