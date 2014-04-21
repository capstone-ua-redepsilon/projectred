package com.uacapstone.red.object;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.uacapstone.red.manager.ResourcesManager;

/**
 * @author Mateusz Mysliwiec
 * @author www.matim-dev.com
 * @version 1.0
 */
public abstract class Player extends AnimatedSprite
{
    // ---------------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------------
    
    public Player(float pX, float pY, VertexBufferObjectManager vbo, Camera camera, PhysicsWorld physicsWorld, int id)
    {
        super(pX, pY, ResourcesManager.getInstance().player_region, vbo);
    	mId = id;
        createPhysics(camera, physicsWorld);
    }
    
    // ---------------------------------------------
    // VARIABLES
    // ---------------------------------------------
	     
    private int mId;
    private Body body;
    private Fixture feet;
    private float velocity = 0;
    private int runDirection = 0;
    private float speed = 5;
    private int footContacts = 0;
    private Vector2 startPosition;
	private boolean mHasJumped;

    // ---------------------------------------------
    // LOGIC
    // ---------------------------------------------
    
    public abstract void onDie();
    
    /**
     * 
     * @param direction - number denoting left (negative) or right (positive)
     */
    
    public int getId() {
    	return mId;
    }
    
    public int getRunDirection() {
    	return runDirection;
    }
    
    public int getNumberOfFeetDown() {
    	return footContacts;
    }
    
    public void setNumberOfFeetDown(int num) {
    	footContacts = num;
    }
    
    public void setRunDirection(float direction)
    {
    	runDirection = (int)(direction / Math.abs(direction));
        velocity = runDirection * speed;
            
    	animateRun(runDirection);
    }
    public Body getBody()
    {
    	return body;
    }
    
    public Vector2 getStartPosition()
    {
    	return startPosition;
    }
    
    private void animateRun(float direction)
    {
        final long[] PLAYER_ANIMATE = new long[] { 100, 100, 100 };
        if (direction != 0)
        {
        	setScaleX(direction);
        	if (isOnGround())
        	{
        		animate(PLAYER_ANIMATE, 0, 2, true);
        	}
        }
        else
        {
        	stopAnimation();
        }
    }
    
    public boolean isRunning()
    {
    	return runDirection != 0;
    }
    
    public void jump()
    {
        if (!mHasJumped && isOnGround()) 
        {
        	body.setLinearVelocity(new Vector2(body.getLinearVelocity().x, 12)); 
    	
        	stopAnimation();
    	}
    }
    
    public void land()
    {
    	mHasJumped = false;
    	
    	if (runDirection == 0)
    	{
    		velocity = 0;
    	}
    	else
    	{
    		setRunDirection(runDirection);
    	}
    }
    
    public boolean isOnGround()
    {
    	return footContacts > 0;
    }
    
    public void increaseFootContacts()
    {
        footContacts++;
        if (footContacts == 1)
        {
        	land();
        }
    }

    public void decreaseFootContacts()
    {
        footContacts--;
        if (footContacts == 0)
        {
        	stopAnimation();
        }
    }
    
    private void createPhysics(final Camera camera, PhysicsWorld physicsWorld)
    {        
        body = PhysicsFactory.createBoxBody(physicsWorld, this, BodyType.DynamicBody, PhysicsFactory.createFixtureDef(0, 0, 0));
        
        final PolygonShape mPoly = new PolygonShape();
        mPoly.setAsBox(8.0f/PhysicsConnector.PIXEL_TO_METER_RATIO_DEFAULT,
        		0.5f/PhysicsConnector.PIXEL_TO_METER_RATIO_DEFAULT,
        		new Vector2(0,-16/PhysicsConnector.PIXEL_TO_METER_RATIO_DEFAULT),
        		0); //The size of the character is 32x32
        final FixtureDef pFixtureDef = PhysicsFactory.createFixtureDef(0f,0f,0f,true);
        pFixtureDef.shape = mPoly;
        feet=body.createFixture(pFixtureDef);
        feet.setUserData(new PlayerData(mId, "feet"));
        mPoly.dispose();
        
        body.setUserData(new PlayerData(mId, "player"));
        body.setFixedRotation(true);
        
        physicsWorld.registerPhysicsConnector(new PhysicsConnector(this, body, true, false)
        {
            @Override
            public void onUpdate(float pSecondsElapsed)
            {
                super.onUpdate(pSecondsElapsed);
                camera.onUpdate(0.1f);
                
                if (getY() <= 0)
                {                    
                    body.setTransform(startPosition, body.getAngle());
                }
            
                body.setLinearVelocity(new Vector2(velocity, body.getLinearVelocity().y)); 
            }
        });
        startPosition = new Vector2(body.getPosition());
    }

	public void setHasjumped(boolean hasJumped) {
		mHasJumped = hasJumped;
	}

	public boolean getHasJumped() {
		return mHasJumped;
	}
}