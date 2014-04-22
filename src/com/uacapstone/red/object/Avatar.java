package com.uacapstone.red.object;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.uacapstone.red.manager.ResourceManager;

/**
 * @author Mateusz Mysliwiec
 * @author www.matim-dev.com
 * @version 1.0
 */
public abstract class Avatar extends AnimatedSprite
{
    // ---------------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------------
    
    public Avatar(float pX, float pY, VertexBufferObjectManager vbom, Camera camera, PhysicsWorld physicsWorld, ITiledTextureRegion region, int id)
    {
        super(pX, pY, region, vbom);
        mVbom = vbom;
        mCamera = camera;
    	mId = id;
    	mPhysicsWorld = physicsWorld;
        createPhysics();
    }
    
    // ---------------------------------------------
    // VARIABLES
    // ---------------------------------------------
	     
    protected int mId;
    protected FixtureDef mFixtureDef;
    protected VertexBufferObjectManager mVbom;
    protected PhysicsWorld mPhysicsWorld;
    protected Camera mCamera;
    protected Body body;
    protected Fixture feet;
    protected float velocity = 0;
    protected int runDirection = 0;
    protected float speed = 5;
    protected int footContacts = 0;
    protected Vector2 startPosition;
    protected boolean mHasJumped;

    // ---------------------------------------------
    // LOGIC
    // ---------------------------------------------

    protected abstract void animateRun(float direction);
    protected abstract void animateJump(float direction);
    protected abstract void animateLand(float direction);
    protected abstract void animateFall(float direction);
    protected abstract void setupPhysics();
    public abstract void setupHud();
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
    
    public boolean isRunning()
    {
    	return runDirection != 0;
    }
    
    public void jump()
    {
        if (!mHasJumped && isOnGround()) 
        {
        	mHasJumped = true;
        	body.setLinearVelocity(new Vector2(body.getLinearVelocity().x, 12)); 
    	
        	animateJump(runDirection);
    	}
    }
    
    public void land()
    {
    	mHasJumped = false;
    	
    	if (runDirection == 0)
    	{
    		velocity = 0;
        	animateLand(runDirection);
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
        if (footContacts == 0 && !mHasJumped)
        {
        	animateFall(runDirection);
        }
    }
    
    private void createPhysics()
    {        
    	mFixtureDef = PhysicsFactory.createFixtureDef(0, 0, 0);
    	mFixtureDef.filter.categoryBits = 0x0002;
    	mFixtureDef.filter.maskBits = ~0x0002;
        body = PhysicsFactory.createBoxBody(mPhysicsWorld, this, BodyType.DynamicBody, mFixtureDef);
        
        final PolygonShape mPoly = new PolygonShape();
        mPoly.setAsBox((mWidth/2-1)/PhysicsConnector.PIXEL_TO_METER_RATIO_DEFAULT,
        		0.5f/PhysicsConnector.PIXEL_TO_METER_RATIO_DEFAULT,
        		new Vector2(0,-(mHeight/2)/PhysicsConnector.PIXEL_TO_METER_RATIO_DEFAULT),
        		0); //The size of the character is 32x32
        final FixtureDef pFixtureDef = PhysicsFactory.createFixtureDef(0f,0f,0f,true);
        pFixtureDef.shape = mPoly;
        feet=body.createFixture(pFixtureDef);
        feet.setUserData(new AvatarData(mId, "feet"));
        mPoly.dispose();
        
        body.setUserData(new AvatarData(mId, "player"));
        body.setFixedRotation(true);
        
        mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(this, body, true, false)
        {
            @Override
            public void onUpdate(float pSecondsElapsed)
            {
                super.onUpdate(pSecondsElapsed);
                mCamera.onUpdate(0.1f);
                
                if (getY() <= 0)
                {                    
                    body.setTransform(startPosition, body.getAngle());
                }
            
                body.setLinearVelocity(new Vector2(velocity, body.getLinearVelocity().y)); 
            }
        });
        startPosition = new Vector2(body.getPosition());
        setupPhysics();
    }

	public void setHasjumped(boolean hasJumped) {
		mHasJumped = hasJumped;
	}

	public boolean getHasJumped() {
		return mHasJumped;
	}
}