package it.polito.s241876.utils;

import java.util.Arrays;
import java.util.List;

public class Action {
    public static final int ACCENSIONE = 1;
    public static final int FUNZIONAMENTO_GENERALE = 2;

    private int value;

    public Action(final int value) {
        this.value = value;
    }

    public static List<Action> values() {
        return Arrays.asList(
                new Action(ACCENSIONE),
                new Action(FUNZIONAMENTO_GENERALE)
        );
    }
}
