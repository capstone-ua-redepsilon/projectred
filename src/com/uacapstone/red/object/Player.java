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
    
    public Player(float pX, float pY, VertexBufferObjectManager vbo, Camera camera, PhysicsWorld physicsWorld)
    {
        super(pX, pY, ResourcesManager.getInstance().player_region, vbo);
        createPhysics(camera, physicsWorld);
        camera.setChaseEntity(this);
    }
    
    // ---------------------------------------------
    // VARIABLES
    // ---------------------------------------------
	     
    private Body body;
    private float velocity = 0;
    private int runDirection = 0;
    private float speed = 5;
    private int footContacts = 0;

    // ---------------------------------------------
    // LOGIC
    // ---------------------------------------------
    
    public abstract void onDie();
    
    /**
     * 
     * @param direction - number denoting left (negative) or right (positive)
     */
    public void setRunDirection(float direction)
    {
    	runDirection = (int)(direction / Math.abs(direction));
        velocity = runDirection * speed;
            
    	animateRun(runDirection);
    }
    
    private void animateRun(float direction)
    {
        final long[] PLAYER_ANIMATE = new long[] { 100, 100, 100 };
        if (direction != 0 && isOnGround())
        {
        	setScaleX(direction);
            animate(PLAYER_ANIMATE, 0, 2, true);
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
        if (isOnGround()) 
        {
        	body.setLinearVelocity(new Vector2(body.getLinearVelocity().x, 12)); 
    	
        	stopAnimation();
    	}
    }
    
    public void land()
    {
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

        body.setUserData("player");
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
                    onDie();
                }
            
                body.setLinearVelocity(new Vector2(velocity, body.getLinearVelocity().y)); 
            }
        });
    }
}