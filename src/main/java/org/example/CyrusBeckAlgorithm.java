package org.example;


import java.util.List;

public class CyrusBeckAlgorithm {
    private Polygon polygon;

    public CyrusBeckAlgorithm(Polygon polygon) {
        this.polygon = polygon;
    }

    public Line clipLine(Line line) {
        Point start = line.getStart();
        Point end = line.getEnd();

        float x1 = start.getX();
        float y1 = start.getY();
        float x2 = end.getX();
        float y2 = end.getY();

        // Напрямний вектор відрізка
        float dx = x2 - x1;
        float dy = y2 - y1;

        // Якщо відрізок - це точка, немає сенсу його обрізати 0.000001 відстань
        if (Math.abs(dx) < 1e-6 && Math.abs(dy) < 1e-6) {
            return null;
        }

        List<Point> vertices = polygon.getVertices();
        int n = vertices.size();

        // Отримуємо оффсет полігону
        float posX = polygon.getPosX();
        float posY = polygon.getPosY();

        // Параметричні значення t для входу та виходу (Початок та кінець)
        float tEnter = 0;
        float tLeave = 1;

        // Перевіряємо кожну сторону полігону
        for (int i = 0; i < n; i++) {
            // Застосувати зсув до поточної та наступної вершин
            Point current = vertices.get(i);
            Point next = vertices.get((i + 1) % n);


            // Зсув положення багатокутника до координат вершини
            float currentX = current.getX() + posX;
            float currentY = current.getY() + posY;
            float nextX = next.getX() + posX;
            float nextY = next.getY() + posY;

            // Вектор сторони багатокутника (з урахуванням позиції)
            float edgeX = nextX - currentX;
            float edgeY = nextY - currentY;

            // Знаходимо внутрішню нормаль для опуклого багатокутника
            float normalX = edgeY;  // Поворот на 90 градусів проти годинникової стрілки
            float normalY = -edgeX;

            // Нормалізуємо нормаль
            float normalLength = (float) Math.sqrt(normalX * normalX + normalY * normalY);
            if (normalLength > 0) {
                normalX /= normalLength;
                normalY /= normalLength;
            }

            // Вектор від поточної точки багатокутника до початку відрізка (з урахуванням позиції)
            float pqX = x1 - currentX;
            float pqY = y1 - currentY;

            // Скалярні добутки
            float dotNormalDirection = normalX * dx + normalY * dy;  // N·D
            float dotNormalPQ = normalX * pqX + normalY * pqY;       // N·PQ

            // Якщо відрізок паралельний стороні
            if (Math.abs(dotNormalDirection) < 1e-6) {
                // Якщо точка зовні (відносно внутрішньої нормалі), відрізок повністю невидимий
                if (dotNormalPQ < 0) {
                    return null;
                }
                continue;
            }

            // Обчислюємо параметр t для перетину з поточною стороною
            float t = -dotNormalPQ / dotNormalDirection;

            // Оновлюємо tEnter і tLeave
            if (dotNormalDirection < 0) { // Відрізок входить у багатокутник (ззовні всередину)
                tEnter = Math.max(tEnter, t);
            } else { // Відрізок виходить з багатокутника (зсередини назовні)
                tLeave = Math.min(tLeave, t);
            }

            // Перевіряємо, чи не порожній інтервал відсікання
            if (tEnter > tLeave) {
                return null; // Відрізок невидимий
            }
        }

        // Перевіряємо, чи параметри в допустимому діапазоні
        if (tEnter > 1 || tLeave < 0) {
            return null;
        }

        // Обмежуємо параметри діапазоном [0,1]
        tEnter = Math.max(0, tEnter);
        tLeave = Math.min(1, tLeave);

        // Обчислюємо нові точки
        float newX1 = x1 + tEnter * dx;
        float newY1 = y1 + tEnter * dy;
        float newX2 = x1 + tLeave * dx;
        float newY2 = y1 + tLeave * dy;

        // Колір відсіченого відрізка
        float[] clipColor = new float[]{
                0.2f,
                0.8f,
                0.6f
        };

        return new Line(new Point(newX1, newY1), new Point(newX2, newY2), clipColor);
    }


}
