package it.polito.s241876.utils;

import java.util.Arrays;
import java.util.List;

public class Action {
    public static final String ACCENSIONE = "accensione";
    public static final String FUNZIONAMENTO_GENERALE = "funzionamento";

    private String value;

    public Action(final String value) {
        this.value = value;
    }

    public static List<Action> values() {
        return Arrays.asList(
                new Action(ACCENSIONE),
                new Action(FUNZIONAMENTO_GENERALE)
        );
    }
}
