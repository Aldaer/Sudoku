package model;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@AllArgsConstructor
class SudokuCell implements SudokuElement {
    //                              Reserved-\   /-Definite value or 0
    //              Definite & hardcoded-\   |  |  /-"Hint On" bit and possible values (bitmask)
    private static final int DEFINITE = 0x1_000_0_000;
    private static final int HARDCODED = 0x3_000_0_000;     // Hardcoded is always definite
    private static final int CLEAR_VAL = 0xFFFF0FFF;

    static final int HINT_MASK = 0x01FF;
    static final int HINT_ON = 0x0200;

    final int index;
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

        List<String> classList = new ArrayList<>(2);
        if (isHardcoded())
            classList.add("hard");
        else {
            builder.append("onclick=\"cellClick(this)\" ");

            if (contradictsHint()) classList.add("bad");
            if (isHinted() & !isDefinite()) classList.add("hint");
        }
        if (classList.size() > 0) {
            StringJoiner sj = new StringJoiner(" ", "class=\"", "\"");
            for (String cls : classList)
                sj.add(cls);
            builder.append(sj.toString());
        }

        builder.append(">");
    }

    boolean contradictsHint() {       // Not checked for hardcoded cells
        if (!(isDefinite() && isHinted())) return false;
        return (hintBit(getDefValue()) & value) == 0;
    }

    static int hardcodedValue(int x) {
        return HARDCODED | (x << 12);
    }

    void setDefiniteValue(int newVal) {
        value &= CLEAR_VAL;
        value = newVal != 0 ? value | DEFINITE | (newVal << 12) : value & ~DEFINITE;
    }

    void reset() {
        if (!isHardcoded()) value = 0;
    }

    void activateHint() {
        value |= HINT_ON;
    }

    static int hintBit(int value) {
        return 1 << value >> 1;
    }
}
