package com.uacapstone.red.base;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.BoundCamera;
import org.andengine.entity.scene.Scene;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.app.Activity;
import android.util.DisplayMetrics;

import com.uacapstone.red.GameActivity;
import com.uacapstone.red.manager.ResourceManager;
import com.uacapstone.red.manager.SceneManager.SceneType;

/**
 * @author Mateusz Mysliwiec
 * @author www.matim-dev.com
 * @version 1.0
 */
public abstract class BaseScene extends Scene
{
    //---------------------------------------------
    // VARIABLES
    //---------------------------------------------
    
    protected Engine engine;
    protected GameActivity activity;
    protected ResourceManager resourceManager;
    protected VertexBufferObjectManager vbom;
    protected BoundCamera camera;
    
    //---------------------------------------------
    // CONSTRUCTOR
    //---------------------------------------------
    
    public BaseScene()
    {
        this.resourceManager = ResourceManager.getInstance();
        this.engine = resourceManager.engine;
        this.activity = resourceManager.activity;
        this.vbom = resourceManager.vbom;
        this.camera = resourceManager.camera;
        createScene();
    }
    
    //---------------------------------------------
    // METHODS
    //---------------------------------------------
    
    public void resetCamera()
    {
        camera.setHUD(null);
        camera.setChaseEntity(null);
        camera.setBoundsEnabled(false);
        camera.setCenter(0, 0);
    }
    
    //---------------------------------------------
    // ABSTRACTION
    //---------------------------------------------
    
    public abstract void createScene();
    
    public abstract void onBackKeyPressed();
    
    public abstract SceneType getSceneType();
    
    public abstract void disposeScene();
}
