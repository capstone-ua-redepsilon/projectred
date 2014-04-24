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
public abstract class Joe extends Avatar
{
    // ---------------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------------
    
    public Joe(float pX, float pY, VertexBufferObjectManager vbo, Camera camera, PhysicsWorld physicsWorld, int id)
    {
        super(pX, pY, vbo, camera, physicsWorld, ResourceManager.getInstance().player_region, id);
    }
    
    @Override
    protected void setupPhysics() {
    	super.setupPhysics();
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
        	this.setCurrentTileIndex(0);
        }
    }
    @Override
    protected void animateJump(float direction)
    {
    	stopAnimation();
    	this.setCurrentTileIndex(0);
    }
    @Override
    protected void animateLand(float direction)
    {
    	stopAnimation();
    	this.setCurrentTileIndex(0);
    }
    @Override
    protected void animateFall(float direction)
    {
    	stopAnimation();
    	this.setCurrentTileIndex(0);
    }
    
    @Override
    public void setupHud()
    {
    }
}