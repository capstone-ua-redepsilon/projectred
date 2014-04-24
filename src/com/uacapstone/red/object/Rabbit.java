package com.uacapstone.red.object;

import org.andengine.engine.camera.Camera;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.uacapstone.red.manager.ResourceManager;

/**
 * @author Mateusz Mysliwiec
 * @author www.matim-dev.com
 * @version 1.0
 */
public abstract class Rabbit extends Avatar
{
    // ---------------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------------
    
    public Rabbit(float pX, float pY, VertexBufferObjectManager vbo, Camera camera, PhysicsWorld physicsWorld, int id)
    {
        super(pX, pY, vbo, camera, physicsWorld, ResourceManager.getInstance().rabbit_region, id);
        speed = 8;
    }
    
    @Override
    protected void setupPhysics() {
    	super.setupPhysics();
    	mFixtureDef.filter.groupIndex = 1;
    	mFixtureDef.filter.categoryBits |= 0x0003;
    	mFixtureDef.filter.maskBits |= 0x0004;
    }
    
    // ---------------------------------------------
    // VARIABLES
    // ---------------------------------------------
	

    // ---------------------------------------------
    // LOGIC
    // ---------------------------------------------
    
    @Override
    protected void animateRun(float direction)
    {
        final long[] ANIMATE = new long[] { 100, 100, 100, 100, 100, 100 };
        if (direction != 0)
        {
        	setScaleX(direction);
        	if (isOnGround())
        	{
        		animate(ANIMATE, 8, 13, true);
        	}
        }
        else
        {
        	if (isOnGround())
        	{
            	stopAnimation();
            	this.setCurrentTileIndex(0);
        	}
        }
    } 
    @Override
    protected void animateJump(float direction)
    {
        final long[] ANIMATE = new long[] { 100, 100, 100, 100, 100, 100, 100 };
    	animate(ANIMATE, 16, 22, false);
    }
    @Override
    protected void animateLand(float direction)
    {
        final long[] ANIMATE = new long[] { 100, 100 };
    	animate(ANIMATE, new int[] {23, 0}, false);
    }
    @Override
    protected void animateFall(float direction)
    {
        stopAnimation();
        setCurrentTileIndex(1);
    }
    
    @Override
    public void setupHud()
    {
    }
}