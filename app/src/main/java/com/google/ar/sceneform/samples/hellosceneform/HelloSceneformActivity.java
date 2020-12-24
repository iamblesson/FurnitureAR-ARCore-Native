/*
 * Copyright 2018 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.sceneform.samples.hellosceneform;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseTransformableNode;
import com.google.ar.sceneform.ux.TransformableNode;
import java.util.Arrays;
/**
 * This is an activity that uses the Sceneform UX package to help build the 4 renderables and place
 * them using button clicks. It also contains the logic to change the color (5 colors) of the model on button click.
 */
public class HelloSceneformActivity extends AppCompatActivity implements View.OnClickListener{

  private static final String TAG = HelloSceneformActivity.class.getSimpleName();
  private static final double MIN_OPENGL_VERSION = 3.0;

  private ArFragment arFragment;
  //Declare 4 ModelRenderables
  private ModelRenderable regularChair1Renderable;
  private ModelRenderable regularChair2Renderable;
  private ModelRenderable regularChair3Renderable;
  private ModelRenderable stoolRenderable;
  //isSpawned boolean to control if an object is rendered/spawned on the scene.
  private boolean isSpawned = false;
  //initialize a boolean array of 4 elements for chair1, chair2, chair3, stool
  private  Boolean[] boolArray = {true, false, false, false};
  @Override
  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
  // CompletableFuture requires api level 24
  // FutureReturnValueIgnored is not valid
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (!checkIsSupportedDeviceOrFinish(this)) {
      return;
    }

    setContentView(R.layout.activity_ux);
    arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

    //Add listeners for the buttons on screen
    Button button1 = findViewById(R.id.Color1);
    Button button2 = findViewById(R.id.Color2);
    Button button3 = findViewById(R.id.Color3);
    Button button4 = findViewById(R.id.Color4);
    Button button5 = findViewById(R.id.Color5);
    button1.setOnClickListener(this);
    button2.setOnClickListener(this);
    button3.setOnClickListener(this);
    button4.setOnClickListener(this);
    button5.setOnClickListener(this);

    Button RegularChair1 = findViewById(R.id.RegularChair1);
    Button RegularChair2 = findViewById(R.id.RegularChair2);
    Button RegularChair3 = findViewById(R.id.RegularChair3);
    Button Table = findViewById(R.id.Stool);
    RegularChair1.setOnClickListener(this);
    RegularChair2.setOnClickListener(this);
    RegularChair3.setOnClickListener(this);
    Table.setOnClickListener(this);

    // When you build a Renderable, Sceneform loads its resources in the background while returning
    // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().

    //Build Renderable for Chair 1.
    ModelRenderable.builder()
        .setSource(this, Uri.parse("Regular Chair/modern chair 11 fbx.sfb"))
        .build()
        .thenAccept(renderable -> regularChair1Renderable = renderable)
        .exceptionally(
            throwable -> {
              Toast toast =
                  Toast.makeText(this, "Unable to load Regular Chair 1 renderable", Toast.LENGTH_LONG);
              toast.setGravity(Gravity.CENTER, 0, 0);
              toast.show();
              return null;
            });

    //Build Renderable for Chair 2.
    ModelRenderable.builder()
          .setSource(this, Uri.parse("Regular Chair 2/uploads_files_1958646_armchair_arno.sfb"))
          .build()
          .thenAccept(renderable -> regularChair2Renderable = renderable)
          .exceptionally(
                  throwable -> {
                      Toast toast =
                              Toast.makeText(this, "Unable to load Regular Chair 2 renderable", Toast.LENGTH_LONG);
                      toast.setGravity(Gravity.CENTER, 0, 0);
                      toast.show();
                      return null;
                  });

    //Build Renderable for Chair 3.
    ModelRenderable.builder()
          .setSource(this, Uri.parse("Regular Chair 3/X bang chair fbx.sfb"))
          .build()
          .thenAccept(renderable -> regularChair3Renderable = renderable)
          .exceptionally(
                  throwable -> {
                      Toast toast =
                              Toast.makeText(this, "Unable to load Regular Chair 3 renderable", Toast.LENGTH_LONG);
                      toast.setGravity(Gravity.CENTER, 0, 0);
                      toast.show();
                      return null;
                  });

    //Build Renderable for Stool.
    ModelRenderable.builder()
          .setSource(this, Uri.parse("Stool/wooden stool.sfb"))
          .build()
          .thenAccept(renderable -> stoolRenderable = renderable)
          .exceptionally(
                  throwable -> {
                      Toast toast =
                              Toast.makeText(this, "Unable to load Stool renderable", Toast.LENGTH_LONG);
                      toast.setGravity(Gravity.CENTER, 0, 0);
                      toast.show();
                      return null;
                  });

    //Registers a callback to be invoked when an ARCore Plane is tapped.
    arFragment.setOnTapArPlaneListener(
    (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
        if (regularChair1Renderable == null) {
        return;
        }
        //isSpawned is used to check if the furniture is already spawned in the scene. Condition helps prevent multiple spawning of same model.
        //It is reset only in the OnClick function executes.
        if(!isSpawned)
        {
            // Create the Anchor.
            Anchor anchor = hitResult.createAnchor();
            AnchorNode anchorNode = new AnchorNode(anchor);
            anchorNode.setParent(arFragment.getArSceneView().getScene());
            if(boolArray[0]){
                // Create the transformable for chair 1 and add it to the anchor.
                TransformableNode chair = new TransformableNode(arFragment.getTransformationSystem());
                chair.getScaleController().setMaxScale(0.8f);
                chair.getScaleController().setMinScale(0.2f);
                chair.setParent(anchorNode);
                chair.setRenderable(regularChair1Renderable);
                chair.select();
            }
            if(boolArray[1]) {
                // Create the transformable for chair 2 and add it to the anchor.
                TransformableNode chair = new TransformableNode(arFragment.getTransformationSystem());
                chair.getScaleController().setMaxScale(0.9f);
                chair.getScaleController().setMinScale(0.2f);
                //andy.setLocalRotation(Quaternion.axisAngle(new Vector3(1f, 0, 0), -90f));
                chair.setParent(anchorNode);
                chair.setRenderable(regularChair2Renderable);
                chair.select();
            }
            if(boolArray[2]){
                // Create the transformable for chair 3 and add it to the anchor.
                TransformableNode chair = new TransformableNode(arFragment.getTransformationSystem());
                chair.getScaleController().setMaxScale(0.8f);
                chair.getScaleController().setMinScale(0.2f);
                chair.setParent(anchorNode);
                chair.setRenderable(regularChair3Renderable);
                chair.select();
            }
            if(boolArray[3]){
                // Create the transformable for stool and add it to the anchor.
                TransformableNode table = new TransformableNode(arFragment.getTransformationSystem());
                table.getScaleController().setMaxScale(0.6f);
                table.getScaleController().setMinScale(0.2f);
                table.setParent(anchorNode);
                table.setRenderable(stoolRenderable);
                table.select();
            }
            isSpawned = true;
        }
    });
  }
    //onClick function handles the assignment of colors to the 4 models and sets necessary booleans to control which object to spawn.
    @Override
    public void onClick(View v) {
    Arrays.fill(boolArray, Boolean.FALSE);
    //Gets the currently selected node. Only the currently selected node can be transformed.
    // Nodes are selected automatically when they are tapped, or when the user begins to translate the node with a drag gesture.
    BaseTransformableNode node = arFragment.getTransformationSystem().getSelectedNode();
    switch (v.getId()) {
        // Set Selected Model Base Color to Blue
        case R.id.Color1:
            if (node != null) {
                node.getRenderable().getMaterial().setFloat3("baseColor", new Color(android.graphics.Color.BLUE));
            }
            break;
        // Set Selected Model Base Color to Red
        case R.id.Color2:
            if (node != null) {
                node.getRenderable().getMaterial().setFloat3("baseColor", new Color(android.graphics.Color.RED));
            }
            break;
        // Set Selected Model Base Color to Cyan
        case R.id.Color3:
            if (node != null) {
                node.getRenderable().getMaterial().setFloat3("baseColor", new Color(android.graphics.Color.CYAN));
            }
            break;
        // Set Selected Model Base Color to Yellow
        case R.id.Color4:
            if (node != null) {
                node.getRenderable().getMaterial().setFloat3("baseColor", new Color(android.graphics.Color.YELLOW));
            }
            break;
        // Set Selected Model Base Color to the Original Color
        case R.id.Color5:
            if (node != null) {
                node.getRenderable().getMaterial().setFloat3("baseColor", new Color(android.graphics.Color.WHITE));
            }
            break;
        // Set Boolean to help spawn Regular Chair 1
        case R.id.RegularChair1:
            boolArray[0] = true;
            break;
        // Set Boolean to help spawn Regular Chair 2
        case R.id.RegularChair2:
            boolArray[1] = true;
            break;
        // Set Boolean to help spawn Regular Chair 3
        case R.id.RegularChair3:
            boolArray[2] = true;
            break;
            // Set Boolean to help spawn Stool
        case R.id.Stool:
            boolArray[3] = true;
            break;
        }
        //isSpawned is set to false to enable the user spawn the same furniture or a new one (only on clicking on the buttons provided).
        isSpawned = false;
    }
  /**
   * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
   * on this device.
   *
   * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
   *
   * <p>Finishes the activity if Sceneform can not run
   */
    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
          Log.e(TAG, "Sceneform requires Android N or later");
          Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
          activity.finish();
          return false;
        }
    String openGlVersionString =
        ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
            .getDeviceConfigurationInfo()
            .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
          Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
          Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
              .show();
          activity.finish();
          return false;
        }
        return true;
    }
}
