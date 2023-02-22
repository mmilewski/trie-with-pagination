package org.example.dictionary;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.example.dictionary.TrieNodeVisitor.ActionAfterVisit.COMPLETE_EXECUTION;

/**
 * Internal implementation of the Dictionary. It's a node of the trie data structure that backs the Dictionary
 */
class TrieNode {

    // we use a sorted map to make pagination work. In pagination we can specify the last seen value,
    // from which the user request the results. Pagination only works if this structure is sorted
    final private Map<Character, TrieNode> children = new TreeMap<>();
    private boolean isWord = false;

    public boolean isWord() {
        return this.isWord;
    }

    /**
     * Creates root of a tree. Note that any node is a root of a subtree.
     */
    public static TrieNode createRoot() {
        return new TrieNode();
    }

    public void insert(String word) {
        if (word.isEmpty()) {
            isWord = true;
        } else {
            TrieNode childNode = children.get(word.charAt(0));
            if (childNode == null) {
                childNode = TrieNode.createRoot();
            }
            children.put(word.charAt(0), childNode);
            childNode.insert(word.substring(1));
        }
    }

    public TrieNode navigateToSubtree(char letter) {
        return children.get(letter);
    }

    public void allWordsWithPrefix(String prefixOfTheParentTree, TrieNodeVisitor visitor) {

        if (!visitor.shouldConsider(prefixOfTheParentTree)) {
            return; // oops, we shouldn't be here
        }

        if (this.isWord()) {
            if (visitor.visit(prefixOfTheParentTree) == COMPLETE_EXECUTION) {
                return;
            }
        }
        Set<Map.Entry<Character, TrieNode>> entries = children.entrySet();
        for (Map.Entry<Character, TrieNode> entry : entries) {
            final Character character = entry.getKey();
            final TrieNode child = entry.getValue();

            child.allWordsWithPrefix(prefixOfTheParentTree + character, visitor);
        }
    }
}
