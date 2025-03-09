package org.example;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glTranslatef;

public class Renderer {
    private Grid grid;
    private ClippingAlgorithm clippingAlgorithm;

    public Renderer(){

    }

    public Renderer(ClippingAlgorithm clippingAlgorithm) {
        this.grid = new Grid();
        this.clippingAlgorithm = clippingAlgorithm;
    }


    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void drawGrid(int width, int height) {
        if (grid != null) {
            grid.draw(width, height);
        }
    }
    public void drawLine(Line line) {
        glLineWidth(2.0f); // Збільшуємо товщину лінії для кращої видимості
        glBegin(GL_LINES);
        glColor3f(line.getColor()[0], line.getColor()[1], line.getColor()[2]);
        glVertex2f(line.getStart().getX(), line.getStart().getY());
        glVertex2f(line.getEnd().getX(), line.getEnd().getY());
        glEnd();

        // Малюємо точки кінців відрізка для кращої видимості
        drawPoint(line.getStart(), line.getColor(), 5.0f);
        drawPoint(line.getEnd(), line.getColor(), 5.0f);
    }

    public void drawPoint(Point point, float[] color, float size) {
        glPointSize(size);
        glBegin(GL_POINTS);
        glColor3f(color[0], color[1], color[2]);
        glVertex2f(point.getX(), point.getY());
        glEnd();
    }


    // Малювання полігону
    public void drawPolygon(Polygon polygon) {
        // Встановлення кольору полігону
        float[] color = polygon.getColor();
        glColor3f(color[0], color[1], color[2]);

        // Початок малювання полігону
        glBegin(GL_POLYGON);
        // Додавання вершин полігону
        for (Point vertex : polygon.getVertices()) {
            glVertex2f(vertex.getX(), vertex.getY());
        }

        // Завершення малювання полігону
        glEnd();
    }


    // Малювання контуру полігону (лінії)
    public void drawPolygonOutline(Polygon polygon) {
        // Встановлення кольору контуру
        float[] color = polygon.getColor();
        glPushMatrix();
        glColor3f(color[0], color[1], color[2]);

        // Положення
        glTranslatef(polygon.getPosX(),polygon.getPosY(),0.0f);

        // Встановлення товщини лінії
        glLineWidth(polygon.getPolygonLineWidth());

        // Початок малювання контуру
        glBegin(GL_LINE_LOOP);


        // Додавання вершин полігону
        for (Point vertex : polygon.getVertices()) {
            glVertex2f(vertex.getX(), vertex.getY());
        }

        // Завершення малювання контуру

        glEnd();
        glPopMatrix();
    }

    // Малювання полігону з контуром
    public void drawPolygonWithOutline(Polygon polygon) {
        // Малювання суцільного полігону
        drawPolygon(polygon);

        // Встановлення кольору контуру (можна змінити)
        float[] outlineColor = new float[]{0f, 0f, 0f}; // Чорний контур
        glColor3f(outlineColor[0], outlineColor[1], outlineColor[2]);

        // Встановлення товщини лінії
        glLineWidth(polygon.getPolygonLineWidth());

        // Початок малювання контуру
        glBegin(GL_LINE_LOOP);

        // Додавання вершин полігону
        for (Point vertex : polygon.getVertices()) {
            glVertex2f(vertex.getX(), vertex.getY());
        }

        // Завершення малювання контуру
        glEnd();
    }

    public void renderClippedLines() {
        List<Line> clippedLines = clippingAlgorithm.clipLines();
        for (Line line : clippedLines) {
            drawLine(line);
        }
    }

    // Сітка
    public Grid getGrid() {
        return grid;
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }

}