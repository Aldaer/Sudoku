package model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

interface SudokuContainer extends SudokuElement {
    String getType();

    List<SudokuElement> getContents();

    @Override
    default void appendHtml(StringBuilder builder) {
        builder.append("<")
                .append(getType())
                .append(">");
        for (SudokuElement cell : getContents())
            cell.appendHtml(builder);
        builder.append("</")
                .append(getType())
                .append(">");
    }
}

@Getter
class SudokuContainerImpl implements SudokuContainer {
    private final String type;
    private final List<SudokuElement> contents;

    SudokuContainerImpl(String type) {
        this.type = type;
        contents = new ArrayList<>(3);
    }
}
