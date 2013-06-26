package com.tvganesh.chain;

/*
 *  Simulation of a Chain in Android
 *  Designed & developed by Tinniam V Ganesh, 26 Jun 2013
 *  Uses AndEngine & Box2D physics
 */

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.HorizontalAlign;


import android.graphics.Typeface;
import android.hardware.SensorManager;


import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;

import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;



public class Chain extends SimpleBaseGameActivity implements  IAccelerationListener, IOnSceneTouchListener {
	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;
	public static final float PIXEL_TO_METER_RATIO_DEFAULT = 32.0f;
	
    
    private Scene mScene;
    
    private PhysicsWorld mPhysicsWorld;
    
	private BitmapTextureAtlas mBitmapTextureAtlas;   
	private TextureRegion mBallTextureRegion, mLinkTextureRegion;
	private TextureRegion mWallTextureRegion;
	private TextureRegion mFloorTextureRegion;
	private TextureRegion mMonkeyTextureRegion;
	
	Rectangle r;
	Rectangle ground,roof,left,right;
	static Sprite lWall,rWall;
	Body lWallBody,rWallBody;
	static Sprite floor;
	Body floorBody;

	final FixtureDef gameFixtureDef = PhysicsFactory.createFixtureDef(10f, 0.0f, 0.2f);

    private static FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(50f, 0.0f, 0.5f);
    private static FixtureDef MONKEY_FIXTURE_DEF = PhysicsFactory.createFixtureDef(2f, 0.0f, 0.5f);
    Sprite monkey;
	Body monkeyBody;
	static Font mFont;
	static Text bText;

    public EngineOptions onCreateEngineOptions() {
		
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}
	
	public void onCreateResources() {
		
	
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");	
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 768, 538, TextureOptions.BILINEAR);		
		this.mBallTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "ball.png", 0, 0);
		this.mLinkTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "link.png", 8, 8);
		this.mWallTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "wall.png", 13, 18);		
		this.mMonkeyTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "monkey.jpg", 18, 498);		
		this.mFloorTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "floor.png", 48, 533);		
		this.mBitmapTextureAtlas.load();
		
	
	}
	
	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		


		this.mScene = new Scene();
		this.mScene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));
		this.mScene.setOnSceneTouchListener(this);
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_JUPITER), false);
		bText = new Text(100, 20, this.mFont, "Kreegah! Bundolo!", new TextOptions(HorizontalAlign.CENTER), this.getVertexBufferObjectManager());
		this.mScene.attachChild(bText);
		bText.setScale((float)0.75);
		this.initChain(mScene);
		this.mScene.registerUpdateHandler(this.mPhysicsWorld);
	
		return mScene;		
		
	}
	

	public void initChain(Scene mScene){
		Sprite circle1;
		Body circleBody1;
		
	
		RevoluteJointDef revoluteJointDef;

		int nBodies = 55;
		final float PI=3.1415f;
		double x[] = new double[nBodies];
		double y[] = new double[nBodies];
		Sprite link[] = new Sprite[nBodies];
		Body linkBody[] = new Body[nBodies];
		
	
		//Create the floor		
		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
		ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		roof = new Rectangle(0, 0, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		left = new Rectangle(0, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
		right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);

		
		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.0f, 0.2f);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);
       
        
		this.mScene.attachChild(ground);
		this.mScene.attachChild(roof);
		this.mScene.attachChild(left);
		this.mScene.attachChild(right);

		// Create the left wall - Collisions happen between bodies
		lWall = new Sprite(0, 0, this.mWallTextureRegion, this.getVertexBufferObjectManager());
		lWallBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, lWall, BodyType.StaticBody, wallFixtureDef);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(lWall, lWallBody, true, true));
		this.mScene.attachChild(lWall);
		
		// Create right wall - Collisions happen between bodies
		rWall = new Sprite(715, 0, this.mWallTextureRegion, this.getVertexBufferObjectManager());
		rWallBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, rWall, BodyType.StaticBody, wallFixtureDef);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(rWall, rWallBody, true, true));
		this.mScene.attachChild(rWall);
		
		// Create floor
		floor = new Sprite(0,475, this.mFloorTextureRegion, this.getVertexBufferObjectManager());
		floorBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, floor, BodyType.StaticBody, wallFixtureDef);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(floor, floorBody, true, true));
		this.mScene.attachChild(floor);
		
		//Create an anchor point
		circle1 = new Sprite(360, 20, this.mBallTextureRegion, this.getVertexBufferObjectManager());
		circleBody1 = PhysicsFactory.createCircleBody(this.mPhysicsWorld, circle1, BodyType.StaticBody, FIXTURE_DEF);
		this.mScene.attachChild(circle1);
	    
		double start = 360.0;
		x[0] = 360;
		y[0] = 20;
	
		link[0] = new Sprite((float) 360, (float) 20, this.mLinkTextureRegion, this.getVertexBufferObjectManager());
		linkBody[0] = PhysicsFactory.createBoxBody(this.mPhysicsWorld, link[0],  BodyType.DynamicBody, FIXTURE_DEF);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(link[0], linkBody[0], true, true));
		this.mScene.attachChild(link[0]);
		
		//Attach first link to anchor point (static body)
		revoluteJointDef = new RevoluteJointDef();
		revoluteJointDef.initialize(circleBody1, linkBody[0], circleBody1.getWorldCenter());
		revoluteJointDef.enableMotor = false;
		revoluteJointDef.motorSpeed = 0;
		revoluteJointDef.maxMotorTorque = 0;
		this.mPhysicsWorld.createJoint(revoluteJointDef);
	
		// Add links to chain with revolute joint between each link
		for(int i =1; i < nBodies ; i++){
			x[i] = x[i-1] + 8 * Math.cos(PI/4);
			y[i] = y[i-1] + 8 * Math.sin(PI/4);
			link[i] = new Sprite((float) x[i], (float) y[i], this.mLinkTextureRegion, this.getVertexBufferObjectManager());
			linkBody[i] = PhysicsFactory.createBoxBody(this.mPhysicsWorld, link[i],  BodyType.DynamicBody, FIXTURE_DEF);
			this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(link[i], linkBody[i], true, true));
			this.mScene.attachChild(link[i]);
			 revoluteJointDef = new RevoluteJointDef();
			 revoluteJointDef.initialize(linkBody[i], linkBody[i-1], linkBody[i-1].getWorldCenter());
			 revoluteJointDef.enableMotor = false;
			 revoluteJointDef.motorSpeed = 0;
			 revoluteJointDef.maxMotorTorque = 0;
			 this.mPhysicsWorld.createJoint(revoluteJointDef);
		
		
		}
		int i = nBodies -1;
		
		 // Suspend a monkey to last link
		 monkey = new Monkey((float)(x[i]+10), (float)(y[i]+10), this.mMonkeyTextureRegion, this.getVertexBufferObjectManager(), mPhysicsWorld);
		 monkeyBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld, monkey, BodyType.DynamicBody, MONKEY_FIXTURE_DEF);
		 this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(monkey, monkeyBody, true, true));
		 this.mScene.attachChild(monkey);
		 revoluteJointDef = new RevoluteJointDef();
		 revoluteJointDef.initialize(monkeyBody, linkBody[i], linkBody[i].getWorldCenter());
		 revoluteJointDef.enableMotor = false;
		 revoluteJointDef.motorSpeed = 0;
		 revoluteJointDef.maxMotorTorque = 0;
		 this.mPhysicsWorld.createJoint(revoluteJointDef);

		
		this.mScene.registerUpdateHandler(this.mPhysicsWorld);
		
	}

	@Override
	public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {
		// TODO Auto-generated method stub
		
	}	

	@Override
	public void onAccelerationChanged(AccelerationData pAccelerationData) {
		final Vector2 gravity = Vector2Pool.obtain(pAccelerationData.getX(), pAccelerationData.getY());
		this.mPhysicsWorld.setGravity(gravity);
		Vector2Pool.recycle(gravity);
		
	}


	@Override
	public void onResumeGame() {
		super.onResumeGame();

		this.enableAccelerationSensor(this);

	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();

		this.disableAccelerationSensor();
	}	


	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if(this.mPhysicsWorld != null) {
			if(pSceneTouchEvent.isActionDown()) {
				//this.addBall(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
				return true;
			}
		}
		return false;
	}
	
	private static class Monkey extends Sprite {
		private final PhysicsHandler mPhysicsHandler;
		  float x,y;
		
		public Monkey(final float pX, final float pY, final TextureRegion pTextureRegion, final VertexBufferObjectManager pVertexBufferObjectManager,PhysicsWorld mPW) {
			super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
			this.mPhysicsHandler = new PhysicsHandler(this);
			this.registerUpdateHandler(this.mPhysicsHandler);
			
			
		}
		
		//Detect collisions with walls & floor
		@Override
		protected void onManagedUpdate(final float pSecondsElapsed) {
			// Check collisions
			if(rWall.collidesWith(this) ){
              
				x = this.getX();
				y = this.getY();
				bText.setPosition(x-40,y - 20);
				bText.setText("Bonk!");
                                 
			}
			if(lWall.collidesWith(this)){
				
				x = this.getX();
				y = this.getY();
				bText.setPosition(x + 20,y - 20);
				bText.setText("Thump!");
			}
			
			if(floor.collidesWith(this)){
				x = this.getX();
				y = this.getY();
				bText.setPosition(x -  20,y - 20);
				bText.setText("Thud!");				
			}
			
			
		}
	}
	
	

	
}	