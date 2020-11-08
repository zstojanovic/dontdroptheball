package org.dontdroptheball.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.github.czyzby.websocket.CommonWebSockets;
import org.dontdroptheball.client.DontDropTheBall;

public class DesktopLauncher {

	public static void main(String[] args) {
		CommonWebSockets.initiate();

		Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
		configuration.setTitle("Don't drop the ball!");
		configuration.setWindowedMode(1280, 720);

		new Lwjgl3Application(new DontDropTheBall(), configuration);
	}
}