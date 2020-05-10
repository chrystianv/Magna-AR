package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.ar.core.Camera;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.collision.Ray;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.ux.ArFragment;

import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.R;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.compass.CompassActivity;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.settings.SettingsActivity;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers.CommonUtils;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers.DisplayRotationHelper;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers.sensors.MagnetometerData;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers.PermissionHelper;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers.SnackbarUtility;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers.TrackingStateHelper;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers.VideoRecorder;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.actors.stablemagarrow.model.StaticArrowModel;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.actors.stablemagsphere.model.StaticSphereModel;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.actors.wirewithcurrent.model.WireWithCurrentActor;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.fields.databacked.DataBackedFieldCreator;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.filters.gridarrow.GridFilter;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.FieldGroup;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.mag.Field;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.render.Filter;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.render.Model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.R.id.constraintLayoutMain;
import static net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.R.id.vr_fragment;

public final class VisualizerActivity extends AppCompatActivity {
    TextView high;
    TextView low;

    ImageView heatmapImageView;
    private File videoDirectory;

    // VideoRecorder encapsulates all the video recording functionality.
    private VideoRecorder videoRecorder;
    public int isRecording;

    public int isAddingArrows;

    int counterTap = 0;
    public boolean readout = true;
    public boolean heatmap = false;

    private DisplayRotationHelper mDisplayRotationHelper;

    private int accuracy = 3;
    private static final int PORTRAIT_ORIENTATION = 0;
    private static final int LANDSCAPE_ORIENTATION = 1;
    private static final String TAG = "PhysicsBoxVisualizer";

    // Handles
    private SensorManager mSensorManager;
    private SharedPreferences.OnSharedPreferenceChangeListener toggleNumbers;

    // Data
    private int orientation = PORTRAIT_ORIENTATION; // default
    // Data groups
    private MagnetometerData recorder;
    private FieldGroup fields;
    private Scene scene;
    private ArrayList<StaticArrowModel> stableArrows = new ArrayList<>();
    private ArrayList<StaticSphereModel> stableSpheres = new ArrayList<>();

    // Views & Fragments
    private TextView[] fieldTextViews = new TextView[4];
    private ArFragment arFragment;
    private DataBackedFieldCreator tempCreator;
    private ImageButton infoButton;
    private ImageButton settingsButton;
    private ImageButton restartButton;
    private ImageButton screenshotButton;
    private ImageButton compassButton;

    private View blurlayout;
    private View redlayout;
    private View greenlayout;
    private View bluelayout;

    private TextView xtextview;
    private TextView ytextview;
    private TextView ztextview;

    ConstraintLayout globalReference;

    int launch = 0;

    private static boolean valueOfCountModified = false;
    private boolean rotatedFlag = false;
    private int rotation = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check if device is compatible with ARCore and SceneKit
        if (!CommonUtils.checkIsSupportedDeviceOrFinish(VisualizerActivity.this)) {
            return;
        }
        // ARCore requires camera permissions to operate.
        if (!PermissionHelper.hasCameraPermission(this)) {
            PermissionHelper.requestCameraPermission(this);
            return;
        }
        setOrientation();
        setContentView(R.layout.activity_visualizer);
        bindViews();
        mSensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        recorder = new MagnetometerData();
        bindRecorderData();
        setListeners();
        // Modify UI

        launchCountDialog();
    }

    public void setOrientation() {
        final SharedPreferences appPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        orientation = appPreferences.getInt("orientation", orientation);
        if (orientation == 1) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    /**
     * Bind MutableLiveData elements to various ui elements.
     */
    private void bindRecorderData() {
        recorder.field.observe(this, f -> {
            final int[] fieldStringIds = {R.string.x_mag_readout, R.string.y_mag_readout, R.string.z_mag_readout, R.string.total_mag_readout};
            for (int i = 0; i < 4; ++i) {
                if (fieldTextViews[i] != null) {
                    fieldTextViews[i].setText(
                            getResources().getString(fieldStringIds[i], f[i])
                    );
                }
            }
        });
    }

    /**
     * Grab handles for Views.
     */
    private void bindViews() {
        final int[] magneticFieldIds = {
                R.id.xTextView,
                R.id.yTextView,
                R.id.zTextView,
                R.id.TotalMagneticTextView,
        };
        for (int i = 0; i < magneticFieldIds.length; ++i) {
            fieldTextViews[i] = findViewById(magneticFieldIds[i]);
        }
        infoButton = findViewById(R.id.infoButton);
        settingsButton = findViewById(R.id.settingsButton);
        restartButton = findViewById(R.id.restartButton);
        screenshotButton = findViewById(R.id.screenshotButton);
        blurlayout = findViewById(R.id.blurLayout);
        compassButton = findViewById(R.id.compassButton);
        redlayout = findViewById(R.id.redblur);
        greenlayout = findViewById(R.id.greenblur);
        bluelayout = findViewById(R.id.blueblur);
        xtextview = findViewById(R.id.xtextview);
        ytextview = findViewById(R.id.ytextview);
        ztextview = findViewById(R.id.ztextview);
        heatmapImageView = findViewById(R.id.heatmapImage);
        high = findViewById(R.id.hightTextViewField);
        low = findViewById(R.id.lowTextViewField);
        globalReference = (ConstraintLayout) findViewById(constraintLayoutMain);

        // 3D Views
        bindAr();
    }

    /**
     * Grab handles for Renderables.
     */
    private void bindAr() {
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(vr_fragment);
    }

    /**
     * Set listeners for recordFab and arFragment.
     */
    private void setListeners() {
        // TODO: create actual interface to avoid these commenting shenanigans
        //     arFragment.setOnTapArPlaneListener(this::createScene);
        arFragment.getArSceneView().setOnClickListener(this::createTempArrow);

        restartButton.setOnClickListener(v -> restartActivity());
        infoButton.setOnClickListener(v -> {
            int i = 0;

            isAddingArrows++;

            if (isAddingArrows == 1) {
                startLoop(i);
                SnackbarUtility.showSnackbarTypeLong(settingsButton, getString(R.string.tap_vectors));
                DrawableCompat.setTint(infoButton.getDrawable(), ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
            }

            if (isAddingArrows > 1) {
                isAddingArrows = 0;
                SnackbarUtility.showSnackbarTypeLong(infoButton, getString(R.string.stopped_adding_vectors));
                DrawableCompat.setTint(infoButton.getDrawable(), ContextCompat.getColor(getApplicationContext(), R.color.white));
            }
        });

        compassButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),
                CompassActivity.class)));

        settingsButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),
                SettingsActivity.class)));

        screenshotButton.setOnClickListener(v -> {
            if (!PermissionHelper.hasWriteStoragePermission(VisualizerActivity.this)) {
                PermissionHelper.requestStoragePermission(VisualizerActivity.this);
                return;
            }

            File mediaDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    + "/PhysicsToolboxAR");
            // Create a folder if not exists
            if (!mediaDir.exists()) {
                mediaDir.mkdir();
            }
            takePhoto();
        });


        screenshotButton.setOnLongClickListener(v -> {
            // Initialize the VideoRecorder.
            isRecording++;

            if (isRecording == 1) {
                videoRecorder = new VideoRecorder();
                int orientation = getResources().getConfiguration().orientation;
                videoRecorder.setVideoQuality(CamcorderProfile.QUALITY_1080P, orientation);
                videoRecorder.setSceneView(arFragment.getArSceneView());
                // Returns true if recording has started.
                boolean recording = videoRecorder.onToggleRecord();
                DrawableCompat.setTint(screenshotButton.getDrawable(), ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                screenshotButton.setElevation(10);
                SnackbarUtility.showSnackbarTypeLong(settingsButton, getString(R.string.video_recording_started));
            } else if (isRecording > 1) {
                // Returns false if recording has stopped.
                boolean recording = videoRecorder.onToggleRecord();
                screenshotButton.setImageResource(R.drawable.sphere);
                DrawableCompat.setTint(screenshotButton.getDrawable(), ContextCompat.getColor(getApplicationContext(), R.color.white));
                isRecording = 0;
                SnackbarUtility.showSnackbarTypeLong(settingsButton, getString(R.string.video_saved));
            }

            return true;
        });

        blurlayout.setOnClickListener(v -> {
            counterTap++;

            if (counterTap == 1) {
                redlayout.setVisibility(View.VISIBLE);
                greenlayout.setVisibility(View.VISIBLE);
                bluelayout.setVisibility(View.VISIBLE);

                xtextview.setVisibility(View.VISIBLE);
                ytextview.setVisibility(View.VISIBLE);

                ztextview.setVisibility(View.VISIBLE);

                for (int i = 0; i < 3; ++i) {
                    fieldTextViews[i].setVisibility(View.VISIBLE);
                }
            } else {
                redlayout.setVisibility(View.INVISIBLE);
                greenlayout.setVisibility(View.INVISIBLE);
                bluelayout.setVisibility(View.INVISIBLE);

                xtextview.setVisibility(View.INVISIBLE);
                ytextview.setVisibility(View.INVISIBLE);

                ztextview.setVisibility(View.INVISIBLE);

                for (int i = 0; i < 3; ++i) {
                    fieldTextViews[i].setVisibility(View.INVISIBLE);
                }
                counterTap = 0;
            }
        });
    }






    /* ****************************** Get settings ******************************** */

    private boolean shouldUseArrow() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("arrows", false);
    }

    private boolean shouldUseSphere() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("spheres", false);
    }

    private boolean shouldHaveNumbers() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("numerical", false);
    }

    /* ***************************** Scene creation ******************************* */

    /**
     * Make the calls to create a scene and subsequently remove the listeners responsible, then add new listeners.
     * <p>
     * Progresses activity to "ready" state from the "initializing" state.
     */
    private void createScene(HitResult hit, Plane plane, MotionEvent motion) {
        if (motion.getActionMasked() == MotionEvent.ACTION_UP) {
            arFragment.setOnTapArPlaneListener(null);
            scene = arFragment.getArSceneView().getScene();
            AnchorNode anchorNode = new AnchorNode(hit.createAnchor());
            fields = new FieldGroup(anchorNode, new GridFilter(Vector3.one(), Vector3.one().scaled(5)));
            scene.addChild(anchorNode);
            fields.filter().loadAsset(this);
            scene.addOnUpdateListener(this::onUpdate);
            scene.setOnTouchListener(this::addWireWithCurrent);
        }
        recorder.mode.observe(this, m -> {
            switch (m) {
                case RECORDING:
                    tempCreator = new DataBackedFieldCreator(fields);
                    recorder.field.observeForever(tempCreator);
                    SnackbarUtility.showSnackbarTypeLong(settingsButton, getString(R.string.data_recording_started));
                    break;
                case STOPPED:
                    recorder.field.removeObserver(tempCreator);
                    SnackbarUtility.showSnackbarTypeLong(settingsButton, getString(R.string.data_recording_stopped));
                    DataBackedFieldCreator temp = tempCreator;
                    tempCreator = null;
                    fields.addField(temp.create());
                    break;
            }
        });
    }


    private void createTempArrow(View view) {
        Session ses = arFragment.getArSceneView().getSession();

        Camera cam = arFragment.getArSceneView().getArFrame().getCamera();
        if (cam.getTrackingState() == TrackingState.TRACKING) {
            Pose cameraPose = cam.getDisplayOrientedPose();
            // init empty fields + scene if null
            if (scene == null) {
                assert ses != null;
                AnchorNode anchorNode = new AnchorNode(
                        ses.createAnchor(Pose.IDENTITY)
                );
                scene = arFragment.getArSceneView().getScene();
                fields = new FieldGroup(anchorNode, new Filter() {
                    Node n = new Node();

                    @Override
                    public void update(@NonNull List<? extends Field> fields, @NonNull Node root) {}

                    @NonNull
                    @Override
                    public Node node() {
                        return n;
                    }

                    @Override
                    public void loadAsset(Context ctx) {}
                });
                scene.addChild(anchorNode);
            }
            float[] currentMag = recorder.field.getValue();
            if (currentMag != null) {
                currentMag = cameraPose.rotateVector(currentMag);
                Vector3 mag = new Vector3(currentMag[0], currentMag[1], currentMag[2]);
                if (mag.length() > 1e-9) {
                    float[] translation = cameraPose
                            .compose(
                                    Pose.makeTranslation(0, 0, -scene.getCamera().getNearClipPlane() * 2)
                            ).extractTranslation().getTranslation();
                    Model m;
                    if (shouldUseSphere()) {
                        m = new StaticSphereModel(
                                new Vector3(translation[0], translation[1], translation[2]),
                                mag, shouldHaveNumbers()
                        );
                        stableSpheres.add((StaticSphereModel) m);
                        // NOTE: Uncomment if we ever get more than 2 models. Alternatively, combine
                        //   shouldUse* methods and return an enum instead
                    // } else if (shouldUseArrow()) {
                    //     m = new StaticArrowModel(
                    //             new Vector3(translation[0], translation[1], translation[2]),
                    //             mag
                    //     );
                    } else {
                        // NOTE: Same as above.
                        // Log.e("Model Creation", "No option selected, using default.");
                        m = new StaticArrowModel(
                                new Vector3(translation[0], translation[1], translation[2]),
                                mag, shouldHaveNumbers()
                        );
                        stableArrows.add((StaticArrowModel) m);
                    }
                    m.loadAsset(getApplicationContext());
                    fields.addModel(m);
                }
            }
        }

        // If not tracking, don't draw 3D objects, show tracking failure reason instead.
        if (cam.getTrackingState() == TrackingState.PAUSED) {
            SnackbarUtility.showSnackbarTypeLong(settingsButton, TrackingStateHelper.getTrackingFailureReasonString(cam));
            return;
        }
    }

    /* **************************** Scene Interaction ***************************** */

    /**
     * Update per frame things here.
     *
     * @param frameTime
     */
    private void onUpdate(FrameTime frameTime) {
        // TODO: if nothing else is needed here, delete this.
        fields.step(frameTime);
        View contentView = findViewById(android.R.id.content);
    }

    /**
     * Make a cylinder from radius, height, coordinate (relates a center of the hit), material
     */
    private boolean addWireWithCurrent(HitTestResult hitTestResult, MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (hitTestResult.getNode() == null) {
                WireWithCurrentActor actor = new WireWithCurrentActor(arFragment.getTransformationSystem());
                // locate collision
                Ray r = scene.getCamera().screenPointToRay(motionEvent.getX(), motionEvent.getY());
                actor.asNode().setWorldPosition(fields.intersect(r));
                actor.model().loadAsset(getApplicationContext());
                fields.addActor(actor);
                return true;
            }
        }
        return false;
    }

    private boolean toggleRecord(View v, MotionEvent event) {
        if (event.getActionButton() == MotionEvent.ACTION_BUTTON_RELEASE) {
            recorder.toggleState();
            return true;
        }
        return false;
    }

    /* *********************** Additional lifetime methods ************************ */

    @Override
    public void onResume() {
        super.onResume();

        bindViews();
        if (mSensorManager == null) {
            mSensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        }

        mSensorManager.unregisterListener(recorder);

        mSensorManager.registerListener(
                recorder,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL
        );

        if (arFragment != null) {
            // hiding the plane discovery
            arFragment.getPlaneDiscoveryController().hide();
            arFragment.getPlaneDiscoveryController().setInstructionView(null);
            arFragment.getArSceneView().getPlaneRenderer().setEnabled(false);
        }

        for (StaticArrowModel sam : this.stableArrows) {
            sam.setNumberVisibility(shouldHaveNumbers());
        }
        for (StaticSphereModel ssm : this.stableSpheres) {
            ssm.setNumberVisibility(shouldHaveNumbers());
        }

        final SharedPreferences appPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        readout = appPreferences.getBoolean("readout", true);

        heatmap = appPreferences.getBoolean("heatmap_switch", false);

        if (!readout) {
            blurlayout.setVisibility(View.INVISIBLE);
            for (int i = 0; i < 4; ++i) {
                fieldTextViews[i].setVisibility(View.INVISIBLE);
            }
        } else {
            blurlayout.setVisibility(View.VISIBLE);
        }

        if (!heatmap) {
            heatmapImageView.setVisibility(View.INVISIBLE);
            low.setVisibility(View.INVISIBLE);
            high.setVisibility(View.INVISIBLE);
        } else {
            heatmapImageView.setVisibility(View.VISIBLE);
            low.setVisibility(View.VISIBLE);
            high.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        mSensorManager.unregisterListener(recorder);
        recorder.setState(false);
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void alertDialog() {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.howtouseapp));
        builder.setIcon(R.drawable.arrow_dialog);
        builder.setMessage(getString(R.string.howtousedescription));
        builder.setPositiveButton("OK", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    public void accuracyDialog() {
        accuracy = recorder.accuracy;
        if (accuracy >= 1) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("De-magnetize Device");
        builder.setIcon(R.drawable.demagnetize);

        // set message
        builder.setMessage("Your device appears to have magnetized. To de-magnetize your device, move it in a figure 8 in multiple dimensions..");

        // add a button
        builder.setPositiveButton("OK", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void takePhoto() {
        ArSceneView view = arFragment.getArSceneView();

        // Create a bitmap the size of the scene view.
        final Bitmap bitmap = Bitmap.createBitmap(
                view.getWidth(),
                view.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        // Create a handler thread to offload the processing of the image.
        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();

        // Make the request to copy.
        PixelCopy.request(view, bitmap, (copyResult) -> {
            if (copyResult != PixelCopy.SUCCESS) {
                SnackbarUtility.showSnackbarTypeShort(settingsButton, "Failed to take screenshot");
            }

            try {
                saveBitmapToDisk(bitmap);
            } catch (IOException e) {
                Toast toast = Toast.makeText(
                        VisualizerActivity.this,
                        e.toString(),
                        Toast.LENGTH_LONG
                );
                toast.show();
                return;
            }

            SnackbarUtility.showSnackbarTypeShort(settingsButton, "Screenshot saved in /Pictures/PhysicsToolboxAR/");
            handlerThread.quitSafely();
        }, new Handler(handlerThread.getLooper()));
    }

    public void saveBitmapToDisk(Bitmap bitmap) throws IOException {
        if (videoDirectory == null) {
            File pictureRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            videoDirectory = new File(pictureRoot + "/PhysicsToolboxAR");
        }

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
        String formattedDate = df.format(c.getTime());

        File mediaFile = new File(videoDirectory, "FieldVisualizer" + formattedDate + ".jpeg");

        FileOutputStream fileOutputStream = new FileOutputStream(mediaFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fileOutputStream);
        fileOutputStream.flush();
        fileOutputStream.close();
    }

    public void launchCountDialog() {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(VisualizerActivity.this);

        if (!valueOfCountModified) {
            launch = preferences.getInt("launchCount", 0);
            if (launch <= 4) {
                preferences.edit().putInt("launchCount", ++launch).apply();

                if (launch > 1) {
                    alertDialog();
                }
            }
        }
    }

    private void startLoop(final int i) {
        if (isAddingArrows == 1) {
            new Handler().postDelayed(() -> {
                Log.e("i", "" + i);
                arFragment.getArSceneView().performClick();
                startLoop(i + 1);
            }, 500);
        }
    }
}

