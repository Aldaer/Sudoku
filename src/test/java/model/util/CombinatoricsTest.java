package model.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static model.util.Combinatorics.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CombinatoricsTest {
    @Test
    public void factorials() throws Exception {
        assertThat(FACTORIALS[3], is(6));
        assertThat(FACTORIALS[5], is(120));
    }

    @Test
    public void combinations() throws Exception {
        Set<String> expected = new HashSet<>(Arrays.asList("012", "013", "014", "023", "024", "034", "123", "124", "134", "234"));

        Set<String> calculated = Arrays.stream(COMBINATIONS[5][3])
                .map(Arrays::stream)
                .collect(Collectors.mapping(is -> is.mapToObj(Integer::toString).collect(Collectors.joining()), Collectors.toSet()));

        assertThat(calculated, is(expected));
    }

    @Test
    public void bitCount() throws Exception {
        assertThat(BIT_COUNT[0b101], is(2));
        assertThat(BIT_COUNT[0b1110101], is(5));
    }
}
