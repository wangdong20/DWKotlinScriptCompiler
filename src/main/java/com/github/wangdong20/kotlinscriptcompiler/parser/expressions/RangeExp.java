package com.github.wangdong20.kotlinscriptcompiler.parser.expressions;

/**
 * range expression, 1..5, range from 1 to 5 contain 1 and 5
 */
public class RangeExp implements Exp {
    private final Exp start;
    private final Exp end;

    public RangeExp(Exp start, Exp end) {
        this.start = start;
        this.end = end;
    }

    public Exp getStart() {
        return start;
    }

    public Exp getEnd() {
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
