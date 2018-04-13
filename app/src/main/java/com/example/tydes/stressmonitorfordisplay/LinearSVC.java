package com.example.tydes.stressmonitorfordisplay;

/**
 * Created by tydes on 21/03/2018.
 */

public class LinearSVC {

    private double[] coefficients;
    private double intercepts;

    public LinearSVC(double[] coefficients, double intercepts) {
        this.coefficients = coefficients;
        this.intercepts = intercepts;
    }

    //Perform vector multiplication to predict the the stress levels
    public int predict(double[] features) {
        double prob = 0.;
        for (int i = 0, il = this.coefficients.length; i < il; i++) {
            prob += this.coefficients[i] * features[i];
        }
        if (prob + this.intercepts > 0) {
            return 1;
        }
        return 0;
    }


    public static void main(String[] args) {
        if (args.length == 7) {

            // Features:
            double[] features = new double[args.length];
            for (int i = 0, l = args.length; i < l; i++) {
                features[i] = Double.parseDouble(args[i]);
            }

            // Parameters:
            double[] coefficients = {-1.36738488082, -17.6755236949, 0.0336051987855, -1.03336182252, -0.277909744711, 0.46407919792, -2.39928849844};
            double intercepts = 2.55030905175;

            // Prediction:
            LinearSVC clf = new LinearSVC(coefficients, intercepts);
            int estimation = clf.predict(features);
        }
    }
}
