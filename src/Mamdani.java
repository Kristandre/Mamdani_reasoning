import java.util.ArrayList;

/**
 * Created by Kristoffer on 27.10.2015.
 */
public class Mamdani {

    private double distance, delta;

    public Mamdani(double distance, double delta) {
        this.distance = distance;
        this.delta = delta;
    }

    public String reason(){
        //Step 1: Fuzzification
        double verySmall = reverse_grade(distance, 0, 2.5,1);
        double small = triangle(distance, 1.5, 3, 4.5, 1);
        double perfect = triangle(distance, 3.5, 5, 6.5, 1);
        double big = triangle(distance, 5.5, 7, 8.5, 1);
        double veryBig = grade(distance, 7.5, 10, 1);

        double shrinkingFast = reverse_grade(delta, -5, -2.5,1);
        double shrinking = triangle(delta, -3.5, -2, -0.5,1);
        double stable = triangle(delta, -1.5, 0, 1.5,1);
        double growing = triangle(delta, 0.5, 2, 3.5,1);
        double growingFast = grade(delta, 2.5, 5,1);

        //Step 2: Rule Evaluation
        double breakHard = 0;
        double slowDown = 0;
        double none = 0;
        double speedUp = 0;
        double floorIt = 0;

        none += Math.min(small,growing);
        slowDown += Math.min(small, stable);
        speedUp += Math.min(perfect, growing);
        if(growing == 0 && growingFast == 0) floorIt += veryBig;
        breakHard += verySmall;

        //Clipping and Step 3: Aggregation of the rule outputs
        ArrayList<Double> actionList = new ArrayList<Double>();
        for (int i = -10; i <= 10; i++) {
            if(i <= -7)actionList.add(reverse_grade(i, -10, -5,breakHard));
            else if(i <= -5)actionList.add(Math.max(reverse_grade(i, -10, -5,breakHard),triangle(i, -7, -4, -1, slowDown)));
            else if(i <= -3)actionList.add(triangle(i, -7, -4, -1, slowDown));
            else if(i <= -1)actionList.add(Math.max(triangle(i, -7, -4, -1, slowDown), triangle(i, -3, 0, 3, none)));
            else if(i <= 1)actionList.add(triangle(i, -3, 0, 3, none));
            else if(i <= 3)actionList.add(Math.max(triangle(i, 1, 4, 7, speedUp), triangle(i, -3, 0, 3, none)));
            else if(i <= 5)actionList.add(triangle(i, 1, 4, 7, speedUp));
            else if(i <= 7)actionList.add(Math.max(triangle(i, 1, 4, 7, speedUp), grade(i, 5, 10, floorIt)));
            else if(i <= 10)actionList.add(grade(i, 5, 10, floorIt));
        }

        //Step 4: Defuzzification
        double numerator = 0;
        double denominator = 0;

        int counter = -10;
        for (double d:actionList){
            numerator += counter*d;
            denominator += d;
            counter++;
        }
        double centerOfMass = numerator/denominator;
        if (centerOfMass < -6)return "BrakeHard";
        else if(centerOfMass < -2) return "SlowDown";
        else if(centerOfMass < 2) return "None";
        else if(centerOfMass < 6) return "SpeedUp";
        else if(centerOfMass <= 10) return "FloorIt";
        else return "No action found";
    }

    public static double triangle(double position, double x0, double x1, double x2, double clip) {
        double value = 0.0;
        if (position >= x0 && position <= x1) value = (position - x0) / (x1 - x0);
        else if (position >= x1 && position <= x2) value = (x2 - position) / (x1 - x0);
        if (value > clip) value = clip;
        return value;
    }
    public static double grade(double position, double x0, double x1, double clip) {
        double value = 0.0;
        if (position >= x1) value = 1.0;
        else if (position <= x0) value = 0.0;
        else value = (position - x0) / (x1 - x0);
        if (value > clip) value = clip;
        return value;
    }
    public static double reverse_grade(double position, double x0, double x1, double clip) {
        double value = 0.0;
        if (position <= x0) value = 1.0;
        else if (position >= x1) value = 0.0;
        else value = (x1 - position) / (x1 - x0);
        if (value > clip) value = clip;
        return value;
    }
}
