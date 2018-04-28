package it.polito.s241876.utils;

import java.util.Arrays;
import java.util.List;

public class Intent {
        public static final int WELCOME = 1;
        public static final int FUNCTIONING = 2;
        public static final int PRESENCE = 3;
        public static final int ALL_ACESSORIES = 4;

        private static int value;

        static void setType(int intent){
            Intent.value = intent;
        }

        private Intent(int v) {
            Intent.value = v;
        }

        public static List<Intent> values() {
            return Arrays.asList(
                    new Intent(WELCOME),
                    new Intent(FUNCTIONING),
                    new Intent(PRESENCE),
                    new Intent(ALL_ACESSORIES)
            );
        }
}
