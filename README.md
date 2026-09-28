# Lunatic

Lunatic is a small Java Swing arcade game. Move the car between lanes, avoid
incoming traffic, and beat your high score.

## Controls

- `Enter` - start the game or retry after a crash
- `Left Arrow` / `Right Arrow` - steer
- `Escape` - quit

## Run locally

The project uses only the Java standard library and has no external
dependencies. Java 8 or newer is required.

From the project root:

```powershell
New-Item -ItemType Directory -Force out | Out-Null
javac -source 8 -target 8 -d out src\Main.java src\GamePanel.java
java -cp out Main
```

The game opens in fullscreen mode. Keep `player.png`, `enemy.png`, and
`road.jpeg` in the project root when running it. If the images are unavailable,
the game falls back to simple colored shapes.

## Project structure

```text
src\
  Main.java        Application entry point and fullscreen window setup
  GamePanel.java   Game loop, rendering, controls, collisions, and scoring
player.png         Player car sprite
enemy.png          Enemy car sprite
road.jpeg          Scrolling road background
```

The local `highscore.txt` file is runtime state and is intentionally ignored by
Git.
