package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.R;

import java.lang.ref.WeakReference;

/**
 * Helper class for showing compact, non-intrusive notifications
 * that appear at the top of the screen and auto-dismiss
 */
public class CompactNotificationHelper {

    private static final long ANIMATION_DURATION = 300;
    private static final long SHORT_DURATION = 2000;
    private static final long LONG_DURATION = 3500;

    // WeakReference: a static strong reference to a View pins its Activity in
    // memory if the notification never finishes dismissing (e.g. the activity
    // is destroyed while one is showing, so the exit animation never runs).
    private static WeakReference<View> currentNotification = null;
    private static Handler dismissHandler = new Handler(Looper.getMainLooper());
    private static Runnable dismissRunnable = null;

    public enum NotificationType {
        INFO,
        SUCCESS,
        WARNING,
        ERROR
    }

    /**
     * Show a compact notification at the top of the screen
     * @param activity The activity context
     * @param message The message to display
     * @param type The type of notification (affects color)
     * @param duration Duration in milliseconds (use 0 for auto short duration)
     */
    public static void showNotification(Activity activity, String message, NotificationType type, long duration) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        // Remove any existing notification
        dismissCurrentNotification();

        // Get the root view
        ViewGroup rootView = activity.findViewById(android.R.id.content);
        if (rootView == null) {
            return;
        }

        // Create the notification view
        CardView notificationCard = new CardView(activity);
        notificationCard.setRadius(dpToPx(activity, 8));
        notificationCard.setCardElevation(dpToPx(activity, 6));
        notificationCard.setUseCompatPadding(false);
        notificationCard.setPreventCornerOverlap(true);

        // Set background color based on type
        int backgroundColor = getBackgroundColor(activity, type);
        notificationCard.setCardBackgroundColor(backgroundColor);

        // Create text view for the message
        TextView messageText = new TextView(activity);
        messageText.setText(message);
        messageText.setTextColor(ContextCompat.getColor(activity, android.R.color.white));
        messageText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        messageText.setPadding(
            dpToPx(activity, 16),
            dpToPx(activity, 12),
            dpToPx(activity, 16),
            dpToPx(activity, 12)
        );
        messageText.setGravity(Gravity.CENTER);
        messageText.setMaxLines(2);

        notificationCard.addView(messageText);

        // Set layout params for the notification
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.bottomMargin = dpToPx(activity, 100); // Above the bottom UI buttons
        params.leftMargin = dpToPx(activity, 32);
        params.rightMargin = dpToPx(activity, 32);

        notificationCard.setLayoutParams(params);

        // Initially invisible and scaled down
        notificationCard.setAlpha(0f);
        notificationCard.setScaleX(0.8f);
        notificationCard.setScaleY(0.8f);
        notificationCard.setTranslationY(dpToPx(activity, 20)); // Start from below

        // Add to root view
        rootView.addView(notificationCard);
        currentNotification = new WeakReference<>(notificationCard);

        // Animate in
        notificationCard.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .translationY(0)
            .setDuration(ANIMATION_DURATION)
            .setListener(null)
            .start();

        // Schedule auto-dismiss
        long dismissDuration = duration > 0 ? duration : SHORT_DURATION;
        dismissRunnable = new Runnable() {
            @Override
            public void run() {
                // Clear the static slot so this runnable (and the view/root it
                // captures) stops being reachable once it has fired.
                if (dismissRunnable == this) {
                    dismissRunnable = null;
                }
                dismissNotification(notificationCard, rootView);
            }
        };
        dismissHandler.postDelayed(dismissRunnable, dismissDuration);

        // Allow tap to dismiss
        notificationCard.setOnClickListener(v -> {
            dismissHandler.removeCallbacks(dismissRunnable);
            dismissNotification(notificationCard, rootView);
        });
    }

    /**
     * Show a short duration notification
     */
    public static void showShort(Activity activity, String message) {
        showNotification(activity, message, NotificationType.INFO, SHORT_DURATION);
    }

    /**
     * Show a long duration notification
     */
    public static void showLong(Activity activity, String message) {
        showNotification(activity, message, NotificationType.INFO, LONG_DURATION);
    }

    /**
     * Show a success notification
     */
    public static void showSuccess(Activity activity, String message) {
        showNotification(activity, message, NotificationType.SUCCESS, SHORT_DURATION);
    }

    /**
     * Show a warning notification
     */
    public static void showWarning(Activity activity, String message) {
        showNotification(activity, message, NotificationType.WARNING, LONG_DURATION);
    }

    /**
     * Show an error notification
     */
    public static void showError(Activity activity, String message) {
        showNotification(activity, message, NotificationType.ERROR, LONG_DURATION);
    }

    /**
     * Dismiss the current notification if any
     */
    private static void dismissCurrentNotification() {
        if (dismissRunnable != null) {
            dismissHandler.removeCallbacks(dismissRunnable);
            dismissRunnable = null;
        }

        View current = currentNotification != null ? currentNotification.get() : null;
        if (current != null && current.getParent() != null) {
            ViewGroup parent = (ViewGroup) current.getParent();
            dismissNotification(current, parent);
        }
    }

    /**
     * Animate out and remove the notification
     */
    private static void dismissNotification(View notification, ViewGroup parent) {
        if (notification == null || parent == null) {
            return;
        }

        notification.animate()
            .alpha(0f)
            .scaleX(0.8f)
            .scaleY(0.8f)
            .translationY(dpToPx(notification.getContext(), 20))
            .setDuration(ANIMATION_DURATION)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    parent.removeView(notification);
                    if (currentNotification != null && notification == currentNotification.get()) {
                        currentNotification = null;
                    }
                }
            })
            .start();
    }

    /**
     * Get background color based on notification type
     */
    private static int getBackgroundColor(Context context, NotificationType type) {
        switch (type) {
            case SUCCESS:
                return ContextCompat.getColor(context, android.R.color.holo_green_dark);
            case WARNING:
                return ContextCompat.getColor(context, android.R.color.holo_orange_dark);
            case ERROR:
                return ContextCompat.getColor(context, android.R.color.holo_red_dark);
            case INFO:
            default:
                // Dark semi-transparent background for info
                return 0xE6303030; // Dark gray with transparency
        }
    }

    /**
     * Convert dp to pixels
     */
    private static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.getResources().getDisplayMetrics()
        );
    }
}
