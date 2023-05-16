package uk.woolhouse.pinreal.cb;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import uk.woolhouse.pinreal.Lambda;

public class SeqCollector<T> {

    private final List<T> results = new ArrayList<>();
    private final ArrayList<Tuple<T>> callbacks = new ArrayList<>();

    public void done(T result) {
        results.add(result);
        trigger();
    }

    public void wait(int count, Lambda<List<T>> callback) {
        callbacks.add(new Tuple<T>(count, callback));
        trigger();
    }

    private void trigger() {
        var size = results.size();
        for (var element : callbacks) {
            if (size >= element.count()) {
                element.cb().call(results);
                callbacks.remove(element);
            }
        }
    }

    public static final class Tuple<T> {
        private final int count;
        private final Lambda<List<T>> cb;

        public Tuple(int count, Lambda<List<T>> cb) {
            this.count = count;
            this.cb = cb;
        }

        public int count() {
            return count;
        }

        public Lambda<List<T>> cb() {
            return cb;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Tuple) obj;
            return this.count == that.count &&
                    Objects.equals(this.cb, that.cb);
        }

        @Override
        public int hashCode() {
            return Objects.hash(count, cb);
        }

        @Override
        public String toString() {
            return "Tuple[" +
                    "count=" + count + ", " +
                    "cb=" + cb + ']';
        }
    }
}