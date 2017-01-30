package model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
class SudokuCell implements SudokuElement {
    private static final int DEFINITE = 0x10_0_000;
    private static final int HARDCODED = 0x11_0_000;
    static final int HINT_MASK = 0x01FF;
    static final int HINT_ON = 0x0200;

    private final int index;
    int value;

    boolean isDefinite() {
        return (value & DEFINITE) == DEFINITE;
    }

    private boolean isHardcoded() {
        return (value & HARDCODED) == HARDCODED;
    }

    int getDefValue() {
        return isDefinite() ? (value >> 12) & 0xF : 0;
    }

    @Override
    public void appendHtml(StringBuilder builder) {
        appendOpeningTag(builder);

        if (isDefinite())
            builder.append(getDefValue());
        else if (isHinted()) {
            int x = value;
            for (int i = 1; i < 10; i++) {
                builder.append((x & 1) > 0 ? i : " ");
                if (i == 3 || i == 6) builder.append("<br>");
                x >>= 1;
            }
        }
        builder.append("</td>");
    }

    private boolean isHinted() {
        return (value & HINT_ON) > 0;
    }

    private void appendOpeningTag(StringBuilder builder) {
        builder.append("<td id=\"")
                .append(index)
                .append("\" ");
        if (isHardcoded())
            builder.append("class=\"hard\"");
        else {
            builder.append("onclick=\"cellClick(this)\"");
            if (!isDefinite() && isHinted())
                builder.append(" class=\"hint\"");
        }
        builder.append(">");
    }

    static int hardcodedValue(int x) {
        return HARDCODED + (x << 12);
    }

    static int definiteValue(int x) {
        return DEFINITE + (x << 12);
    }
}
