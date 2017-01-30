package model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
class SudokuCell implements SudokuElement {
    private static final int DEFINITE = 0x10_0000;
    static final int HARDCODED = 0x11_0000;
    private final int index;
    int value = 987654321;

    boolean definite() {
        return value >= DEFINITE;
    }

    private boolean hardcoded() {
        return value >= HARDCODED;
    }

    int getDefValue() {
        return definite() ? value & 0xF : 0;
    }

    @Override
    public void appendHtml(StringBuilder builder) {
        appendOpeningTag(builder);

        if (definite())
            builder.append(getDefValue());
        else {
            int x = value;
            for (int i = 1; i < 10; i++) {
                if ((x & 1) > 0) builder.append(i);
                if (i == 3 || i == 6) builder.append("<br>");
                x >>= 1;
            }
        }
        builder.append("</td>");
    }

    private void appendOpeningTag(StringBuilder builder) {
        builder.append("<td id=\"")
                .append(index)
                .append("\" ");
        if (hardcoded())
            builder.append("class=\"hard\"");
        else {
            builder.append("onclick=\"cellClick(this)\"");
            if (!definite() && value > 0)
                builder.append(" class=\"hint\"");
        }
        builder.append(">");
    }
}
