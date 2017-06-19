package cz.cuni.mff.xrg.odcs.commons.app.pipeline.graph;

import java.util.Iterator;
import java.util.List;

/**
 * Iterates over dependency graph in an order that satisfies all dependencies.
 * If final node is set, iteration may end before all nodes are iterated, immediately after final node is iterated.
 * 
 * @author Jan Vojt
 */
public class GraphIterator implements Iterator<ExecutedNode> {

    /**
     * Stack of nodes used to perform breath-first search.
     */
    private final List<ExecutedNode> stack;

    /**
     * Constructs iterator from dependency graph.
     * 
     * @param graph
     */
    public GraphIterator(DependencyGraph graph) {
        this.stack = graph.getStarters();
    }

    /**
     * Tells whether there are more node, that have not been used yet.
     */
    @Override
    public boolean hasNext() {
        return !stack.isEmpty();
    }

    /**
     * Returns the next node to be processed.
     */
    @Override
    public ExecutedNode next() {
        for (ExecutedNode n : stack) {
            if (n.hasMetDependencies()) {
                replaceWithDependants(n);
                return n;
            }
        }
        return null;
    }

    /**
     * Removal of dependencies is not supported for obvious reasons.
     */
    @Override
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "Operation remove is forbidden on dependency iterator.");
    }

    /**
     * Remove given node from stack and replace it with its dependants
     * 
     * @param node
     */
    private void replaceWithDependants(ExecutedNode node) {
        node.setExecuted(true);
        stack.remove(node);

        // we need to make sure dependant is not already in the stack
        for (ExecutedNode n : node.getDependants()) {
            if (!stack.contains(n)) {
                stack.add(n);
            }
        }
    }

}
