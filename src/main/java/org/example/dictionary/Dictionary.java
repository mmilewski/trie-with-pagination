package org.example.dictionary;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Dictionary {

    public record LookupRequest(String prefix, String lastSeen, int maxResults) {
    }

    private final TrieNode root = TrieNode.createRoot();

    public Dictionary(Iterator<String> words) throws IOException {
        words.forEachRemaining(root::insert);
        System.out.println("Loading the dictionary completed");
    }

    public Dictionary(Iterable<String> words) throws IOException {
        this(words.iterator());
    }

    public Dictionary(Path filepath) throws IOException {
        this(Files.readAllLines(filepath).iterator());
    }

    /**
     * To paginate the result, provide the last seen word. Provide empty string to start from the beginning
     */
    public List<String> lookup(LookupRequest request) {
        System.out.println("Lookup request: " + request);

        if (request.prefix == null || request.prefix.isEmpty()) {
            throw new IllegalArgumentException("prefix must contain at least one character, was: " + request.prefix);
        }
        if (request.lastSeen == null) {
            throw new IllegalArgumentException("lastSeen must not be null. To start from the beginning use empty string, was: " + request.lastSeen);
        }
        if (request.maxResults < 0) {
            throw new IllegalArgumentException("maxResults must not be negative. was: " + request.maxResults);
        }

        // Find the node that starts at the prefix
        TrieNode rootForPrefix = root;
        for (char aChar : request.prefix.toCharArray()) {
            rootForPrefix = rootForPrefix.navigateToSubtree(aChar);
            // if there is no node we can step into using this character...
            if (rootForPrefix == null) {
                // ...then there are no words for the requested prefix, so we quit
                return List.of();
            }
        }

        // walk through the tree to collect words
        final List<String> result = new ArrayList<>();
        final TrieNodeVisitor visitor = createTrieVisitorThatAccumulatesWords(request, result);
        rootForPrefix.allWordsWithPrefix(request.prefix, visitor);
        return result;
    }

    private TrieNodeVisitor createTrieVisitorThatAccumulatesWords(LookupRequest request, List<String> result) {
        return new TrieNodeVisitor() {
            @Override
            public boolean shouldConsider(String prefixUnderConsideration) {
                if (collectedEnough()) {
                    return false;
                }

                // take the prefix of equal size to the prefix we are considering
                String lastSeenPrefix = request.lastSeen;
                if (lastSeenPrefix.length() > prefixUnderConsideration.length()) {
                    lastSeenPrefix = lastSeenPrefix.substring(0, prefixUnderConsideration.length());
                }

                boolean lastSeenPassedPrefix = lastSeenPrefix.compareTo(prefixUnderConsideration) > 0;
                // If lastSeen hasn't passed the prefix we are looking at, then there is a chance that it won't
                // for some letters in the subtree.
                // Also, once the lastSeen passes the prefix we are looking at, then there is no chance for sure
                return !lastSeenPassedPrefix;
            }

            @Override
            public ActionAfterVisit visit(String word) {
                if (request.lastSeen.compareTo(word) < 0) {
                    result.add(word);
                }
                if (collectedEnough()) {
                    return ActionAfterVisit.COMPLETE_EXECUTION;
                } else {
                    return ActionAfterVisit.KEEP_GOING;
                }
            }

            private boolean collectedEnough() {
                return result.size() >= request.maxResults;
            }
        };
    }
}
