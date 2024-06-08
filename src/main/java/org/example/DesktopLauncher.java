package org.example;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Memory Matrix Challenge";
        config.width = 800;
        config.height = 800;
        config.resizable = false;
        new LwjglApplication(new MemoryMatrixGame(), config);
    }
}
