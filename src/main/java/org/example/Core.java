package org.example;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Core {
    private long window;
    private int WIDTH = 800;
    private int HEIGHT = 600;
    private Renderer renderer;
    private Polygon polygon;
    private Grid grid;
    private ClippingAlgorithm clippingAlgorithm;
    private GUI gui;

    private List<Line> lines;

    public void run() {
        init();
        loop();

        // Звільняємо ресурси
        if (gui != null) {
            gui.dispose();
        }

        // Звільняємо вікно та колбеки
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Завершуємо GLFW
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Налаштування обробника помилок
        GLFWErrorCallback.createPrint(System.err).set();

        // Ініціалізація GLFW
        if (!glfwInit())
            throw new IllegalStateException("Помилка ініціалізації GLFW");

        // Налаштування GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // Створення вікна
        window = glfwCreateWindow(WIDTH, HEIGHT, "Аналізатор відрізків", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Помилка створення GLFW вікна");

        // Налаштування колбеку клавіатури
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
        });

        // Налаштування колбеку зміни розміру вікна
        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            WIDTH = width;
            HEIGHT = height;
            glViewport(0, 0, width, height);
        });

        // Отримуємо потік для управління пам'яттю стеку
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            // Отримуємо розмір вікна
            glfwGetWindowSize(window, pWidth, pHeight);

            // Отримуємо роздільну здатність основного монітора
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Центруємо вікно
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        // Робимо контекст OpenGL поточним
        glfwMakeContextCurrent(window);
        // Вмикаємо вертикальну синхронізацію
        glfwSwapInterval(1);
        // Показуємо вікно
        glfwShowWindow(window);

        // Створюємо можливості OpenGL
        GL.createCapabilities();



        lines = new ArrayList<>(Arrays.asList(
                new Line(
                        new Point(-0.1f, -0.7f),
                        new Point(0.1f, 0.7f),
                        new float[]{0f, 1f, 0f}
                ),
                new Line(
                        new Point(-0.5f, -0.9f),
                        new Point(0.3f, 0.8f),
                        new float[]{0f, 1.0f, 0f}
                ))

        );

        polygon = new Polygon();

        clippingAlgorithm = new ClippingAlgorithm(polygon,lines);

        // Init рендер
        renderer = new Renderer(clippingAlgorithm);

        // Сітка
        grid = new Grid();

        // Ініціалізація GUI
        gui = new GUI(window, grid, lines, polygon, clippingAlgorithm);
        gui.init();
    }

    private void loop() {
        // Встановлюємо колір очищення
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        // Основний цикл рендерингу
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // Оновлюємо viewport і матрицю проекції
            updateViewport();

            renderer.setGrid(grid);
            renderer.drawGrid(WIDTH, HEIGHT);
//            renderer.drawPolygon(polygon); // Суцільний полігон
//            renderer.drawPolygonWithOutline(polygon); // Полігон і чорний контур навколо нього
            renderer.drawPolygonOutline(polygon); // Контур полігону

            for(Line line : lines){
                renderer.drawLine(line);
            }


            renderer.renderClippedLines();
            gui.render();


            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void updateViewport() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        float aspectRatio = (float) WIDTH / HEIGHT;
        if (WIDTH > HEIGHT) {
            // Широке вікно
            glOrtho(-aspectRatio, aspectRatio, -1.0f, 1.0f, -1.0f, 1.0f);
        } else {
            // Високе вікно
            glOrtho(-1.0f, 1.0f, -1.0f / aspectRatio, 1.0f / aspectRatio, -1.0f, 1.0f);
        }

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
    }

    public static void main(String[] args) {
        new Core().run();
    }
}