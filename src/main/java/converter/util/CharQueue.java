package converter.util;

import java.util.ArrayDeque;
import java.util.Queue;

public class CharQueue {
    private final Queue<Character> q = new ArrayDeque<>();

    public void init(String s) {
        q.clear();
        for (char c : s.toCharArray()) {
            if (!Character.isSpaceChar(c)) {
                q.offer(c);
            }
        }
    }

    public char peek() {
        return q.element();
    }

    public char poll() {
        return q.remove();
    }

    public void poll(char c) {
        if (peek() != c) throw new IllegalStateException();
        poll();
    }

    public void poll(String word) {
        for (char c : word.toCharArray()) poll(c);
    }
}
