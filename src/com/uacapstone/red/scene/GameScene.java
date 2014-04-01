package com.uacapstone.red.scene;

import java.io.IOException;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.LoopEntityModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.extension.debugdraw.DebugRenderer;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.util.SAXUtils;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;
import org.andengine.util.level.EntityLoader;
import org.andengine.util.level.constants.LevelConstants;
import org.andengine.util.level.simple.SimpleLevelEntityLoaderData;
import org.andengine.util.level.simple.SimpleLevelLoader;
import org.xml.sax.Attributes;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.uacapstone.red.base.BaseScene;
import com.uacapstone.red.manager.SceneManager;
import com.uacapstone.red.manager.SceneManager.SceneType;
import com.uacapstone.red.object.Player;

public class GameScene extends BaseScene implements IOnSceneTouchListener
{
    @Override
    public void createScene()
    {
        createBackground();
        createHUD();
        createPhysics();
        loadLevel(1);
        createGameOverText();
        setOnSceneTouchListener(this);
    }

    @Override
    public void onBackKeyPressed()
    {
        SceneManager.getInstance().loadMenuScene(engine);
    }

    @Override
    public SceneType getSceneType()
    {
        return SceneType.SCENE_GAME;
    }

    @Override
    public void disposeScene()
    {
        camera.setHUD(null);
        camera.setCenter(400, 240);
        camera.setChaseEntity(null);

        // TODO code responsible for disposing scene
        // removing all game scene objects.
    }
    
    private void createBackground()
    {
        setBackground(new Background(Color.BLUE));
    }

    private void createHUD()
    {
        gameHUD = new HUD();
        
        // CREATE SCORE TEXT
        scoreText = new Text(20, 420, resourcesManager.font, "Score: 0123456789", new TextOptions(HorizontalAlign.LEFT), vbom);
        scoreText.setAnchorCenter(0, 0);    
        scoreText.setText("Score: 0");
        gameHUD.attachChild(scoreText);
        
        camera.setHUD(gameHUD);
    }

    private void createPhysics()
    {
        physicsWorld = new FixedStepPhysicsWorld(60, new Vector2(0, -17), false); 
        physicsWorld.setContactListener(contactListener());
        registerUpdateHandler(physicsWorld);
        DebugRenderer debug = new DebugRenderer(physicsWorld, vbom);
        this.attachChild(debug);
    }
    
    private void addToScore(int i)
    {
        score += i;
        scoreText.setText("Score: " + score);
    }
    
    private void loadLevel(int levelID)
    {
        final SimpleLevelLoader levelLoader = new SimpleLevelLoader(vbom);
        
        final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(0, 0.01f, 0.5f);
        
        levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(LevelConstants.TAG_LEVEL)
        {
            public IEntity onLoadEntity(final String pEntityName, final IEntity pParent, final Attributes pAttributes, final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData) throws IOException 
            {
                final int width = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_WIDTH);
                final int height = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_HEIGHT);
                
                camera.setBounds(0, 0, width, height); // here we set camera bounds
                camera.setBoundsEnabled(true);

                return GameScene.this;
            }
        });
        
        levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(TAG_ENTITY)
        {
            public IEntity onLoadEntity(final String pEntityName, final IEntity pParent, final Attributes pAttributes, final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData) throws IOException
            {
                final int x = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_X);
                final int y = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_Y);
                final String type = SAXUtils.getAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_TYPE);
                
                final Sprite levelObject;
                
                if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM1))
                {
                    levelObject = new Sprite(x, y, resourcesManager.platform1_region, vbom);
                    PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF).setUserData("platform1");
                } 
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM2))
                {
                    levelObject = new Sprite(x, y, resourcesManager.platform2_region, vbom);
                    final Body body = PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF);
                    body.setUserData("platform2");
                    physicsWorld.registerPhysicsConnector(new PhysicsConnector(levelObject, body, true, false));
                }
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_SWITCH))
                {
                    levelObject = new Sprite(x, y, resourcesManager.switch_region, vbom);
                    final Body body = PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF);
                    body.setUserData("switch");
                    physicsWorld.registerPhysicsConnector(new PhysicsConnector(levelObject, body, true, false));
                }
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_FLAG))
                {
                    levelObject = new Sprite(x, y, resourcesManager.flag_region, vbom)
                    {
                        @Override
                        protected void onManagedUpdate(float pSecondsElapsed) 
                        {
                            super.onManagedUpdate(pSecondsElapsed);

                            if (player.collidesWith(this))
                            {
                                addToScore(10);
                                this.setVisible(false);
                                this.setIgnoreUpdate(true);
                            }
                        }
                    };
                    levelObject.registerEntityModifier(new LoopEntityModifier(new ScaleModifier(1, 1, 1.3f)));
                }     
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER))
                {
                	player = new Player(x, y, vbom, camera, physicsWorld)
                	{
                		@Override
                		public void onDie()
                		{
                		    if (!gameOverDisplayed)
                		    {
                		        displayGameOverText();
                		    }
                		}
                	};
                	levelObject = player;
                }
                else
                {
                    throw new IllegalArgumentException();
                }

                levelObject.setCullingEnabled(true);

                return levelObject;
            }
        });

        levelLoader.loadLevelFromAsset(activity.getAssets(), "level/" + levelID + ".lvl");
    }
    
    private ContactListener contactListener()
    {
        ContactListener contactListener = new ContactListener()
        {
            public void beginContact(Contact contact)
            {
                final Fixture x1 = contact.getFixtureA();
                final Fixture x2 = contact.getFixtureB();
                Fixture ft = null;

                if (x1.getUserData() != null && x1.getUserData().equals("feet"))
                {
                	ft = x1;
                }
                else if (x2.getUserData() != null && x2.getUserData().equals("feet"))
                {
                	ft = x2;
                }
            	if (ft != null)
            	{
                    player.increaseFootContacts();
                }
            }

            public void endContact(Contact contact)
            {
                final Fixture x1 = contact.getFixtureA();
                final Fixture x2 = contact.getFixtureB();
                Fixture ft = null;

                if (x1.getUserData() != null && x1.getUserData().equals("feet"))
                {
                	ft = x1;
                }
                else if (x2.getUserData() != null && x2.getUserData().equals("feet"))
                {
                	ft = x2;
                }
            	if (ft != null)
            	{
                    player.decreaseFootContacts();
                }
            }

            public void preSolve(Contact contact, Manifold oldManifold)
            {

            }

            public void postSolve(Contact contact, ContactImpulse impulse)
            {

            }
        };
        return contactListener;
    }
    
    private void createGameOverText()
    {
        gameOverText = new Text(0, 0, resourcesManager.font, "Game Over!", vbom);
    }

    private void displayGameOverText()
    {
        camera.setChaseEntity(null);
        gameOverText.setPosition(camera.getCenterX(), camera.getCenterY());
        attachChild(gameOverText);
        gameOverDisplayed = true;
    }
    
    private HUD gameHUD;
    private Text scoreText;
    private int score = 0;
    private PhysicsWorld physicsWorld;
    private Player player;
    private Vector2 lastTouchCoords;
    private static int DRAG_DISTANCE = 50;
    private static double MOVE_TOUCH_PERCENTAGE = .2;
    private boolean hasJumped = false;
    private Text gameOverText;
    private boolean gameOverDisplayed = false;
    
    private static final String TAG_ENTITY = "entity";
    private static final String TAG_ENTITY_ATTRIBUTE_X = "x";
    private static final String TAG_ENTITY_ATTRIBUTE_Y = "y";
    private static final String TAG_ENTITY_ATTRIBUTE_TYPE = "type";
        
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM1 = "platform1";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM2 = "platform2";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_SWITCH = "switch";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_FLAG = "flag";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER = "player";
    
	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if (pSceneTouchEvent.isActionDown())
		{
			float x = pSceneTouchEvent.getX() - camera.getXMin();
			float y = pSceneTouchEvent.getY() - camera.getYMin();
			int xdir = 0;
			lastTouchCoords = new Vector2(x, y);
			if (x < camera.getWidth()*MOVE_TOUCH_PERCENTAGE)
			{
				xdir = -1;
			}
			else if (x > camera.getWidth() - camera.getWidth()*MOVE_TOUCH_PERCENTAGE)
			{
				xdir = 1;
			}
			player.setRunDirection(xdir);
		}
		else if (pSceneTouchEvent.isActionMove())
		{
			float x = pSceneTouchEvent.getX() - camera.getXMin();
			float y = pSceneTouchEvent.getY() - camera.getYMin();
			Vector2 currentCoords = new Vector2(x, y);
			Vector2 displacement = currentCoords.sub(lastTouchCoords);
			if (Math.abs(displacement.y) > DRAG_DISTANCE && !hasJumped)
			{
				hasJumped = true;
				player.jump();
			}
		}
		else if (pSceneTouchEvent.isActionUp())
		{
			if (player.isRunning())
			{
				player.setRunDirection(0);
			}
			if (hasJumped)
			{
				hasJumped = false;
			}
		}
		return false;
	}
}