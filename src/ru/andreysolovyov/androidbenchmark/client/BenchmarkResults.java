package ru.andreysolovyov.androidbenchmark.client;

import java.io.Serializable;

/**
 *
 * @author andrey
 */

public class BenchmarkResults implements Serializable {

   public BenchmarkResults() {
    }

   public BenchmarkResults(String m,
            int iop,
            int fop,
            int dop,
            int om) {
        model = m;
        intOps = iop;
        floatOps = fop;
        doubleOps = dop;
        overallMark = om;
    }

   public String model;
   public int intOps;
   public int floatOps;
   public int doubleOps;
   public int overallMark;

    public String toString() {
        return model + " " + intOps + " " + floatOps + " " + doubleOps + " " + overallMark;
    }
}
