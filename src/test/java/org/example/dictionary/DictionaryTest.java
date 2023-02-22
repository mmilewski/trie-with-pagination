package org.example.dictionary;

import org.example.dictionary.Dictionary.LookupRequest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DictionaryTest {

    @Test
    void single_word_lookup_of_empty_prefix_is_illegal() throws IOException {
        Dictionary dict = new Dictionary(List.of("foo"));

        assertThrows(IllegalArgumentException.class, () -> lookupWithoutPagination(dict, ""));
    }

    @Test
    void single_word_can_find_word_in_a_single_word_dict() throws IOException {
        Dictionary dict = new Dictionary(List.of("foo"));
        assertEquals(List.of("foo"), lookupWithoutPagination(dict, "foo"));
    }

    @Test
    void single_word_returns_words_with_prefix() throws IOException {
        Dictionary dict = new Dictionary(List.of("foo"));

        assertEquals(List.of("foo"), lookupWithoutPagination(dict, "f"));
        assertEquals(List.of("foo"), lookupWithoutPagination(dict, "fo"));
    }

    @Test
    void same_prefix_returns_all_words() throws IOException {
        Dictionary dict = new Dictionary(List.of("tablet", "tablets", "tableting", "tabletting"));

        assertEquals(List.of("tablet", "tableting", "tablets", "tabletting"), lookupWithoutPagination(dict, "t"));
        assertEquals(List.of("tablet", "tableting", "tablets", "tabletting"), lookupWithoutPagination(dict, "ta"));
        assertEquals(List.of("tablet", "tableting", "tablets", "tabletting"), lookupWithoutPagination(dict, "tab"));
        assertEquals(List.of("tablet", "tableting", "tablets", "tabletting"), lookupWithoutPagination(dict, "tabl"));
        assertEquals(List.of("tablet", "tableting", "tablets", "tabletting"), lookupWithoutPagination(dict, "table"));
        assertEquals(List.of("tablet", "tableting", "tablets", "tabletting"), lookupWithoutPagination(dict, "tablet"));
        assertEquals(List.of("tabletting"), lookupWithoutPagination(dict, "tablett"));
        assertEquals(List.of("tabletting"), lookupWithoutPagination(dict, "tabletti"));
        assertEquals(List.of("tabletting"), lookupWithoutPagination(dict, "tablettin"));
        assertEquals(List.of("tabletting"), lookupWithoutPagination(dict, "tabletting"));
    }

    @Test
    void only_first_letter_differs() throws IOException {
        Dictionary dict = new Dictionary(List.of("cat", "mat", "pat", "fat"));

        assertEquals(List.of("cat"), lookupWithoutPagination(dict, "cat"));
        assertEquals(List.of("fat"), lookupWithoutPagination(dict, "fat"));
        assertEquals(List.of("pat"), lookupWithoutPagination(dict, "pat"));
        assertEquals(List.of("mat"), lookupWithoutPagination(dict, "mat"));
    }

    @Test
    void lookup_word_does_not_exist() throws IOException {
        Dictionary dict = new Dictionary(List.of("tablets", "tablet", "tableting", "tabletting"));

        assertEquals(List.of(), lookupWithoutPagination(dict, "tabletomania"));
    }


    private List<String> lookupWithoutPagination(Dictionary dict, String lookupWord) {
        return dict.lookup(new LookupRequest(lookupWord, "", 10));
    }

    @Test
    void paginated_validate_input() throws IOException {
        Dictionary dict = new Dictionary(List.of("foo"));

        assertThrows(IllegalArgumentException.class, () -> dict.lookup(new LookupRequest("p", null, 10)));
        assertThrows(IllegalArgumentException.class, () -> dict.lookup(new LookupRequest("p", "", -1)));
        assertThrows(IllegalArgumentException.class, () -> dict.lookup(new LookupRequest("p", "", -10)));
        assertThrows(IllegalArgumentException.class, () -> dict.lookup(new LookupRequest("p", "last", -10)));
    }


    @Test
    void paginated_respects_maxResults() throws IOException {
        Dictionary dict = new Dictionary(List.of("tablets", "tableting", "tablet", "tabletting"));

        assertEquals(List.of("tablet"), dict.lookup(new LookupRequest("t", "", 1)));
        assertEquals(List.of("tablet", "tableting"), dict.lookup(new LookupRequest("t", "", 2)));
    }


    @Test
    void paginated_respects_lastSeen() throws IOException {
        Dictionary dict = new Dictionary(List.of("xbb", "xcc", "xdd", "xaa"));

        assertEquals(List.of("xaa", "xbb"), dict.lookup(new LookupRequest("x", "", 2)));
        assertEquals(List.of("xaa", "xbb", "xcc", "xdd"), dict.lookup(new LookupRequest("x", "", 4)));
        assertEquals(List.of("xbb", "xcc"), dict.lookup(new LookupRequest("x", "xaa", 2)));
        assertEquals(List.of("xbb", "xcc", "xdd"), dict.lookup(new LookupRequest("x", "xaa", 3)));
        assertEquals(List.of("xcc", "xdd"), dict.lookup(new LookupRequest("x", "xbb", 2)));
        assertEquals(List.of("xcc", "xdd"), dict.lookup(new LookupRequest("x", "xbb", 3)));
        assertEquals(List.of("xdd"), dict.lookup(new LookupRequest("x", "xcc", 2)));
        assertEquals(List.of(), dict.lookup(new LookupRequest("x", "xdd", 2)));

        assertEquals(List.of("xaa"), dict.lookup(new LookupRequest("xaa", "", 2)));
        assertEquals(List.of(), dict.lookup(new LookupRequest("xaa", "xaa", 2)));
        assertEquals(List.of(), dict.lookup(new LookupRequest("xbb", "xbb", 2)));
    }

    @Test
    void paginated_respects_lastSeen_longer_words() throws IOException {
        Dictionary dict = new Dictionary(List.of(
                "piggy", "piggyback", "piggybacked", "piggybacking", "piggybacks"
        ));

        assertEquals(List.of(), dict.lookup(new LookupRequest("piggy", "piggybacks", 2)));
    }


}