package bearmaps.utils.trie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MyTrieSet implements TrieSet {
    private TrieNode root;

    public MyTrieSet() {
        root = new TrieNode(' ');
    }

    /**
     * Clears all items out of Trie
     */
    @Override
    public void clear() {
        root = new TrieNode(' ');
    }

    /**
     * Returns true if the Trie contains KEY, false otherwise
     *
     * @param key They key to find in this Trie
     */
    @Override
    public boolean contains(String key) {
        TrieNode curr = root;
        for (int i = 0; i < key.length(); i++) {
            curr = curr.findChildren(key.charAt(i));
            if (curr == null) {
                return false;
            }
        }
        return curr.isWordEnd;
    }

    /**
     * Inserts string KEY into Trie
     *
     * @param key the string to insert into this trie
     */
    @Override
    public void add(String key) {
        if (key == null) {
            return;
        }
        TrieNode curr = root;
        for (int i = 0; i < key.length(); i++) {
            curr = curr.addOrGet(key.charAt(i));
        }
        curr.isWordEnd = true;
    }

    /**
     * Returns a list of all words that start with PREFIX
     *
     * @param prefix Find all strings starting with {@code prefix}
     */
    @Override
    public List<String> keysWithPrefix(String prefix) {
        TrieNode curr = root;
        for (int i = 0; i < prefix.length(); i++) {
            curr = curr.findChildren(prefix.charAt(i));
            if (curr == null) {
                return new ArrayList<>();
            }
        }
        List<String> afterwards = curr.getChildren();
        return afterwards.stream().parallel().map((s) -> prefix + s)
                .collect(Collectors.toList());
    }

    /**
     * Returns the longest prefix of KEY that exists in the Trie
     * Not required for Lab 18. If you don't implement this, throw an
     * UnsupportedOperationException.
     *
     * @param key The key to lookup in the trie.
     */
    @Override
    public String longestPrefixOf(String key) {
        String currLongest = "";
        StringBuilder wordCounter = new StringBuilder();
        TrieNode curr = root;
        for (int i = 0; i < key.length(); i++) {
            curr = curr.findChildren(key.charAt(i));
            if (curr == null) {
                break;
            }
            wordCounter.append(curr.currChar);
            if (curr.isWordEnd) {
                currLongest = wordCounter.toString();
            }
        }
        return currLongest;
    }

    class TrieNode {
        char currChar;
        Map<Character, TrieNode> children;
        boolean isWordEnd;

        TrieNode(char c) {
            this(c, false);
        }

        TrieNode(char c, boolean isend) {
            this.currChar = c;
            children = new HashMap<>();
            isWordEnd = isend;
        }

        // Concatenate children and return a list of string, not including itself.
        public List<String> getChildren() {
            List<String> tmp = new ArrayList<>();
            if (this.isWordEnd) {
                tmp.add("");
            }
            for (TrieNode child : children.values()) {
                tmp.addAll(child.getChildren(""));
            }
            return tmp;
        }

        public TrieNode findChildren(char k) {
            return children.getOrDefault(k, null);
        }

        public List<String> getChildren(String prefix) {
            List<String> tmp = new ArrayList<>();
            String wordToMe = prefix + currChar;
            if (this.isWordEnd) {
                tmp.add(wordToMe);
            }
            for (TrieNode child : children.values()) {
                tmp.addAll(child.getChildren(wordToMe));
            }
            return tmp;
        }

        /**
         * Add child, or get child if already exist
         *
         * @param c
         */
        TrieNode addOrGet(char c) {
            if (children.containsKey(c)) {
                return children.get(c);
            } else {
                children.put(c, new TrieNode(c));
                return children.get(c);
            }
        }
    }
}
