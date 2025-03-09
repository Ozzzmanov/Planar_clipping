package org.example;

import java.util.ArrayList;
import java.util.List;

public class ClippingAlgorithm {

    private float Xmin, Xmax, Ymin, Ymax;
    private Polygon polygon;
    private List<Line> lines;
    private CohenSutherlandAgorithm cohenSutherlandAgorithm;
    private LiangBarskyAgorithm liangBarskyAgorithm;
    private CyrusBeckAlgorithm cyrusBeckAlgorithm;


    // Константи для вибору алгоритму
    public static final int COHEN_SUTHERLAND = 0;
    public static final int LIANG_BARSKY = 1;
    public static final int CYRUS_BECK = 2;

    // Поточний вибраний алгоритм
    private int currentAlgorithm = COHEN_SUTHERLAND;

    private long startTime;
    private long endTime;
    private long duration;
    private String algorithmName = "";
    private int ClippedLinesSize;

    public ClippingAlgorithm(Polygon polygon, List<Line> line) {
        this.polygon = polygon;
        this.lines = line;
        calculateClippingWindow();
    }

    // Встановлення меж прямокутника відсікання
    public void calculateClippingWindow() {
        List<Point> points = polygon.getVertices();

        // Отримуємо оффсет полігону
        float posX = polygon.getPosX();
        float posY = polygon.getPosY();

        // Знаходимо мінімальні та максимальні координати
        Xmin = points.get(0).getX() + posX;
        Ymin = points.get(0).getY() + posY;
        Xmax = points.get(2).getX() + posX;
        Ymax = points.get(2).getY() + posY;

        init();
    }

    private void init (){
        cohenSutherlandAgorithm = new CohenSutherlandAgorithm(Xmin, Xmax ,Ymin ,Ymax);
        liangBarskyAgorithm = new LiangBarskyAgorithm(Xmin, Xmax ,Ymin ,Ymax);
        cyrusBeckAlgorithm = new CyrusBeckAlgorithm(polygon);
    }


    // Метод для зміни поточного алгоритму
    public void setCurrentAlgorithm(int algorithm) {
        this.currentAlgorithm = algorithm;
    }

    // Метод для обрізки всіх ліній
    public List<Line> clipLines() {
        List<Line> clippedLines = new ArrayList<>();

        startTime = System.nanoTime();


        for (Line line : lines) {
            Line clippedLine = null;

            // Вибір алгоритму відсікання
            switch (currentAlgorithm) {
                case COHEN_SUTHERLAND:
                    algorithmName = "Cohen-Sutherland";
                    clippedLine = cohenSutherlandAgorithm.clipLine(line);
                    break;
                case LIANG_BARSKY:
                    algorithmName = "Liang-Barsky";
                    clippedLine = liangBarskyAgorithm.clipLine(line);
                    break;
                case CYRUS_BECK:
                    algorithmName = "Cyrus-Beck";
                    clippedLine = cyrusBeckAlgorithm.clipLine(line);
                    break;
            }



            if (clippedLine != null) {
                clippedLines.add(clippedLine);
            }
        }

        endTime = System.nanoTime();
        duration = endTime - startTime;
        ClippedLinesSize = clippedLines.size();




        return clippedLines;
    }

    public long getDuration() {
        return duration;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public int getClippedLinesSize() {
        return ClippedLinesSize;
    }

    public List<Line> getLines() {
        return lines;
    }


}