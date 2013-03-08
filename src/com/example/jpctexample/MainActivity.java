package com.example.jpctexample;

import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Logger;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;

public class MainActivity extends Activity {

	private static MainActivity master = null;

	ArrayList<Object3D> objects;
	Object3D plane = null;
	private GLSurfaceView mGLView;
	private MyRenderer renderer = null;
	private FrameBuffer fb = null;
	private World world = null;
	private RGBColor back = new RGBColor(50, 50, 100);

	private Light sun = null;
	
	private SeekBar mSeekBar;
	private long delayTime;

	protected void onCreate(Bundle savedInstanceState) {

		if (master != null) {
			copy(master);
		}

		super.onCreate(savedInstanceState);
		mGLView = new GLSurfaceView(getApplication());

		
		renderer = new MyRenderer();
		mGLView.setRenderer(renderer);
		setContentView(mGLView);
		
		addContentView(getOverlayView(),
                new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
		
		startSeekBar();
		
		delayTime = 500;
		
	}

	private void startSeekBar() {
		mSeekBar = (SeekBar) findViewById(R.id.seekBar1);
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				Toast.makeText(getApplicationContext(), "Speed " + seekBar.getProgress(), Toast.LENGTH_LONG).show();
				
				delayTime = - ((seekBar.getProgress() * 100) - 1000);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				
			}
		});
	}

	private View getOverlayView() {
		return View.inflate(getApplicationContext(), R.layout.activity_main, null);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mGLView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mGLView.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	private void copy(Object src) {
		try {
			Logger.log("Copying data from master Activity!");
			Field[] fs = src.getClass().getDeclaredFields();
			for (Field f : fs) {
				f.setAccessible(true);
				f.set(this, f.get(src));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	class MyRenderer implements GLSurfaceView.Renderer {

		private long time = System.currentTimeMillis();
		
		public MyRenderer() {
			objects = new ArrayList<Object3D>();
		}

		public Object3D newCube() {
			Object3D cube = Primitives.getCube(1f);
			float x = (float) (Math.random() * 10);
			float y = (float) (Math.random() * 20);
			float z = (float) (Math.random() * 30);
			cube.setOrigin(new SimpleVector(x, y, z));
			cube.setAdditionalColor(new RGBColor(20, 250, 23));
			cube.strip();
			cube.build();

			return cube;
		}

		public void onSurfaceChanged(GL10 gl, int w, int h) {
			if (fb != null) {
				fb.dispose();
			}
			fb = new FrameBuffer(gl, w, h);

			if (master == null) {

				world = new World();
				world.setAmbientLight(210, 220, 20);

				sun = new Light(world);
				sun.setIntensity(250, 250, 250);

				world.addObject(newCube());
				world.addObject(newCube());
				world.addObject(newCube());
				world.addObject(newCube());
				world.addObject(newCube());

				Camera cam = world.getCamera();
				cam.setPosition(0, 0, 0);
				cam.lookAt(new SimpleVector(new float[] { 0, 0, 0 }));
				cam.moveCamera(Camera.CAMERA_MOVEOUT, 30);

				SimpleVector sv = new SimpleVector();

				sv.y -= 100;
				sv.z -= 100;
				sun.setPosition(sv);
				MemoryHelper.compact();

				if (master == null) {
					Logger.log("Saving master Activity!");
					master = MainActivity.this;
				}
			}
		}

		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		}

		public void onDrawFrame(GL10 gl) {
			fb.clear(back);
			world.renderScene(fb);
			world.draw(fb);
			fb.display();

			if (System.currentTimeMillis() - time >= delayTime) {
					for (int i = 0; i < world.getSize(); i++) {
						int valor = ((int) (Math.random() * 10) % 3);
						SimpleVector vector = null;
						switch (valor) {
						case 0:
							vector = world.getObject(i).getXAxis();
							break;
						case 1:
							vector = world.getObject(i).getYAxis();
							break;
						case 2:
							vector = world.getObject(i).getZAxis();
							break;
						default:
							break;
						}
						if (time % 2 == 0) {
							vector.scalarMul(-1);
						} else {
							vector.scalarMul(1);
						}
						world.getObject(i).translate(vector);
						time = System.currentTimeMillis();
					}
			}
		}
	}

}
