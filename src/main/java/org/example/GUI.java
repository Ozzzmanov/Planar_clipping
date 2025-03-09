package org.example;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiDir;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;

public class GUI {
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private Grid grid;
    private Polygon polygon;
    private ClippingAlgorithm clippingAlgorithm;
    private int currentLineIndex = 0;
    private List<Line> lines; // Массив ліній

    private long window;

    public static final int WIDTH_TEXT_VIEW = 100;

    // Стан перетягування мишею
    private boolean dragging = false;
    private Point dragPoint = null;
    private Line dragLine = null;
    private boolean isStartPoint = false;
    private float worldMouseX, worldMouseY;

    // Чекбокси керування панелями
    private final ImBoolean editModeLine = new ImBoolean(false);
    private final ImBoolean editModeGrid = new ImBoolean(false);
    private final ImBoolean editModePolygon = new ImBoolean(false);

    // Поля для налаштування полігону
    private final float[] colorLines = {0.5f, 0.5f, 0.5f};
    private List<ImFloat> pointXInputs = new ArrayList<>();
    private List<ImFloat> pointYInputs = new ArrayList<>();
    private final ImFloat PolygonLineWidth = new ImFloat();
    private final ImFloat inputX = new ImFloat();
    private final ImFloat inputY = new ImFloat();

    // Поля введення для ліній
    private final ImFloat inputX1 = new ImFloat();
    private final ImFloat inputY1 = new ImFloat();
    private final ImFloat inputX2 = new ImFloat();
    private final ImFloat inputY2 = new ImFloat();
    private final float[] currentLineColor = {0.5f, 0.2f, 0.7f};
    private final float[] defaultLineColor = {0f, 1f, 0f};
    private int currentAlgorithmIndex = 0;
    private final String[] algorithmNames = {"Cohen-Sutherland", "Liang-Barsky" , "Cyrus-Beck"};


    // Поля для налаштування сітки
    private final ImFloat gridSizeInput = new ImFloat(0.1f);
    private final ImFloat majorLineWidthInput = new ImFloat(1.5f);
    private final ImFloat minorLineWidthInput = new ImFloat(0.8f);
    private final ImFloat axisLineWidthInput = new ImFloat(2.0f);
    private final ImBoolean showLabelsInput = new ImBoolean(true);
    private final float[] majorColorInput = {0.5f, 0.5f, 0.5f};
    private final float[] minorColorInput = {0.3f, 0.3f, 0.3f};
    private final float[] xAxisColorInput = {0.8f, 0.2f, 0.2f};
    private final float[] yAxisColorInput = {0.2f, 0.8f, 0.2f};

    public GUI(long window, Grid grid, List<Line> lines, Polygon polygon, ClippingAlgorithm clippingAlgorithm) {
        this.window = window;
        this.grid = grid;
        this.lines = lines;
        this.polygon = polygon;
        this.clippingAlgorithm = clippingAlgorithm;

        // Оновлення
        updateGridInputFields();
        updatePolygonPointInputFields();
        updateInputFields();

        // Налаштування обробників подій миші
        setupMouseCallbacks();
    }

    public void init() {
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);

        // Загрузка шрифта
        io.getFonts().clear();

        try {
            InputStream fontStream = getClass().getResourceAsStream("/fonts/Roboto-Regular.ttf");
            if (fontStream != null) {
                // Временное сохранение шрифта
                Path tempFont = Files.createTempFile("roboto", ".ttf");
                Files.copy(fontStream, tempFont, StandardCopyOption.REPLACE_EXISTING);

                io.getFonts().addFontFromFileTTF(tempFont.toString(), 18, io.getFonts().getGlyphRangesCyrillic());
                System.out.println("Шрифт успешно загружен");

                // Удаление временного файла
                Files.delete(tempFont);
            } else {
                System.err.println("Шрифт не найден в ресурсах");
            }
        } catch (IOException e) {
            System.err.println("Ошибка при загрузке шрифта: " + e.getMessage());
        }

        imGuiGlfw.init(window, true);
        imGuiGl3.init("#version 150");
    }

    public void render() {
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        renderSettingsWindow();

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    private void renderSettingsWindow(){
        ImGui.begin("Панель керування", ImGuiWindowFlags.AlwaysAutoResize);


        // Режим редагування
        ImGui.textColored(0.3f, 0.7f, 1.0f, 1.0f, "Панель керування");
        ImGui.checkbox("Налаштування сітки", editModeGrid);
        ImGui.checkbox("Налаштування полігону", editModePolygon);
        ImGui.checkbox("Налаштування відрізку", editModeLine);


        if (editModeGrid.get()) {
            ImGui.separator();
            renderGridSettingsWindow();
        }
        if (editModePolygon.get()){
            ImGui.separator();
            renderPolygonSettingsWindow();
        }
        if(editModeLine.get()){
            ImGui.separator();
            renderLineEditorWindow();
        }

        ImGui.end();
    }

    private void updatePolygonPointInputFields() {
        // Колір
        float[] colorLine = polygon.getColor();
        if(colorLine != null) System.arraycopy(colorLine, 0, colorLines, 0, 3);

        // Очистка попередніх полів
        pointXInputs.clear();
        pointYInputs.clear();

        // Вершини
        List<Point> points = polygon.getVertices();

        // Створення нових полів для кожної вершини
        for (int i = 0; i < points.size(); i++) {
            ImFloat xInput = new ImFloat(points.get(i).getX());
            ImFloat yInput = new ImFloat(points.get(i).getY());
            pointXInputs.add(xInput);
            pointYInputs.add(yInput);
        }

        // Товщина ліній
        PolygonLineWidth.set(polygon.getPolygonLineWidth());
        // Положення
        inputX.set(polygon.getPosX());
        inputY.set(polygon.getPosY());
    }

    private void updatePolygonVertex(int index, boolean isX) {
        List<Point> points = polygon.getVertices();

        if (index >= 0 && index < points.size()) {
            Point point = points.get(index);

            if (isX) {
                point.setX(pointXInputs.get(index).get() + polygon.getPosX());
            } else {
                point.setY(pointYInputs.get(index).get() + polygon.getPosY());
            }
            clippingAlgorithm.calculateClippingWindow();
        }
    }

    private void updateGridInputFields() {
        // Оновлення полів з поточних значень сітки
        gridSizeInput.set(grid.getGridSize());
        majorLineWidthInput.set(grid.getMajorLineWidth());
        minorLineWidthInput.set(grid.getMinorLineWidth());
        axisLineWidthInput.set(grid.getAxisLineWidth());
        showLabelsInput.set(grid.isShowLabels());

        // Копіювання кольорів з сітки
        float[] majorColor = grid.getMajorColor();
        float[] minorColor = grid.getMinorColor();
        float[] xAxisColor = grid.getXAxisColor();
        float[] yAxisColor = grid.getYAxisColor();

        if (majorColor != null) System.arraycopy(majorColor, 0, majorColorInput, 0, 3);
        if (minorColor != null) System.arraycopy(minorColor, 0, minorColorInput, 0, 3);
        if (xAxisColor != null) System.arraycopy(xAxisColor, 0, xAxisColorInput, 0, 3);
        if (yAxisColor != null) System.arraycopy(yAxisColor, 0, yAxisColorInput, 0, 3);
    }

    private void updateInputFields() {
        Line currentLine = getCurrentLine();
        inputX1.set(currentLine.getStart().getX());
        inputY1.set(currentLine.getStart().getY());
        inputX2.set(currentLine.getEnd().getX());
        inputY2.set(currentLine.getEnd().getY());
    }

    private void renderPolygonSettingsWindow() {
        // Колір полігону
        ImGui.textColored(0.3f, 0.7f, 1.0f, 1.0f, "Колір");
        ImGui.separator();
        if (ImGui.colorEdit3("Колір полігону", colorLines)) {
            polygon.setColor(colorLines);
        }
        ImGui.separator();
        ImGui.textColored(0.3f, 0.7f, 1.0f, 1.0f, "Вершини");
        // Положення полігону
        ImGui.text("Положення");

        ImGui.text("X:");
        ImGui.sameLine();
        ImGui.pushItemWidth(100);
        if (ImGui.inputFloat("##PointX", inputX, 0.05f, 1.0f, "%.2f")) {
            polygon.setPosX(inputX.get());
            for (int i = 0; i < polygon.getVertices().size(); i++) {
                updatePolygonVertex(i, true);
            }

        }
        ImGui.popItemWidth();

        ImGui.sameLine();
        ImGui.text("Y:");
        ImGui.sameLine();
        ImGui.pushItemWidth(100);
        if (ImGui.inputFloat("##PointY", inputY, 0.05f, 1.0f, "%.2f")) {
            polygon.setPosY(inputY.get());
            for (int i = 0; i < polygon.getVertices().size(); i++) {
                updatePolygonVertex(i, false);
            }
        }
        ImGui.popItemWidth();

        // Додати вершину
        ImGui.dummy(20, 0);
        if (ImGui.button("Створити вершину")) {
            Point newVertex = new Point(0.0f, 0.0f);
            polygon.addVertex(newVertex);

            // Поля введення для нової вершини
            pointXInputs.add(new ImFloat(newVertex.getX()));
            pointYInputs.add(new ImFloat(newVertex.getY()));
        }

        // Координати вершин
        for (int i = 0; i < polygon.getVertices().size(); i++) {
            ImGui.text("Вершина " + (i + 1) + ":");

            ImGui.text("X:");
            ImGui.sameLine();
            ImGui.pushItemWidth(100);
            if (ImGui.inputFloat("##PointX" + i, pointXInputs.get(i), 0.1f, 1.0f, "%.2f")) {
                updatePolygonVertex(i, true);
            }
            ImGui.popItemWidth();

            ImGui.sameLine();
            ImGui.text("Y:");
            ImGui.sameLine();
            ImGui.pushItemWidth(100);
            if (ImGui.inputFloat("##PointY" + i, pointYInputs.get(i), 0.1f, 1.0f, "%.2f")) {
                updatePolygonVertex(i, false);
            }
            ImGui.popItemWidth();
        }
        ImGui.separator();
        ImGui.textColored(0.3f, 0.7f, 1.0f, 1.0f, "Лінії");
        ImGui.text("Товщина ліній");
        ImGui.sameLine();
        ImGui.pushItemWidth(100);
        if (ImGui.inputFloat("##Товщина: ", PolygonLineWidth, 0.1f, 1.0f, "%.2f")) {
            polygon.setPolygonLineWidth(PolygonLineWidth.get());
        }
    }

    private void renderGridSettingsWindow() {
        ImGui.textColored(0.3f, 0.7f, 1.0f, 1.0f, "Основні параметри");
        ImGui.separator();

        // Розмір сітки
        ImGui.text("Розмір сітки:");
        ImGui.sameLine();
        ImGui.pushItemWidth(150);

        float[] gridSizeArray = new float[]{gridSizeInput.get()};
        if (ImGui.sliderFloat("##GridSize", gridSizeArray, 0.05f, 0.5f, "%.2f")) {
            gridSizeInput.set(gridSizeArray[0]);
            grid.setGridSize(gridSizeInput.get());
        }
        ImGui.popItemWidth();

        // Товщина ліній
        ImGui.text("Товщина ліній:");

        ImGui.text("  Основні:");
        ImGui.sameLine();
        ImGui.pushItemWidth(150);
        float[] majorWidthArray = new float[]{majorLineWidthInput.get()};
        if (ImGui.sliderFloat("##MajorWidth", majorWidthArray, 0.5f, 3.0f, "%.1f")) {
            majorLineWidthInput.set(majorWidthArray[0]);
            grid.setLineWidths(majorLineWidthInput.get(), minorLineWidthInput.get(), axisLineWidthInput.get());
        }
        ImGui.popItemWidth();

        ImGui.text("  Допоміжні:");
        ImGui.sameLine();
        ImGui.pushItemWidth(150);
        float[] minorWidthArray = new float[]{minorLineWidthInput.get()};
        if (ImGui.sliderFloat("##MinorWidth", minorWidthArray, 0.5f, 3.0f, "%.1f")) {
            minorLineWidthInput.set(minorWidthArray[0]);
            grid.setLineWidths(majorLineWidthInput.get(), minorLineWidthInput.get(), axisLineWidthInput.get());
        }
        ImGui.popItemWidth();

        ImGui.text("  Осі:");
        ImGui.sameLine();
        ImGui.pushItemWidth(150);
        float[] axisWidthArray = new float[]{axisLineWidthInput.get()};
        if (ImGui.sliderFloat("##AxisWidth", axisWidthArray, 0.5f, 5.0f, "%.1f")) {
            axisLineWidthInput.set(axisWidthArray[0]);
            grid.setLineWidths(majorLineWidthInput.get(), minorLineWidthInput.get(), axisLineWidthInput.get());
        }
        ImGui.popItemWidth();

        // Показувати мітки
        if (ImGui.checkbox("Показувати мітки осей", showLabelsInput)) {
            grid.setShowLabels(showLabelsInput.get());
        }

        ImGui.textColored(0.3f, 0.7f, 1.0f, 1.0f, "Кольори");
        ImGui.separator();

        // Кольори
        if (ImGui.colorEdit3("Колір основних ліній", majorColorInput)) {
            grid.setMajorColor(majorColorInput);
        }

        if (ImGui.colorEdit3("Колір допоміжних ліній", minorColorInput)) {
            grid.setMinorColor(minorColorInput);
        }

        if (ImGui.colorEdit3("Колір осі X", xAxisColorInput)) {
            grid.setXAxisColor(xAxisColorInput);
        }

        if (ImGui.colorEdit3("Колір осі Y", yAxisColorInput)) {
            grid.setYAxisColor(yAxisColorInput);
        }

    }

    private void setupMouseCallbacks() {
        // Обробник натискання кнопки миші
        glfwSetMouseButtonCallback(window, (windowHandle, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                double[] xpos = new double[1];
                double[] ypos = new double[1];
                glfwGetCursorPos(window, xpos, ypos);

                // Перетворення координат екрана в координати світу
                updateWorldMouseCoordinates(xpos[0], ypos[0]);

                if (action == GLFW_PRESS) {
                    // Перевіряємо, чи натиснули на точку
                    checkPointSelection();
                } else if (action == GLFW_RELEASE) {
                    // Завершуємо перетягування
                    dragging = false;
                    dragPoint = null;
                    dragLine = null;
                }
            }
        });

        // Обробник руху миші
        glfwSetCursorPosCallback(window, (windowHandle, xpos, ypos) -> {
            if (dragging && dragPoint != null) {
                // Перетворення координат екрана в координати світу
                updateWorldMouseCoordinates(xpos, ypos);

                // Оновлюємо позицію точки
                dragPoint.setX(worldMouseX);
                dragPoint.setY(worldMouseY);

                // Оновлюємо поля введення
                updateInputFields();
            }
        });
    }

    private void updateWorldMouseCoordinates(double xpos, double ypos) {
        int[] windowWidth = new int[1];
        int[] windowHeight = new int[1];
        glfwGetWindowSize(window, windowWidth, windowHeight);

        float screenX = (float) xpos;
        float screenY = (float) ypos;

        // Перетворення координат екрана в координати OpenGL (від -1 до 1) *Нормалізація
        float aspectRatio = (float) windowWidth[0] / windowHeight[0];
        worldMouseX = (2.0f * screenX / windowWidth[0] - 1.0f) * (aspectRatio > 1.0f ? aspectRatio : 1.0f);
        worldMouseY = (1.0f - 2.0f * screenY / windowHeight[0]) * (aspectRatio < 1.0f ? 1.0f / aspectRatio : 1.0f);
    }

    private void checkPointSelection() {
        float threshold = 0.05f;
        Line currentLine = getCurrentLine();

        // Перевіряємо точки поточної лінії
        float distStart = distanceToPoint(worldMouseX, worldMouseY, currentLine.getStart().getX(), currentLine.getStart().getY());
        float distEnd = distanceToPoint(worldMouseX, worldMouseY, currentLine.getEnd().getX(), currentLine.getEnd().getY());

        if (distStart < threshold || distEnd < threshold) {
            dragging = true;

            if (distStart < distEnd) {
                dragPoint = currentLine.getStart();
                dragLine = currentLine;
                isStartPoint = true;
            } else {
                dragPoint = currentLine.getEnd();
                dragLine = currentLine;
                isStartPoint = false;
            }
        }
    }
    // Евклідова відстань
    private float distanceToPoint(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private void renderLineEditorWindow() {
        Line currentLine = getCurrentLine();
        ImGui.textColored(0.3f, 0.7f, 1.0f, 1.0f, "Налаштування точок");

        // Кнопка для генерації випадкових ліній
        ImGui.separator();
        ImGui.dummy(20, 0);
        if (ImGui.button("Створити 10 випадкових ліній")) {
            generateRandomCoordinates(10);
            updateInputFields(); // Обновляем поля ввода
        }

        ImGui.text("Лінія:");
        ImGui.sameLine();

        // Кнопка "Попередня лінія"
        if (ImGui.arrowButton("##PrevLine", ImGuiDir.Left)) {
            currentLineIndex = Math.max(0, currentLineIndex - 1);
        }

        ImGui.sameLine();
        ImGui.text(String.format("%d / %d", currentLineIndex + 1, lines.size()));

        ImGui.sameLine();
        if (ImGui.arrowButton("##NextLine", ImGuiDir.Right)) {
            currentLineIndex = Math.min(lines.size() - 1, currentLineIndex + 1);
        }

        // Алгоритм
        ImGui.separator();
        ImGui.textColored(0.3f, 0.7f, 1.0f, 1.0f, "Алгоритм відсікання");
        ImGui.text("Алгоритм:");
        ImGui.sameLine();

        if (ImGui.arrowButton("##PrevAlg", ImGuiDir.Left)) {
            currentAlgorithmIndex = Math.max(0, currentAlgorithmIndex - 1);
            clippingAlgorithm.setCurrentAlgorithm(currentAlgorithmIndex);
        }

        ImGui.sameLine();
        ImGui.text(algorithmNames[currentAlgorithmIndex]);

        ImGui.sameLine();
        if (ImGui.arrowButton("##NextAlg", ImGuiDir.Right)) {
            currentAlgorithmIndex = Math.min(algorithmNames.length - 1, currentAlgorithmIndex + 1);
            clippingAlgorithm.setCurrentAlgorithm(currentAlgorithmIndex);
        }

        ImGui.separator();
        ImGui.textColored(0.3f, 0.7f, 1.0f, 1.0f, "Статистика алгоритму відсікання");
        ImGui.text("Використаний алгоритм: " + clippingAlgorithm.getAlgorithmName());
        ImGui.text("Опрацьовано ліній: " + clippingAlgorithm.getLines().size());
        ImGui.text("Відсічено ліній: " + clippingAlgorithm.getClippedLinesSize());
        ImGui.text("Час виконання: " + clippingAlgorithm.getDuration() + " мс");
        ImGui.separator();
        renderLineControls(currentLine, inputX1, inputY1, inputX2, inputY2, "1", "2");


        // Підказка щодо перетягування мишею
        ImGui.separator();
        ImGui.textColored(1.0f, 1.0f, 0.5f, 1.0f, "Підказка: Ви можете перетягувати точки за допомогою миші");

        // Показуємо статус перетягування
        if (dragging) {
            ImGui.text("Перетягування: " + (dragLine == currentLine ? "Відрізок 1" : "Відрізок 2") +
                    ", точка " + (isStartPoint ? "P" + (dragLine == currentLine ? "1" : "3") : "P" + (dragLine == currentLine ? "2" : "4")));
        }
    }

    private void renderLineControls(Line line, ImFloat inputX1, ImFloat inputY1, ImFloat inputX2, ImFloat inputY2, String p1Name, String p2Name) {
        float[] start = {line.getStart().getX(), line.getStart().getY()};
        float[] end = {line.getEnd().getX(), line.getEnd().getY()};

        // P1 з слайдерами та полями введення
        ImGui.text("P" + p1Name + ":");
        ImGui.pushItemWidth(200);
        if (ImGui.sliderFloat2("##Start" + p1Name, start, -1.0f, 1.0f)) {
            line.setStart(new Point(start[0], start[1]));
            inputX1.set(start[0]);
            inputY1.set(start[1]);
        }
        ImGui.popItemWidth();

        ImGui.sameLine();
        ImGui.text("X:");
        ImGui.sameLine();
        ImGui.pushItemWidth(WIDTH_TEXT_VIEW);
        if (ImGui.inputFloat("##X" + p1Name, inputX1, 0.01f, 0.1f, "%.2f")) {
            if (inputX1.get() < -1.0f) inputX1.set(-1.0f);
            if (inputX1.get() > 1.0f) inputX1.set(1.0f);
            line.getStart().setX(inputX1.get());
        }
        ImGui.popItemWidth();

        ImGui.sameLine();
        ImGui.text("Y:");
        ImGui.sameLine();
        ImGui.pushItemWidth(WIDTH_TEXT_VIEW);
        if (ImGui.inputFloat("##Y" + p1Name, inputY1, 0.01f, 0.1f, "%.2f")) {
            if (inputY1.get() < -1.0f) inputY1.set(-1.0f);
            if (inputY1.get() > 1.0f) inputY1.set(1.0f);
            line.getStart().setY(inputY1.get());
        }
        ImGui.popItemWidth();
        // P2 з слайдерами та полями введення
        ImGui.text("P" + p2Name + ":");
        ImGui.pushItemWidth(200);
        if (ImGui.sliderFloat2("##End" + p2Name, end, -1.0f, 1.0f)) {
            line.setEnd(new Point(end[0], end[1]));
            inputX2.set(end[0]);
            inputY2.set(end[1]);
        }
        ImGui.popItemWidth();

        ImGui.sameLine();
        ImGui.text("X:");
        ImGui.sameLine();
        ImGui.pushItemWidth(WIDTH_TEXT_VIEW);
        if (ImGui.inputFloat("##X" + p2Name, inputX2, 0.01f, 0.1f, "%.2f")) {
            if (inputX2.get() < -1.0f) inputX2.set(-1.0f);
            if (inputX2.get() > 1.0f) inputX2.set(1.0f);
            line.getEnd().setX(inputX2.get());
        }
        ImGui.popItemWidth();

        ImGui.sameLine();
        ImGui.text("Y:");
        ImGui.sameLine();
        ImGui.pushItemWidth(WIDTH_TEXT_VIEW);
        if (ImGui.inputFloat("##Y" + p2Name, inputY2, 0.01f, 0.1f, "%.2f")) {
            if (inputY2.get() < -1.0f) inputY2.set(-1.0f);
            if (inputY2.get() > 1.0f) inputY2.set(1.0f);
            line.getEnd().setY(inputY2.get());
        }
        ImGui.popItemWidth();
    }

    private Line getCurrentLine() {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalStateException("Список ліній пустий");
        }

        resetAllLineColors();

        lines.get(currentLineIndex).setColor(currentLineColor);
        // Ограничиваем індекс диапазоном списка
        currentLineIndex = Math.max(0, Math.min(currentLineIndex, lines.size() - 1));
        return lines.get(currentLineIndex);
    }
    private void resetAllLineColors() {
        for (int i = 0; i < lines.size(); i++) {
            lines.get(i).setColor(defaultLineColor);
        }
    }

    private void generateRandomCoordinates(int lineNumber) {
        // Об'єкт Random для генерації випадкових чисел
        Random random = new Random();

        for (int i = 0; i < lineNumber; i++) {
            // Генеруємо випадкові координати для початкової точки
            float startX = random.nextFloat() * 2.0f - 1.0f; // від -1 до 1
            float startY = random.nextFloat() * 2.0f - 1.0f; // від -1 до 1

            // Генеруємо випадкові координати для кінцевої точки
            float endX = random.nextFloat() * 2.0f - 1.0f; // від -1 до 1
            float endY = random.nextFloat() * 2.0f - 1.0f; // від -1 до 1

            // Добавляем точку
            lines.add(
                    new Line(
                            new Point(startX, startY),
                            new Point(endX, endY),
                            new float[]{0f, 1f, 0f}
                    )
            );
        }
    }

    public void dispose() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }

}
