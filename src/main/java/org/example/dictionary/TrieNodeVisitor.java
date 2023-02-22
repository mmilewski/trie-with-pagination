package org.example.dictionary;

public interface TrieNodeVisitor {

    enum ActionAfterVisit {
        // using enum because it's easier to read and in case we want to add something in the future.
        // For now a boolean would work, too

        KEEP_GOING,
        COMPLETE_EXECUTION;
    }

    /**
     * Used to make a decision if the algorithm/visitor should even try looking at the subtree
     */
    boolean shouldConsider(String prefixUnderConsideration);

    /**
     * Visit the node and receive instructions what to do next
     *
     * @param word
     * @return
     */
    ActionAfterVisit visit(String word);

}
