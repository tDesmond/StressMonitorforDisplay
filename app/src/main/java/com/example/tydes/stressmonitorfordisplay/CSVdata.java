package com.example.tydes.stressmonitorfordisplay;

/**
 * Created by tydes on 17/03/2018.
 */

class CSVdata {
    private float col_1;
    private float col_2;

    public float getCol_1() {
        return col_1;
    }

    public void setCol_1(float col_1) {
        this.col_1 = col_1;
    }

    public float getCol_2() {
        return col_2;
    }

    public void setCol_2(float col_2) {
        this.col_2 = col_2;
    }

    @Override
    public String toString() {
        return "CSVdata{" +
                "col_1=" + col_1 +
                ", col_2=" + col_2 +
                '}';
    }
}
