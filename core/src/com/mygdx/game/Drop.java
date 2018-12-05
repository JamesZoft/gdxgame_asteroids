package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("IntegerDivisionInFloatingPointContext")
public class Drop extends ApplicationAdapter {
    private Stage stage;
    private Texture spaceshipImage;
    private Sound laserSound;
    private Sound asteroidDeathSound;
    private Texture rocketTexture;
    private Texture asteroidTexture;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Polygon shipPolygon;
    private Map<Polygon, Vector2> asteroids;
    private Map<Polygon, Vector2> rockets;
    private List<Polygon> asteroidsExploded;
    private List<Polygon> rocketsExploded;
    private long lastRocketFiredTime = 0;
    private long lastAsteroidSpawnedTime = 0;
    private Vector2 velocity = new Vector2(0, 0);
    private static final int screenWidth = 1600;
    private static final int screenHeight = 960;
    private GameState state;
    private int score;
    private Label scoreText;
    private Map<Animation<TextureRegion>, Vector2> explosions;
    private Map<Animation<TextureRegion>, Float> explosionsStateTimeMap;
    private Array<TextureRegion> frames;
    private float stateTime;

    public static int getScreenWidth() {
        return screenWidth;
    }

    public static int getScreenHeight() {
        return screenHeight;
    }

    @Override
    public void create() {
        state = GameState.RUNNING;
        // load the images for the droplet and the shipPolygon, 64x64 pixels each
        stage = new Stage(new ScreenViewport());
        camera = new OrthographicCamera();
        camera.setToOrtho(false, screenWidth, screenHeight);
        spaceshipImage = new Texture(Gdx.files.internal("SpaceShip.png"));
        batch = new SpriteBatch();
        Rectangle shipRect = new Rectangle();
        shipRect.x = screenWidth / 2 - 64 / 2;
        shipRect.y = screenHeight / 2 - 20 / 2;
        shipRect.width = 64;
        shipRect.height = 64;
        shipPolygon = new Polygon(new float[] {
            shipRect.x, shipRect.y,
            shipRect.x, shipRect.y + shipRect.height,
            shipRect.x + shipRect.width, shipRect.y + shipRect.height,
            shipRect.x + shipRect.width, shipRect.y     
        });
        shipPolygon.setPosition(shipRect.x, shipRect.y);

        rocketTexture = new Texture("rocket.png");
        asteroidTexture = new Texture("asteroid.png");
        asteroids = new HashMap<>();
        rockets = new HashMap<>();
        asteroidsExploded = new ArrayList<>();
        rocketsExploded = new ArrayList<>();

        Label.LabelStyle label1Style = new Label.LabelStyle();
        label1Style.font = new BitmapFont(Gdx.files.internal("Amble-Regular-26.fnt"));
        label1Style.fontColor = Color.RED;

        scoreText = new Label("Score: 0",label1Style);
        int row_height = Gdx.graphics.getWidth() / 12;
        scoreText.setSize(Gdx.graphics.getWidth(),row_height);
//        scoreText.setPosition(0,Gdx.graphics.getHeight()-row_height*2);
        scoreText.setAlignment(Align.top);
        stage.addActor(scoreText);

        TexturePacker.Settings settings = new TexturePacker.Settings();
        settings.maxWidth = 904;
        settings.maxHeight = 904;
        settings.pot = false;

        TexturePacker.process(settings, "explosionPack", ".", "explosion");
         Texture explosionTexture = new Texture(Gdx.files.internal("explosion.png"));
        final int FRAME_COLS = 9, FRAME_ROWS = 9;
        TextureRegion[][] tmp = TextureRegion.split(explosionTexture,
                explosionTexture.getWidth() / FRAME_COLS,
                explosionTexture.getHeight() / FRAME_ROWS);
        frames = new Array<>(FRAME_COLS * FRAME_ROWS);
        for (int i = 0; i < FRAME_ROWS; i++) {
            for (int j = 0; j < FRAME_COLS; j++) {
                frames.add(tmp[i][j]);
            }
        }
        explosions = new HashMap<>();
        explosionsStateTimeMap = new HashMap<>();
        laserSound = Gdx.audio.newSound(Gdx.files.internal("laserblast.mp3"));
        asteroidDeathSound = Gdx.audio.newSound(Gdx.files.internal("asteroid_kill.mp3"));

        createAsteroid();
    }

    @Override
    public void render() {
        if (state.equals(GameState.NOT_RUNNING)) {
            return;
        }
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        scoreText.setText("Score: " + score);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        drawObject(spaceshipImage, shipPolygon, 64, 64);
        rockets.forEach((rocket, velocity) -> drawObject(rocketTexture, rocket, 16, 16));
        asteroids.forEach((asteroid, velocity) -> {
            TextureRegion region1 = new TextureRegion(asteroidTexture);
            batch.draw(region1.getTexture(), asteroid.getX(), asteroid.getY(),
                0, 0,
                64, 64,
                asteroid.getScaleX(), asteroid.getScaleY(),
                asteroid.getRotation(),
                region1.getRegionX(), region1.getRegionY(),
                region1.getRegionWidth(), region1.getRegionHeight(),
                false, false);
        });
        for (Iterator<Map.Entry<Animation<TextureRegion>, Vector2>> iterator = explosions.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<Animation<TextureRegion>, Vector2> entry = iterator.next();
            Animation<TextureRegion> explosionAnimation = entry.getKey();
            Vector2 value = entry.getValue();
            float stateTime = explosionsStateTimeMap.get(explosionAnimation) != null
                    ? explosionsStateTimeMap.get(explosionAnimation)
                    : Gdx.graphics.getDeltaTime();
            explosionsStateTimeMap.put(explosionAnimation, stateTime + Gdx.graphics.getDeltaTime()); // Accumulate elapsed animation time
            TextureRegion currentFrame = explosionAnimation.getKeyFrame(explosionsStateTimeMap.get(explosionAnimation), true);
            batch.draw(currentFrame, value.x, value.y);
            if (explosionAnimation.isAnimationFinished(explosionsStateTimeMap.get(explosionAnimation))) {
                explosions.remove(explosionAnimation);
                explosionsStateTimeMap.remove(explosionAnimation);
            }
        }
        batch.end();

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            velocity = velocity.clamp(-5, 5);
            var position = new Vector2(shipPolygon.getX(), shipPolygon.getY());
            var heading = -MathUtils.degreesToRadians * shipPolygon.getRotation();
            var acceleration = new Vector2(
                    -MathUtils.cos(heading) * 5f,
                    MathUtils.sin(heading) * 5f
            );
            velocity = velocity.add(acceleration);

            velocity = velocity.clamp(-5, 5);
            var newPos = position.add(velocity);
            shipPolygon.setPosition(newPos.x, newPos.y);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            int theta = 5;
            shipPolygon.rotate(theta);

        }
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            int theta = -5;
            shipPolygon.rotate(theta);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            if (System.currentTimeMillis() - lastRocketFiredTime > 200) {
                fireRocket();
                lastRocketFiredTime = System.currentTimeMillis();
            }
        }

        moveRockets();
        moveAsteroids();
        checkRocketAsteroidCollisions();
        dealWithRocketAsteroidCollisions();
        createAsteroid();

        if (isShipCollidedWith()) {
            Label.LabelStyle label1Style = new Label.LabelStyle();
            label1Style.font = new BitmapFont(Gdx.files.internal("Amble-Regular-26.fnt"));
            label1Style.fontColor = Color.RED;

            Label label1 = new Label("You lost!",label1Style);
            int row_height = Gdx.graphics.getWidth() / 12;
            label1.setSize(Gdx.graphics.getWidth(),row_height);
            label1.setPosition(0,Gdx.graphics.getHeight()-row_height*2);
            label1.setAlignment(Align.center);
            stage.addActor(label1);
            stage.act();
            stage.draw();
            pause();
        }

        stage.act();
        stage.draw();
    }

    private void drawObject(Texture texture, Polygon polygon, int width, int height) {
        TextureRegion region1 = new TextureRegion(texture);
        batch.draw(region1.getTexture(), polygon.getX(), polygon.getY(),
                width / 2, height / 2,
                width, height,
                polygon.getScaleX(), polygon.getScaleY(),
                polygon.getRotation(),
                region1.getRegionX(), region1.getRegionY(),
                region1.getRegionWidth(), region1.getRegionHeight(),
                false, false);
    }

    private void createAsteroid() {
        if (System.currentTimeMillis() - lastAsteroidSpawnedTime < 5000) {
            return;
        }
        Rectangle asteroidRect = new Rectangle(
                MathUtils.random(200, screenWidth - 200), MathUtils.random(200, screenHeight - 200),
                64, 64);
        Polygon asteroidPoly = new Polygon(new float[] {
                asteroidRect.x, asteroidRect.y,
                asteroidRect.x, asteroidRect.y + asteroidRect.height,
                asteroidRect.x + asteroidRect.width, asteroidRect.y + asteroidRect.height,
                asteroidRect.x + asteroidRect.width, asteroidRect.y
        });
        asteroidPoly.setPosition(asteroidRect.x, asteroidRect.y);
        asteroidPoly.setRotation(MathUtils.random(0, 360));
        asteroids.put(asteroidPoly, new Vector2(1, 1));
        lastAsteroidSpawnedTime = System.currentTimeMillis();
    }

    private void fireRocket() {
       Polygon rocketPoly = new Polygon(new float[] {
               -rocketTexture.getWidth()/2, -rocketTexture.getHeight()/2,
               -rocketTexture.getWidth()/2, rocketTexture.getHeight()/2,
               rocketTexture.getWidth()/2, rocketTexture.getHeight()/2,
               rocketTexture.getWidth()/2, -rocketTexture.getHeight()/2
        });
        rocketPoly.setPosition(shipPolygon.getX() + shipPolygon.getBoundingRectangle().width / 2,
                shipPolygon.getY() + shipPolygon.getBoundingRectangle().height);
        rocketPoly.setRotation(shipPolygon.getRotation() + 90);
        rockets.put(rocketPoly, new Vector2(0, 0));
        laserSound.play(0.05f);
    }

    private void moveRockets() {
        rockets = rockets.entrySet()
                .stream()
                .filter(e -> !isOutOfBounds(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        for (var e : rockets.entrySet()) {
            var rocket = e.getKey();
            var position = new Vector2(rocket.getX(), rocket.getY());
            var heading = MathUtils.degreesToRadians * rocket.getRotation();
            var acceleration = new Vector2(-MathUtils.sin(heading) * 1f, MathUtils.cos(heading) * 1f);

            Vector2 velocity = rockets.get(rocket).add(acceleration);

            velocity = velocity.clamp(-10, 10);
            rockets.put(rocket, velocity);
            var newPos = position.add(velocity);
            rocket.setPosition(newPos.x, newPos.y);

        }
    }

    private void moveAsteroids() {
        asteroids = asteroids.entrySet()
                .stream()
                .filter(e -> !isOutOfBounds(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        for (var e : asteroids.entrySet()) {
            var asteroid = e.getKey();
            var position = new Vector2(asteroid.getX(), asteroid.getY());

            var newPos = position.add(e.getValue());
            asteroid.setPosition(newPos.x, newPos.y);

        }
    }

    private boolean isShipCollidedWith() {
        return asteroids.entrySet()
                .stream()
                .anyMatch(entry -> arePolygonsIntersecting(shipPolygon, entry.getKey()));
    }

    private void checkRocketAsteroidCollisions() {
        asteroids.forEach((asteroid, velocity) -> {
            rockets.entrySet()
                .stream()
                .filter(entry -> arePolygonsIntersecting(asteroid, entry.getKey()))
                .findAny()
                .ifPresent(entry -> {
                    var rocket = entry.getKey();
                    rocketsExploded.add(rocket);
                    asteroidsExploded.add(asteroid);
                    explosions.put(new Animation<>(0.012346f, frames, Animation.PlayMode.NORMAL),
                        new Vector2(rocket.getX() + rocket.getBoundingRectangle().width/2,
                                    rocket.getY() + rocket.getBoundingRectangle().height));
                    asteroidDeathSound.play(0.2f);
                });
        });
    }

    private boolean arePolygonsIntersecting(Polygon a, Polygon b) {
        return a.getX() < b.getX() + b.getBoundingRectangle().width
                && a.getX() + a.getBoundingRectangle().width > b.getX()
                && a.getY() < b.getY() + b.getBoundingRectangle().height
                && a.getY() + a.getBoundingRectangle().height > b.getY();
    }

    private void dealWithRocketAsteroidCollisions() {
        rocketsExploded.forEach(rockets::remove);
        rocketsExploded.clear();
        asteroidsExploded.stream()
            .map(asteroids::remove)
            .forEach(asteroid -> score++);
        asteroidsExploded.clear();
    }

    private boolean isOutOfBounds(Polygon p) {
        return p.getX() > screenWidth || p.getX() < 0
            || p.getY() > screenHeight || p.getY() < 0;
    }

    @Override
    public void resume() {
        state = GameState.RUNNING;
    }

    @Override
    public void pause() {
        state = GameState.NOT_RUNNING;
    }

    @Override
    public void dispose() {
        stage.dispose();
        asteroidTexture.dispose();
        rocketTexture.dispose();
        spaceshipImage.dispose();
        batch.dispose();
    }
}
