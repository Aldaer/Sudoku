package model.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static model.util.Combinatorics.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class CombinatoricsTest {
    @Test
    public void factorials() throws Exception {
        assertThat(FACTORIALS[3], is(6));
        assertThat(FACTORIALS[5], is(120));
    }

    @Test
    public void combinations() throws Exception {
        List<String> expected = Arrays.asList("012", "013", "014", "023", "024", "034", "123", "124", "134", "234");

        List<String> calculated = Arrays.stream(COMBINATIONS[5][3])
                .map(Arrays::stream)
                .collect(Collectors.mapping(is -> is.mapToObj(Integer::toString).collect(Collectors.joining()), Collectors.toList()));

        assertThat(calculated, is(expected));
    }

    @Test
    public void antiCombinations() throws Exception {
        List<String> expected = Arrays.asList("34", "24", "23", "14", "13", "12", "04", "03", "02", "01");

        List<String> calculated = Arrays.stream(ANTI_COMBINATIONS[5][3])
                .map(Arrays::stream)
                .collect(Collectors.mapping(is -> is.mapToObj(Integer::toString).collect(Collectors.joining()), Collectors.toList()));

        assertThat(calculated, is(expected));

        List<String> complement = Arrays.stream(COMBINATIONS[5][2])
                .map(Arrays::stream)
                .collect(Collectors.mapping(is -> is.mapToObj(Integer::toString).collect(Collectors.joining()), Collectors.toList()));

        assertNotEquals(calculated, complement);
        assertEquals(new HashSet<>(calculated), new HashSet<>(complement));     // same elements but in other order
    }

    @Test
    public void bitCount() throws Exception {
        assertThat(BIT_COUNT[0b101], is(2));
        assertThat(BIT_COUNT[0b1110101], is(5));
    }
}
