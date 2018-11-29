package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;

@SuppressWarnings("IntegerDivisionInFloatingPointContext")
public class Drop extends ApplicationAdapter {
    private Texture dropImage;
    private Texture spaceshipImage;
    private Sound dropSound;
    private Music rainMusic;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Polygon shipPolygon;
    private Vector3 touchPos;
    private Array<Polygon> asteroids;
    private long lastDropTime;
    private static final int screenWidth = 1600;
    private static final int screenHeight = 960;

    public static int getScreenWidth() {
        return screenWidth;
    }

    public static int getScreenHeight() {
        return screenHeight;
    }

    @Override
    public void create() {
        // load the images for the droplet and the shipPolygon, 64x64 pixels each
        camera = new OrthographicCamera();
        camera.setToOrtho(false, screenWidth, screenHeight);
        dropImage = new Texture(Gdx.files.internal("droplet.png"));
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
        touchPos = new Vector3();
        asteroids = new Array<>();

        // load the drop sound effect and the rain background "music"
//        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
//        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

        // start the playback of the background music immediately
//        rainMusic.setLooping(true);
//        rainMusic.play();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        TextureRegion region = new TextureRegion(spaceshipImage);
//        ShapeRenderer renderer = new ShapeRenderer();
//        renderer.begin(ShapeRenderer.ShapeType.Line);
//        renderer.polygon(shipPolygon.getTransformedVertices());
//        renderer.end();
        Affine2 affine2 = new Affine2();
        affine2.rotate(shipPolygon.getRotation());

//        float x = shipPolygon.getX(), y = shipPolygon.getY(),
//                cx = shipPolygon.getBoundingRectangle().width * .5f, cy = shipPolygon.getBoundingRectangle().height * .5f;
//        shipPolygon.translate(-(x + cx), -(y + cy));
        batch.draw(region.getTexture(), shipPolygon.getX(), shipPolygon.getY(),
                shipPolygon.getBoundingRectangle().width / 2, shipPolygon.getBoundingRectangle().height / 2,
                64f, 64f,
                shipPolygon.getScaleX(), shipPolygon.getScaleY(),
                shipPolygon.getRotation(),
                region.getRegionX(), region.getRegionY(),
                region.getRegionWidth(), region.getRegionHeight(),
                false, false);
//        for(Rectangle raindrop: asteroids) {
//            batch.draw(dropImage, raindrop.x, raindrop.y);
//        }
        batch.end();

        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
//            shipPolygon.setOrigin(screenWidth/2, screenHeight/2);
//            shipPolygon.rotate(-(Gdx.graphics.getDeltaTime() / 10));
            int theta = 5;
//            int modifiedRotation = theta
            shipPolygon.rotate(theta);
            setScaling(theta);

//            shipPolygon.setScale();

//            float x = shipPolygon.getX()*MathUtils.cos(theta) - shipPolygon.getY()*MathUtils.sin(theta);
//            float y = shipPolygon.getX()*MathUtils.sin(theta) + shipPolygon.getY()*MathUtils.cos(theta);
//            //This will give you the location of a point rotated θ degrees around the origin. Since the corners of the
//            // square are rotated around the center of the square and not the origin, a couple of steps need to be added
//            // to be able to use this formula. First you need to set the point relative to the origin. Then you can use
//            // the rotation formula. After the rotation you need to move it back relative to the center of the square.
//
//            // cx, cy - center of square coordinates
//            // x, y - coordinates of a corner point of the square
//            // theta is the angle of rotation
//
//            float cx = shipPolygon.getX() + shipPolygon.getBoundingRectangle().width / 2;
//            float cy = shipPolygon.getY() + shipPolygon.getBoundingRectangle().height / 2;
//            // translate point to origin
//            float tempX = x - cx;
//            float tempY = y - cy;
//
//            // now apply rotation
//            float rotatedX = tempX* MathUtils.cos(theta) - tempY*MathUtils.sin(theta);
//            float rotatedY = tempX*MathUtils.sin(theta) + tempY*MathUtils.cos(theta);
//
//            // translate back
//            x = rotatedX + cx;
//            y = rotatedY + cy;
//            shipPolygon.setPosition(x, y);
        }
//            shipPolygon.x -= 200 * Gdx.graphics.getDeltaTime();
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
//            shipPolygon.rotate(Gdx.graphics.getDeltaTime() / 10);
            int theta = -5;
//            int modifiedRotation = theta
            shipPolygon.rotate(theta);
            setScaling(theta);
        }
//        shipPolygon.setVertices();
        shipPolygon.getBoundingRectangle().setHeight(64f);
        shipPolygon.getBoundingRectangle().setWidth(64f);
//            shipPolygon.x += 200 * Gdx.graphics.getDeltaTime();

//        if(shipPolygon.x < 0)å
//            shipPolygon.x = 0;
//        if(shipPolygon.x > screenWidth - 64)
//            shipPolygon.x = screenWidth - 64;

//        for (Iterator<Rectangle> iter = asteroids.iterator(); iter.hasNext(); ) {
//            Rectangle raindrop = iter.next();
//            raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
//            if(raindrop.y + 64 < 0)
//                iter.remove();
//            if(raindrop.overlaps(shipPolygon)) {
////                dropSound.play();
//                iter.remove();
//            }
//        }
    }

    private void setScaling(int theta) {
//        shipPolygon.set
//        int n = Math.abs(theta) % 90;
//        int distanceFrom45Intervals = Math.min(n, 90 - n);
//        shipPolygon.setScale(1 + distanceFrom45Intervals / 10f, 1 + distanceFrom45Intervals / 10f);
    }

    @Override
    public void dispose() {
        dropImage.dispose();
        spaceshipImage.dispose();
//        dropSound.dispose();
//        rainMusic.dispose();
        batch.dispose();
    }
}
