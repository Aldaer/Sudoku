package model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
class SudokuCell implements SudokuElement {
    private final int index;
    int value = 987654321;

    boolean definite() {
        return value >= 1_000_000_000;
    }

    boolean hardcoded() {
        return value >= 1_100_000_000;
    }

    int getDefValue() {
        return definite() ? value % 10 : 0;
    }

    @Override
    public void appendHtml(StringBuilder builder) {
        appendOpeningTag(builder);

        if (definite())
            builder.append(value % 10);
        else {
            int x = value;
            for (int i = 0; i < 9; i++) {
                int z = x % 10;
                if (z > 0) builder.append(z);
                if (i == 3 || i == 6) builder.append("<br>");
                x /= 10;
            }
        }
        builder.append("</td>");
    }

    private void appendOpeningTag(StringBuilder builder) {
        builder.append("<td id=\"")
                .append(index)
                .append("\" ");
        if (hardcoded())
            builder.append("class:\"hard\"");
        else {
            builder.append("onclick=\"cellClick(this)\"");
            if (!definite())
                builder.append(" class=\"hint\"");
        }
        builder.append(">");
    }
}
