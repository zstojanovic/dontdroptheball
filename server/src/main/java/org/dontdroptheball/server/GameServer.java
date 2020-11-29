package org.dontdroptheball.server;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import org.dontdroptheball.shared.*;
import org.dontdroptheball.shared.protocol.*;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class GameServer extends ApplicationAdapter {
	Logger logger = LoggerFactory.getLogger(GameServer.class);
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
	ServerConnectionManager socketManager;
	World world;
	Ball ball;
	Player[] players = new Player[Const.MAX_PLAYERS];
	Random random = new Random();
	Queue<ChatMessage> chatQueue = new LinkedList<>();

	@Override
	public void create() {
		logger.info("Server started");
		socketManager = new ServerConnectionManager(this);
		world = new World(Vector2.Zero, true);
		ball = new Ball(this);
	}

	@Override
	public void render() {
		var delta = Gdx.graphics.getDeltaTime();
		world.step(delta, 6, 2);
		ball.step(delta);
		for (Player player: players) {
			if (player != null) player.step(delta);
		}
		socketManager.broadcast(getState());
	}

	private GameState getState() {
		var playerStates = new ArrayList<PlayerState>(Const.MAX_PLAYERS);
		for (Player player: players) {
			if (player != null) playerStates.add(player.getState());
		}
		return new GameState(ball.getState(), playerStates);
	}

	Optional<Player> createNewPlayer(NewPlayerRequest request, WebSocket socket) {
		byte newIndex = 0;
		while (newIndex < Const.MAX_PLAYERS && players[newIndex] != null) newIndex++;
		if (newIndex == Const.MAX_PLAYERS) {
			logger.error("Too many players");
			return Optional.empty();
		}
		var name = request.name.substring(0, Math.min(request.name.length(), 10));
		var newLocation = random.nextInt(100) * Const.Path.LENGTH / 100;
		var player = new Player(newIndex, name, newLocation, world);
		players[newIndex] = player;
		var names = new String[Const.MAX_PLAYERS];
		for (int i = 0; i < Const.MAX_PLAYERS; i++)
			names[i] = players[i] == null ? null : players[i].name;
		socketManager.broadcast(new PlayerNames(names));
		socketManager.send(socket, new NewPlayerResponse(player.index, chatQueue.toArray(ChatMessage[]::new)));
		handleMessage(new ChatMessage(getTimestamp(), player.name + " joined the game\n"));
		return Optional.of(player);
	}

	void handleMessage(Player player, String message) {
		handleMessage(new ChatMessage(getTimestamp(), player.index, message));
	}

	void handleMessage(ChatMessage chatMessage) {
		chatQueue.add(chatMessage);
		if (chatQueue.size() > Const.MESSAGE_LIMIT) chatQueue.remove();
		socketManager.broadcast(chatMessage);
	}

	private String getTimestamp() {
		return formatter.format(LocalTime.now());
	}

	void disconnectPlayer(Player player) {
		socketManager.broadcast(new ChatMessage(getTimestamp(), player.name + " left the game\n"));
		players[player.index] = null;
		player.dispose();
	}

	void handleKeyEvent(Player player, KeyEvent event) {
		player.handleKeyEvent(event);
	}

	Optional<Player> getRandomPlayer() {
		var active = Arrays.stream(players).filter(Objects::nonNull).toArray();
		if (active.length == 0) return Optional.empty();
		return Optional.of((Player)active[MathUtils.random(active.length - 1)]);
	}

	public static void main(String[] args) {
		var config = new HeadlessApplicationConfiguration();
		config.renderInterval = 1 / 30f;
		new HeadlessApplication(new GameServer(), config);
	}
}
