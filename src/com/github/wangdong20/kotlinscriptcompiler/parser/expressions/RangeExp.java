package com.github.wangdong20.kotlinscriptcompiler.parser.expressions;

/**
 * range expression, 1..5, range from 1 to 5 contain 1 and 5
 */
public class RangeExp implements Exp {
    private final IntExp start;
    private final IntExp end;

    public RangeExp(IntExp start, IntExp end) {
        this.start = start;
        this.end = end;
    }

    public IntExp getStart() {
        return start;
    }

    public IntExp getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof RangeExp) {
            if(((RangeExp)obj).getStart().equals(start) && ((RangeExp)obj).getEnd().equals(end)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "RangeExp{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }
}
