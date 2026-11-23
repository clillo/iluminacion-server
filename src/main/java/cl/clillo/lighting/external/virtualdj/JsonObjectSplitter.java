package cl.clillo.lighting.external.virtualdj;

import java.util.ArrayList;
import java.util.List;

/**
 * Separa un flujo de texto en objetos JSON completos de primer nivel, sin depender de saltos de línea
 * y respetando las llaves que aparezcan dentro de strings.
 */
final class JsonObjectSplitter {

    private final int maxObjectLength;
    private final StringBuilder current = new StringBuilder();
    private int depth;
    private boolean inString;
    private boolean escaped;
    private boolean overflowed;

    JsonObjectSplitter(final int maxObjectLength) {
        this.maxObjectLength = maxObjectLength;
    }

    boolean takeOverflow() {
        final boolean result = overflowed;
        overflowed = false;
        return result;
    }

    List<String> feed(final char[] chars, final int length) {
        final List<String> objects = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            final char c = chars[i];

            if (depth == 0) {
                if (c == '{') {
                    depth = 1;
                    current.append(c);
                }
                continue;
            }

            current.append(c);
            if (inString) {
                if (escaped)
                    escaped = false;
                else if (c == '\\')
                    escaped = true;
                else if (c == '"')
                    inString = false;
            } else if (c == '"') {
                inString = true;
            } else if (c == '{') {
                depth++;
            } else if (c == '}' && --depth == 0) {
                objects.add(current.toString());
                current.setLength(0);
            }

            if (current.length() > maxObjectLength) {
                reset();
                overflowed = true;
            }
        }
        return objects;
    }

    private void reset() {
        current.setLength(0);
        depth = 0;
        inString = false;
        escaped = false;
    }
}
