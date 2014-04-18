package com.uacapstone.red.scene;

import java.io.IOException;
import java.util.ArrayList;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.IUpdateHandler;
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
import com.uacapstone.red.networking.FlaggedNetworkMessage;
import com.uacapstone.red.networking.NetworkMessage;
import com.uacapstone.red.networking.NetworkingConstants;
import com.uacapstone.red.networking.NetworkingConstants.MessageFlags;
import com.uacapstone.red.networking.PlayerChangeDirectionMessage;
import com.uacapstone.red.object.Player;
import com.uacapstone.red.object.PlayerData;

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
        FIXTURE_DEF = PhysicsFactory.createFixtureDef(0, 0.01f, 0.5f);
        
        registerUpdateHandler(new IUpdateHandler() {
	    	@Override
	    	public void onUpdate(float pSecondsElapsed) {
	    	    for (Sprite s : spritesToAdd)
	    	    {
	    	    	final Sprite levelObject = s;
	                hiddenPlatformBody = PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF);
	                hiddenPlatformBody.setUserData("platform3");
	                s.setCullingEnabled(true);
	                attachChild(s);
	    	    }
	    	    for (int i=0; i<bodiesToRemove.size(); i++)
	    	    {
	                physicsWorld.destroyBody(bodiesToRemove.get(i));
	                hiddenPlatformBody = null;
	                detachChild(hiddenPlatformSprite);
	    	    }
	    	    spritesToAdd.clear();
	    	    bodiesToRemove.clear();
	    	}

			@Override
			public void reset() {
				// TODO Auto-generated method stub
				
			}
	    	
        });
    }
    
    private void addToScore(int i)
    {
        score += i;
        scoreText.setText("Score: " + score);
    }
    
    private void loadLevel(int levelID)
    {
        final SimpleLevelLoader levelLoader = new SimpleLevelLoader(vbom);
        
        levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(LevelConstants.TAG_LEVEL)
        {
            public IEntity onLoadEntity(final String pEntityName, final IEntity pParent, final Attributes pAttributes, final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData) throws IOException 
            {
                final int width = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_WIDTH);
                final int height = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_HEIGHT);
                final int nPlayers = SAXUtils.getIntAttributeOrThrow(pAttributes, "numPlayers");
                players = new Player[nPlayers];
                mId = activity.getNormalizedId();
                
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
                    hiddenPlatformSprite = new Sprite(x+100, y+100, resourcesManager.platform3_region, vbom);
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

                            for (Player p : players)
                            {
                                if (p.collidesWith(this))
                                {
                                    addToScore(10);
                                    this.setVisible(false);
                                    this.setIgnoreUpdate(true);
                                }
                            }
                        }
                    };
                    levelObject.registerEntityModifier(new LoopEntityModifier(new ScaleModifier(1, 1, 1.3f)));
                }     
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER))
                {
                	Player p = new Player(x, y, vbom, camera, physicsWorld, numPlayers)
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
                	levelObject = p;
                	players[numPlayers++] = p;
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
        player = players[mId];
        camera.setChaseEntity(player);
    }
    
    private ContactListener contactListener()
    {
        ContactListener contactListener = new ContactListener()
        {
            public void beginContact(Contact contact)
            {
                final Fixture x1 = contact.getFixtureA();
                final Fixture x2 = contact.getFixtureB();
                PlayerData pd1, pd2, pd = null;
                Fixture ft = null;
                Fixture o = null;

                if (x1.getUserData() != null && x1.getUserData() instanceof PlayerData)
                {
                	pd1 = (PlayerData)x1.getUserData();
                	if (pd1.mDescription == "feet")
                	{
                		pd = pd1;
                    	ft = x1;
                    	o = x2;
                	}
                }
                else if (x2.getUserData() != null && x2.getUserData() instanceof PlayerData)
                {
                	pd2 = (PlayerData)x2.getUserData();
                	if (pd2.mDescription == "feet")
                	{
                		pd = pd2;
                    	ft = x2;
                    	o = x1;
                	}
                }
            	if (ft != null && pd != null)
            	{
                    players[Integer.parseInt(pd.mId)].increaseFootContacts();
                    if (o.getBody().getUserData() != null && o.getBody().getUserData().equals("switch"))
                    {
                    	mNumPlayersOnSwitch++;
                    	if (mNumPlayersOnSwitch == 1)
                    	{
                    		spritesToAdd.add(hiddenPlatformSprite);
                    	}
                    }
                }
            }

            public void endContact(Contact contact)
            {
            	final Fixture x1 = contact.getFixtureA();
                final Fixture x2 = contact.getFixtureB();
                PlayerData pd1, pd2, pd = null;
                Fixture ft = null;
                Fixture o = null;

                if (x1.getUserData() != null && x1.getUserData() instanceof PlayerData)
                {
                	pd1 = (PlayerData)x1.getUserData();
                	if (pd1.mDescription == "feet")
                	{
                		pd = pd1;
                    	ft = x1;
                    	o = x2;
                	}
                }
                else if (x2.getUserData() != null && x2.getUserData() instanceof PlayerData)
                {
                	pd2 = (PlayerData)x2.getUserData();
                	if (pd2.mDescription == "feet")
                	{
                		pd = pd2;
                    	ft = x2;
                    	o = x1;
                	}
                }
            	if (ft != null && pd != null)
            	{
                    players[Integer.parseInt(pd.mId)].decreaseFootContacts();
                    if (o.getBody().getUserData() != null && o.getBody().getUserData().equals("switch"))
                    {
                    	mNumPlayersOnSwitch--;
                    	if (mNumPlayersOnSwitch == 0)
                    	{
                    		bodiesToRemove.add(hiddenPlatformBody);
                    	}
                    }
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
    private Player[] players;
    private Player player;
    private int mId;
	private int numPlayers = 0;
    private Vector2 lastTouchCoords;
    private static int DRAG_DISTANCE = 50;
    private static double MOVE_TOUCH_PERCENTAGE = .2;
    private boolean hasJumped = false;
    private Text gameOverText;
    private boolean gameOverDisplayed = false;
	private Body hiddenPlatformBody = null;
	private Sprite hiddenPlatformSprite;
	private ArrayList<Sprite> spritesToAdd = new ArrayList<Sprite>();
	private ArrayList<Body> bodiesToRemove = new ArrayList<Body>();
	private int mNumPlayersOnSwitch = 0;
    
    private static final String TAG_ENTITY = "entity";
    private static final String TAG_ENTITY_ATTRIBUTE_X = "x";
    private static final String TAG_ENTITY_ATTRIBUTE_Y = "y";
    private static final String TAG_ENTITY_ATTRIBUTE_TYPE = "type";
        
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM1 = "platform1";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM2 = "platform2";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_SWITCH = "switch";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_FLAG = "flag";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER = "player";
    
    private static FixtureDef FIXTURE_DEF;
    
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
			
			PlayerChangeDirectionMessage message = new PlayerChangeDirectionMessage(mId, xdir);
			FlaggedNetworkMessage flaggedMessage = new FlaggedNetworkMessage(message.getFlag(), message.getBytes());
			
			try {
				activity.sendMessage(NetworkingConstants.messagePackInstance.write(flaggedMessage));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			
//			byte[] message = new byte[8];
//			message[0] = (byte)mId;
//			message[1] = (byte)0;
//			message[2] = (byte)xdir;
//			activity.sendMessage(message);
		}
		else if (pSceneTouchEvent.isActionMove())
		{
//			float x = pSceneTouchEvent.getX() - camera.getXMin();
//			float y = pSceneTouchEvent.getY() - camera.getYMin();
//			Vector2 currentCoords = new Vector2(x, y);
//			Vector2 displacement = currentCoords.sub(lastTouchCoords);
//			if (Math.abs(displacement.y) > DRAG_DISTANCE && !hasJumped)
//			{
//				hasJumped = true;
//				player.jump();				
//				byte[] message = new byte[8];
//				message[0] = (byte)mId;
//				message[1] = (byte)1;
//				activity.sendMessage(message);
//			}
		}
		else if (pSceneTouchEvent.isActionUp())
		{
//			if (player.isRunning())
//			{
//				player.setRunDirection(0);
//				byte[] message = new byte[8];
//				message[0] = (byte)mId;
//				message[1] = (byte)0;
//				message[2] = (byte)0;
//				activity.sendMessage(message);
//			}
//			if (hasJumped)
//			{
//				hasJumped = false;
//			}
		}
		return false;
	}
	
	public void handlePlayerChangeDirectionMessage(PlayerChangeDirectionMessage msg) {
		players[msg.playerId].setRunDirection(msg.direction);
	}
	
	public void handleMessage(byte[] message)
	{
		try {
			FlaggedNetworkMessage flaggedMessage = NetworkingConstants.messagePackInstance.read(message, FlaggedNetworkMessage.class);
			
			switch (flaggedMessage.messageFlag) {
			case NetworkingConstants.MessageFlags.MESSAGE_FROM_CLIENT_PLAYER_DIRECTION:
				handlePlayerChangeDirectionMessage(NetworkingConstants.messagePackInstance.read(flaggedMessage.messageBytes, PlayerChangeDirectionMessage.class));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
//		int i = message[0];
//		int t = message[1];
//		if (t == 0)
//		{
//			float d = (float)message[2];
//			players[i].setRunDirection(d);
//		}
//		else
//		{
//			players[i].jump();
//		}
		return;
	}
}