package org.xbib.elements.marc.dialects.mab;

public class Periodic {

    private int base;

    private int period;

    private int maxfactor;

    public Periodic(int base, int period, int maxfactor) {
        this.base = base;
        this.period = period;
        this.maxfactor = maxfactor;
    }

    public Periodic setBase(int base) {
        this.base = base;
        return this;
    }

    public int base() {
        return base;
    }

    public Periodic setPeriodic(int periodic) {
        this.period = period;
        return this;
    }

    public boolean inPeriod(int candidate) {
        return candidate >= base && candidate < base + (maxfactor * period);
    }

    public int period(int candidate) {
        return inPeriod(candidate) ? (candidate - base) / period : -1;
    }
}
