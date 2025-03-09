package org.example;

import java.util.ArrayList;
import java.util.List;

public class Polygon {
    private List<Point> vertices;
    private float[] color;
    private float PolygonLineWidth = 2.0f;
    private float posX = 0.0f, posY = 0.0f;

    // Конструктор за замовчуванням створює прямокутник
    public Polygon() {
        vertices = new ArrayList<>();
        // Вершини прямокутника за замовчуванням (за годинниковою стрілкою)
        vertices.add(new Point(-0.5f, -0.5f)); // Лівий нижній кут
        vertices.add(new Point(0.5f, -0.5f));  // Правий нижній кут
        vertices.add(new Point(0.5f, 0.5f));   // Правий верхній кут
        vertices.add(new Point(-0.5f, 0.5f));  // Лівий верхній кут


        // Колір за замовчуванням (синій)
        this.color = new float[]{0.0f, 0.0f, 1f};
    }

    // Конструктор з кастомними вершинами
    public Polygon(List<Point> vertices) {
        this.vertices = new ArrayList<>(vertices);
        // Колір за замовчуванням (сірий)
        this.color = new float[]{0.5f, 0.5f, 0.5f};
    }

    // Конструктор з вершинами та кольором
    public Polygon(List<Point> vertices, float[] color) {
        this.vertices = new ArrayList<>(vertices);
        this.color = color;
    }

    // Додати вершину до багатокутника
    public void addVertex(Point point) {
        vertices.add(point);
    }

    // Отримати всі вершини
    public List<Point> getVertices() {
        return new ArrayList<>(vertices);
    }

    // Встановити колір
    public void setColor(float[] color) {
        this.color = color;
    }

    // Отримати колір
    public float[] getColor() {
        return color;
    }

    // Обчислення векторного добутку для визначення орієнтації багатокутника
    private float crossProduct(Point a, Point b, Point c) {
        return (b.getX() - a.getX()) * (c.getY() - a.getY()) -
                (b.getY() - a.getY()) * (c.getX() - a.getX());
    }

    // Строкове представлення багатокутника
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Polygon: [");
        for (Point p : vertices) {
            sb.append(p).append(" ");
        }
        sb.append("]");
        return sb.toString();
    }

    public float getPolygonLineWidth() {
        return PolygonLineWidth;
    }

    public void setPolygonLineWidth(float polygonLineWidth) {
        PolygonLineWidth = polygonLineWidth;
    }

    public float getPosX() {
        return posX;
    }

    public void setPosX(float posX) {
        this.posX = posX;
    }

    public float getPosY() {
        return posY;
    }

    public void setPosY(float posY) {
        this.posY = posY;
    }
}
