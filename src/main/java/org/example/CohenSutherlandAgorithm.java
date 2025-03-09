package org.example;

public class CohenSutherlandAgorithm {

    private static final int INSIDE = 0; // 0000
    private static final int LEFT = 1;   // 0001
    private static final int RIGHT = 2;  // 0010
    private static final int BOTTOM = 4; // 0100
    private static final int TOP = 8;    // 1000

    private float Xmin, Xmax, Ymin, Ymax;

    public CohenSutherlandAgorithm(float xmin, float xmax, float ymin, float ymax) {
        Xmin = xmin;
        Xmax = xmax;
        Ymin = ymin;
        Ymax = ymax;
    }

    // Визначення регіону точки
    private int computeRegionCode(float x, float y) {
        int code = INSIDE;

        if (x < Xmin)       // ліворуч від вікна
            code |= LEFT;
        else if (x > Xmax)  // праворуч від вікна
            code |= RIGHT;

        if (y < Ymin)       // нижче вікна
            code |= BOTTOM;
        else if (y > Ymax)  // вище вікна
            code |= TOP;

        return code;
    }

    // Алгоритм відсікання Коена-Сазерленда
    public Line clipLine(Line line) {
        Point start = line.getStart();
        Point end = line.getEnd();

        float x1 = start.getX();
        float y1 = start.getY();
        float x2 = end.getX();
        float y2 = end.getY();

        // Визначаємо регіони кінців лінії
        int code1 = computeRegionCode(x1, y1);
        int code2 = computeRegionCode(x2, y2);

        boolean accept = false;

        while (true) {

            // Лінія повністю всередині вікна
            if ((code1 | code2) == 0) {
                accept = true;
                break;
            }
            // Лінія повністю за межами вікна
            else if ((code1 & code2) != 0) {
                break;
            }
            // Лінія частково у вікні
            else {
                // Вибираємо точку за межами вікна
                int outsideCode = (code1 != 0) ? code1 : code2;

                float x = 0, y = 0;

                // Знаходимо точку перетину
                if ((outsideCode & TOP) != 0) {
                    x = x1 + (x2 - x1) * (Ymax - y1) / (y2 - y1);
                    y = Ymax;
                }
                else if ((outsideCode & BOTTOM) != 0) {
                    x = x1 + (x2 - x1) * (Ymin - y1) / (y2 - y1);
                    y = Ymin;
                }
                else if ((outsideCode & RIGHT) != 0) {
                    y = y1 + (y2 - y1) * (Xmax - x1) / (x2 - x1);
                    x = Xmax;
                }
                else if ((outsideCode & LEFT) != 0) {
                    y = y1 + (y2 - y1) * (Xmin - x1) / (x2 - x1);
                    x = Xmin;
                }

                // Оновлюємо точку та її код регіону
                if (outsideCode == code1) {
                    x1 = x;
                    y1 = y;
                    code1 = computeRegionCode(x1, y1);
                }
                else {
                    x2 = x;
                    y2 = y;
                    code2 = computeRegionCode(x2, y2);
                }
            }
        }

        // Якщо лінія видима, створюємо нову лінію
        if (accept) {
            Point newStart = new Point(x1, y1);
            Point newEnd = new Point(x2, y2);

            // Колір білий
            float[] clippedColor = new float[]{
                    1.0f,
                    1.0f,
                    1.0f
            };

            return new Line(newStart, newEnd, clippedColor);
        }

        return null; // Лінія не видима
    }

}
