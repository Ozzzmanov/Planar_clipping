package org.example;

public class LiangBarskyAgorithm {

    private float Xmin, Xmax, Ymin, Ymax;

    public LiangBarskyAgorithm(float xmin, float xmax, float ymin, float ymax) {
        Xmin = xmin;
        Xmax = xmax;
        Ymin = ymin;
        Ymax = ymax;
    }

    public Line clipLine(Line line) {
        Point start = line.getStart();
        Point end = line.getEnd();

        float x1 = start.getX();
        float y1 = start.getY();
        float x2 = end.getX();
        float y2 = end.getY();

        // Розраховуємо параметричні направлення ліній
        float dx = x2 - x1;
        float dy = y2 - y1;

        // Параметри t, при яких лінія пересікає границі
        float tMin = 0;  // Початкове t (0 відповідає началу лінії)
        float tMax = 1;  // Кінцеве t (1 відповідає концу лінії)

        // Масиви для хранения делителей и числителей
        float[] p = new float[4];  // Делители
        float[] q = new float[4];  // Числители

        // Заполняем массивы вычисленными значениями для всех границ
        p[0] = -dx;  q[0] = x1 - Xmin;  // Левая граница
        p[1] = dx;   q[1] = Xmax - x1;  // Правая граница
        p[2] = -dy;  q[2] = y1 - Ymin;  // Нижняя граница
        p[3] = dy;   q[3] = Ymax - y1;  // Верхняя граница

        // Проверяем каждую границу
        for (int i = 0; i < 4; i++) {
            if (p[i] == 0) {
                // Линия параллельна границе
                if (q[i] < 0) {
                    // Линия полностью снаружи границы
                    return null;
                }
            } else {
                // Вычисляем значение t для пересечения с текущей границей
                float t = q[i] / p[i];

                if (p[i] < 0) {
                    // Линия входит в окно (извне внутрь)
                    if (t > tMax) {
                        // Пересечение после конца линии, линия не видима
                        return null;
                    }
                    if (t > tMin) {
                        // Обновляем tMin
                        tMin = t;
                    }
                } else {
                    // Линия выходит из окна (изнутри наружу)
                    if (t < tMin) {
                        // Пересечение до начала линии, линия не видима
                        return null;
                    }
                    if (t < tMax) {
                        // Оновлюємо tMax
                        tMax = t;
                    }
                }
            }
        }

        // Якщо tMin > tMax, лінія не пересікає окно
        if (tMin > tMax) {
            return null;
        }

        // Координати відсікання
        float clipX1 = x1 + tMin * dx;
        float clipY1 = y1 + tMin * dy;
        float clipX2 = x1 + tMax * dx;
        float clipY2 = y1 + tMax * dy;

        Point clipStart = new Point(clipX1, clipY1);
        Point clipEnd = new Point(clipX2, clipY2);

        // Колір відсікання
        float[] clipColor = new float[]{
                1.0f,
                0.0f,
                0.0f
        };

        return new Line(clipStart, clipEnd, clipColor);
    }
}
