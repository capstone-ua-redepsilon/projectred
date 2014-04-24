package com.uacapstone.red.scene;

import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

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

import android.annotation.SuppressLint;

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
import com.uacapstone.red.networking.NetworkingConstants;
import com.uacapstone.red.networking.NetworkingConstants.MessageFlags;
import com.uacapstone.red.networking.PhysicsBodyState;
import com.uacapstone.red.networking.PlayerState;
import com.uacapstone.red.networking.WizardPlayerState;
import com.uacapstone.red.networking.messaging.FlaggedNetworkMessage;
import com.uacapstone.red.networking.messaging.NetworkMessage;
import com.uacapstone.red.networking.messaging.PlayerStateMessage;
import com.uacapstone.red.networking.messaging.WizardPlayerStateMessage;
import com.uacapstone.red.object.Avatar;
import com.uacapstone.red.object.AvatarData;
import com.uacapstone.red.object.CollisionData;
import com.uacapstone.red.object.FeetData;
import com.uacapstone.red.object.ICollisionTarget;
import com.uacapstone.red.object.Joe;
import com.uacapstone.red.object.Rabbit;
import com.uacapstone.red.object.Tornado;
import com.uacapstone.red.object.UserData;
import com.uacapstone.red.object.Wizard;
//github.com/capstone-ua-redepsilon/projectred.git

@SuppressLint("DefaultLocale")
public class GameScene extends BaseScene implements IOnSceneTouchListener
{
	final Date mLastGameStateMessageReceived = new Date();
	
	private boolean mIsGamePaused;
	
    @Override
    public void createScene()
    {
        createBackground();
        createHUD();
        createPhysics();
        loadLevel(2);
        createCongratsText();
        setOnSceneTouchListener(this);
    }

    @Override
    public void onBackKeyPressed()
    {
    	quit();
    }

    @Override
    public SceneType getSceneType()
    {
        return SceneType.SCENE_GAME;
    }

    @Override
    public void disposeScene()
    {
        
    	activity.leaveRoom();
    	resetCamera();

        // TODO code responsible for disposing scene
        // removing all game scene objects.
    }
    
    private void createBackground()
    {
        setBackground(new Background(Color.BLUE));
    }
    
    private void quit()
    {
    	disposeScene();
        SceneManager.getInstance().loadMenuScene(engine);
    }

    private void createHUD()
    {
        gameHUD = new HUD();
        
        // CREATE TEXT
		flagText = new Text(20, 420, resourceManager.font, "Flags: 0123456789", new TextOptions(HorizontalAlign.LEFT), vbom);
        timeText = new Text(20, 370, resourceManager.font, "Time: 00:00", new TextOptions(HorizontalAlign.LEFT), vbom);
        flagText.setAnchorCenter(0, 0);
        timeText.setAnchorCenter(0, 0);
        flagText.setText("Flags: 0");
        startTime = System.currentTimeMillis();
        updateTimeDisplay();
        gameHUD.attachChild(flagText);
        gameHUD.attachChild(timeText);
        
        camera.setHUD(gameHUD);
    }

    private void createPhysics()
    {
        physicsWorld = new FixedStepPhysicsWorld(60, new Vector2(0, -17), false); 
        physicsWorld.setContactListener(contactListener());
        registerUpdateHandler(physicsWorld);
        //DebugRenderer debug = new DebugRenderer(physicsWorld, vbom);
        //this.attachChild(debug);
        FIXTURE_DEF = PhysicsFactory.createFixtureDef(0, 0.01f, 0.5f);
        FIXTURE_DEF.filter.categoryBits = 0x0001;
        
        registerUpdateHandler(new IUpdateHandler() {
        	
        	final Date timeOfLastMessage = new Date();
	    	@Override
	    	public void onUpdate(float pSecondsElapsed) {
	    		// wait for the game to be resumed
	    		if (mIsGamePaused) return;
	    	    updateTimeDisplay();
	    	    
	    	    Date currentTime = new Date();
	    	    if (activity.isMultiplayer()) {
		    	    if (/*activity.isHost() &&*/ (currentTime.getTime() - timeOfLastMessage.getTime()) > 250 ) {
		    	    	timeOfLastMessage.setTime(currentTime.getTime());
		    	    	sendGameStateUpdate();
		    	    }
	    	    }
	    	}

			@Override
			public void reset() {
				// TODO Auto-generated method stub
				
			}
	    	
        });
    }
    
    private PlayerState createStateForPlayer(Avatar p) {
    	PlayerState pState = new PlayerState();
    	PhysicsBodyState bodyState = new PhysicsBodyState();
    	bodyState.x = p.getBody().getPosition().x;
    	bodyState.y = p.getBody().getPosition().y;
    	bodyState.velocityX = p.getBody().getLinearVelocity().x;
    	bodyState.velocityY = p.getBody().getLinearVelocity().y;
    	
    	pState.bodyState = bodyState;
    	pState.direction = p.getRunDirection();
    	pState.id = p.getId();
    	pState.playerFeetDown = p.getNumberOfFeetDown();
    	pState.hasJumped = p.getHasJumped();
    	
    	return pState;
    }
    
    protected void sendGameStateUpdate() {
    	// State updates for local player
    	
    	PlayerState genericState = createStateForPlayer(this.avatar);
    	
    	NetworkMessage msgToSend = null;
    	
    	// State updates for other entities (abilities)
    	if (this.avatar instanceof Wizard )
    	{
    		Wizard w = (Wizard)this.avatar;
    		WizardPlayerStateMessage stateMessage = new WizardPlayerStateMessage();
    		
    		WizardPlayerState wizardState = new WizardPlayerState();
    		wizardState.playerState = genericState;
    		if (w.mDidJustCastTornado) {
    			wizardState.didCastTornado = w.mDidJustCastTornado;
    			w.mDidJustCastTornado = false;
    		}
    		
    		stateMessage.state = wizardState;
    		
    		msgToSend = stateMessage;
    	}
    	else 
    	{
    		PlayerStateMessage stateMessage = new PlayerStateMessage();
    		stateMessage.state = genericState;
    		
    		msgToSend = stateMessage;
    	}
    	
    	msgToSend.sequenceNumber = messageSequenceNumber++;
    	activity.sendMessage(new FlaggedNetworkMessage(msgToSend));
	}

	private void captureFlag()
    {
		setFlags(flagsRemaining-1);
    }
	
	private void resetFlags()
	{
        setFlags(numFlags);
	}
	
	private void setFlags(int n)
	{
		flagsRemaining = n;
        flagText.setText("Flags: " + flagsRemaining);
        if (flagsRemaining == 0)
        {
            activity.updateLeaderboard(System.currentTimeMillis() - startTime);
        	displayCongratsText();
        	Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    quit();
                }
            }, CelebrationTimeInMilliseconds);
        }
	}
    
    private void updateTimeDisplay()
    {
    	long currentTime = System.currentTimeMillis();
    	long runTimeMill = currentTime - startTime;
    	long second = (runTimeMill / 1000) % 60;
    	long minute = (runTimeMill / (1000 * 60)) % 60;
//    	long hour = (runTimeMill / (1000 * 60 * 60)) % 24;

    	String time = String.format("%02d:%02d", minute, second);
    	timeText.setText("Time: " + time);
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
                minPlayers = SAXUtils.getIntAttributeOrThrow(pAttributes, "numPlayers");
                numPlayers = activity.getNumPlayers();
                avatars = new Avatar[numPlayers];
                mId = activity.getNormalizedId();
                
                camera.setBounds(0, 0, width, height); // here we set camera bounds
                camera.setBoundsEnabled(true);

                return GameScene.this;
            }
        });
        
        levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(TAG_ENTITY)
        {
        	int playerIndex = 0;
            public IEntity onLoadEntity(final String pEntityName, final IEntity pParent, final Attributes pAttributes, final SimpleLevelEntityLoaderData pSimpleLevelEntityLoaderData) throws IOException
            {
                final int x = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_X);
                final int y = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_Y);
                final String type = SAXUtils.getAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_TYPE);
                
                Sprite levelObject = null;
                
                if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM1))
                {
                    levelObject = new Sprite(x, y, resourceManager.platform1_region, vbom);
                    PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF).setUserData("platform1");
                } 
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_BLOCK))
                {
                    levelObject = new Sprite(x, y, resourceManager.block_region, vbom);
                    PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF).setUserData("block");
                } 
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM2))
                {
                    levelObject = new Sprite(x, y, resourceManager.platform2_region, vbom);
                    final Body body = PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF);
                    body.setUserData("platform2");
                    physicsWorld.registerPhysicsConnector(new PhysicsConnector(levelObject, body, true, false));
                }
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_SWITCH))
                {
                    levelObject = new Sprite(x, y, resourceManager.switch_region, vbom);
                    hiddenPlatformSprite = new Sprite(x+100, y+100, resourceManager.platform3_region, vbom);
                    final Body body = PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyType.StaticBody, FIXTURE_DEF);
                    body.setUserData("switch");
                    physicsWorld.registerPhysicsConnector(new PhysicsConnector(levelObject, body, true, false));
                }
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_FLAG))
                {
                	numFlags++;
                    levelObject = new Sprite(x, y, resourceManager.flag_region, vbom)
                    {
                        @Override
                        protected void onManagedUpdate(float pSecondsElapsed) 
                        {
                            super.onManagedUpdate(pSecondsElapsed);

                            for (Avatar p : avatars)
                            {
                                if (p.collidesWith(this))
                                {
                                    captureFlag();
                                    this.setVisible(false);
                                    this.setIgnoreUpdate(true);
                                }
                            }
                        }
                    };
                    levelObject.registerEntityModifier(new LoopEntityModifier(new ScaleModifier(1, 1, 1.3f)));
                }     
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_JOE))
                {
                	if (playerIndex < numPlayers)
                	{
                    	Avatar p = new Joe(x, y, vbom, camera, physicsWorld, playerIndex)
                    	{
                    		@Override
                    		public void onDie()
                    		{
                    		    //getBody().setTransform(getStartPosition(), getBody().getAngle());
                    		}
                    	};
                    	levelObject = p;
                    	avatars[playerIndex++] = p;	
                	}
                }
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_WIZARD))
                {
                	if (playerIndex < numPlayers)
                	{
                    	Avatar p = new Wizard(x, y, vbom, camera, physicsWorld, playerIndex)
                    	{
                    		@Override
                    		public void onDie()
                    		{
                    		    //getBody().setTransform(getStartPosition(), getBody().getAngle());
                    		}
                    	};
                    	levelObject = p;
                    	avatars[playerIndex++] = p;	
                	}
                }
                else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_RABBIT))
                {
                	if (playerIndex < numPlayers)
                	{
                    	Rabbit p = new Rabbit(x, y, vbom, camera, physicsWorld, playerIndex)
                    	{
                    		@Override
                    		public void onDie() {
                    			
                    		}

							@Override
							public void collideOther(ICollisionTarget target) {
								super.collideOther(target);
								if (target instanceof Tornado) {
									mPhysicsWorld.postRunnable(new Runnable() {
										@Override
										public void run() {
											getBody().applyLinearImpulse(TornadoForce, getBody().getPosition());
										}
									});
								}
							}
                    		
                    		
                    	};
                    	levelObject = p;
                    	avatars[playerIndex++] = p;	
                    	
                    	p.getBody().setUserData(new AvatarData(new CollisionData(p, p.getBody(), "rabbit")));
                	}
                }
                else
                {
                    throw new IllegalArgumentException();
                }

                if (levelObject != null)
                {
	                levelObject.setCullingEnabled(true);
	
                }
                return levelObject;
            }
        });

        levelLoader.loadLevelFromAsset(activity.getAssets(), "level/" + levelID + ".lvl");

        resetFlags();
        avatar = avatars[mId];
        avatar.setupHud();
        camera.setChaseEntity(avatar);
    }
    
    private ContactListener contactListener()
    {
        ContactListener contactListener = new ContactListener()
        {
        	private CollisionData getCollisionDataFromUserData(UserData u) {
        		if (u.getClass() == AvatarData.class) {
        			return ((AvatarData)u).mCollisionData;
        		} else if (u.getClass() == CollisionData.class) {
        			return (CollisionData)u;
        		}
        		
        		return null;
        	}
        	
        	private Fixture testFeet(Fixture f) {
        		if (f.getUserData() != null && f.getUserData() instanceof FeetData) {
                	return f;
                }
        		return null;
        	}
        	
            public void beginContact(Contact contact)
            {
                final Fixture x1 = contact.getFixtureA();
                final Fixture x2 = contact.getFixtureB();
                
                Fixture feet = null;
                Fixture other = null;
                
                feet = testFeet(x1);
                other = x2;
                if (feet == null) {
                	feet = testFeet(x2);
                	other = x1;
                }
                
                if (feet != null) {
                	if ((feet.getFilterData().maskBits & other.getFilterData().categoryBits) == other.getFilterData().categoryBits)
            		{
            			((FeetData)feet.getUserData()).mAvatar.increaseFootContacts();
            		}
                }
                
//                Debug.d("Attempting Contact");
                
                // if not type UserData, don't even try to do anything
                if (! (x1.getBody().getUserData() != null && x1.getBody().getUserData() instanceof UserData )) return;
                if (! (x2.getBody().getUserData() != null && x2.getBody().getUserData() instanceof UserData )) return;
                
                UserData userData1 = (UserData)x1.getBody().getUserData();
                UserData userData2 = (UserData)x2.getBody().getUserData();
                
//                Debug.d("Both are userdata");
                
                CollisionData c1 = getCollisionDataFromUserData(userData1);
                CollisionData c2 = getCollisionDataFromUserData(userData2);
                
                // some type of collision data must be found to continue
                if (c1 == null || c2 == null) return;
                
//                Debug.d("Both userdatas have collisiondata, running collision");
                
                c1.mCollisionTarget.collideOther(c2.mCollisionTarget);
                c2.mCollisionTarget.collideOther(c1.mCollisionTarget);
            }

            public void endContact(Contact contact)
            {
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
    
    private void createCongratsText()
    {
    	congratsText = new Text(activity.getScreenHalfWidth(), activity.getScreenHalfHeight(), resourceManager.font, "Congratulations!", vbom);
    }

    private void displayCongratsText()
    {
    	gameHUD.attachChild(congratsText);
        congratsDisplayed = true;
    }
    
    private HUD gameHUD;
    private Text flagText;
    private Text timeText;
    private long startTime;
    private PhysicsWorld physicsWorld;
    private Avatar avatar;
    private int mId;
    private int numFlags = 0;
    private int flagsRemaining;
    private static final int MaxPlayers = 4;
    private Avatar[] avatars;
	private int numPlayers;
	private int minPlayers = 0;
    private Vector2 lastTouchCoords;
    private static int DRAG_DISTANCE = 50;
    private static double HORIZ_MOVE_TOUCH_PERCENTAGE = .2;
    private static double VERT_MOVE_TOUCH_PERCENTAGE = .5;
    private boolean hasJumped = false;
    private Text congratsText;
    private boolean congratsDisplayed = false;
	private Body hiddenPlatformBody = null;
	private Sprite hiddenPlatformSprite;
	private int mNumPlayersOnSwitch = 0;
	
	private static final long CelebrationTimeInMilliseconds = 5000;
	private static final Vector2 TornadoForce = new Vector2(0.0f, 15.0f);
    
    private static final String TAG_ENTITY = "entity";
    private static final String TAG_ENTITY_ATTRIBUTE_X = "x";
    private static final String TAG_ENTITY_ATTRIBUTE_Y = "y";
    private static final String TAG_ENTITY_ATTRIBUTE_TYPE = "type";
        
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM1 = "platform1";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_BLOCK = "block";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM2 = "platform2";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_SWITCH = "switch";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_FLAG = "flag";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_JOE = "joe";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_WIZARD = "wizard";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_RABBIT = "rabbit";
    
    private static FixtureDef FIXTURE_DEF;
    
    private int messageSequenceNumber = 0;

    public PhysicsWorld getPhysicsWorld() {
    	return this.physicsWorld;
    }
    
	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		// Don't accept inputs while the game is paused
		if (this.mIsGamePaused) return false;
		
		if (pSceneTouchEvent.isActionDown())
		{
			float x = pSceneTouchEvent.getX() - camera.getXMin();
			float y = pSceneTouchEvent.getY() - camera.getYMin();
			int xdir = 0;
			lastTouchCoords = new Vector2(x, y);
			if (y < camera.getHeight()*VERT_MOVE_TOUCH_PERCENTAGE)
			{
				if (x < camera.getWidth()*HORIZ_MOVE_TOUCH_PERCENTAGE)
				{
					xdir = -1;
				}
				else if (x > camera.getWidth() - camera.getWidth()*HORIZ_MOVE_TOUCH_PERCENTAGE)
				{
					xdir = 1;
				}
			}
			avatar.setRunDirection(xdir);
			
			sendGameStateUpdate();
		}
		else if (pSceneTouchEvent.isActionMove())
		{
			float x = pSceneTouchEvent.getX() - camera.getXMin();
			float y = pSceneTouchEvent.getY() - camera.getYMin();
			Vector2 currentCoords = new Vector2(x, y);
			Vector2 displacement = currentCoords.sub(lastTouchCoords);
			
			if (Math.abs(displacement.y) > DRAG_DISTANCE) {
				avatar.jump(true);
				sendGameStateUpdate();
			}
		}
		else if (pSceneTouchEvent.isActionUp())
		{
			if (avatar.isRunning())
			{
				avatar.setRunDirection(0);
				sendGameStateUpdate();
			}
		}
		return false;
	}
	
	
	void handlePlayerStateMessage(PlayerStateMessage message) {
		
		for (Avatar p: this.avatars) {
			if (p.getId() == message.state.id) {
				message.state.apply(p);
			}
		}
	}
	
	void handleWizardPlayerStateMessage(WizardPlayerStateMessage message) {
		
		// handle wizard thingies
		for (Avatar p: this.avatars) {
			if (p.getId() == message.state.playerState.id) {
				message.state.apply((Wizard)p);
			}
		}
		
	}
	
	public void createTornado(int playerId, float x, float y, int speed, long duration, final Runnable onComplete) {
		
		final Tornado tornado = new Tornado(x, y, this, playerId, speed) {

			@Override
			public void collideOther(ICollisionTarget target) {
				if (target instanceof Rabbit) onComplete();
			}
			
			
			@Override 
			public void onComplete() {
				super.onComplete();
				onComplete.run();
			}
		};
				
		
	    Timer timer = new Timer();
	    timer.schedule(new TimerTask()
	    {
	    	@Override
	    	public void run()
	    	{
	    		if (!tornado.didComplete())
	    			tornado.onComplete();
	    	}
	    }, duration);
		
	    tornado.getBody().setUserData(new CollisionData(tornado, tornado.getBody(), "tornado"));
        this.attachChild(tornado);    	
	}
	
	
	public void handleMessage(FlaggedNetworkMessage message) throws IOException
	{
		switch (message.messageFlag) {
		case MessageFlags.MESSAGE_FROM_SERVER_PLAYER_STATE:
			if (new Date(message.timestamp).after(mLastGameStateMessageReceived)) {
				mLastGameStateMessageReceived.setTime(message.timestamp);
				handleMessage(NetworkingConstants.messagePackInstance.read(message.messageBytes, PlayerStateMessage.class));
			}
			break;
		case MessageFlags.MESSAGE_WIZARD_PLAYER_STATE:
			if (new Date(message.timestamp).after(mLastGameStateMessageReceived)) {
				mLastGameStateMessageReceived.setTime(message.timestamp);
				handleMessage(NetworkingConstants.messagePackInstance.read(message.messageBytes, WizardPlayerStateMessage.class));
			}
			break;
		}
		return;
	}
	
	public void handleMessage(NetworkMessage message) {
		switch (message.getFlag()) {
		case MessageFlags.MESSAGE_FROM_SERVER_PLAYER_STATE:
			handlePlayerStateMessage((PlayerStateMessage)message);
			break;
		case MessageFlags.MESSAGE_WIZARD_PLAYER_STATE:
			handleWizardPlayerStateMessage((WizardPlayerStateMessage)message);
			break;
		}
	}
	

	public void resumeGame() {
		if (this.mIsGamePaused) {
			this.engine.start();
			this.mIsGamePaused = false;
		}
	}
	
	public void pauseGame() {
		if (!this.mIsGamePaused) {
			this.engine.stop();
			this.mIsGamePaused = true;
		}
	}
}
