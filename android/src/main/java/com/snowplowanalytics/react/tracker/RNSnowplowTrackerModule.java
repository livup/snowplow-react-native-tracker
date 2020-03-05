
package com.snowplowanalytics.react.tracker;

import android.util.Log;

import java.util.UUID;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.snowplowanalytics.react.util.EventUtil;
import com.snowplowanalytics.snowplow.tracker.Emitter;
import com.snowplowanalytics.snowplow.tracker.Subject;
import com.snowplowanalytics.snowplow.tracker.Tracker;
import com.snowplowanalytics.snowplow.tracker.emitter.HttpMethod;
import com.snowplowanalytics.snowplow.tracker.emitter.RequestCallback;
import com.snowplowanalytics.snowplow.tracker.emitter.RequestSecurity;
import com.snowplowanalytics.snowplow.tracker.events.EcommerceTransaction;
import com.snowplowanalytics.snowplow.tracker.events.SelfDescribing;
import com.snowplowanalytics.snowplow.tracker.events.Structured;
import com.snowplowanalytics.snowplow.tracker.events.ScreenView;
import com.snowplowanalytics.snowplow.tracker.utils.LogLevel;

import javax.annotation.Nullable;

public class RNSnowplowTrackerModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private Tracker tracker;
    private Emitter emitter;

    public RNSnowplowTrackerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNSnowplowTracker";
    }

    public RequestCallback getCallback() {
        return new RequestCallback() {
            @Override
            public void onSuccess(int i) {
                Log.d("Tracker", "onSuccess: " + i);
            }

            @Override
            public void onFailure(int i, int i1) {
                Log.d("Tracker", "successCount: " + i + "failureCount" + i1);
            }
        };
    }

    @ReactMethod
    public void initialize(String endpoint, String method, String protocol,
                           String namespace, String appId, ReadableMap options,
                           Callback callback) {
        this.emitter = new Emitter.EmitterBuilder(endpoint, this.reactContext)
                .method(method.equalsIgnoreCase("post") ? HttpMethod.POST : HttpMethod.GET)
                .security(protocol.equalsIgnoreCase("https") ? RequestSecurity.HTTPS : RequestSecurity.HTTP)
                .callback(getCallback())
                .build();
        this.emitter.waitForEventStore();
        this.tracker = Tracker.init(new Tracker
                .TrackerBuilder(this.emitter, namespace, appId, this.reactContext)
                .base64(false)
                .level(LogLevel.VERBOSE)
                .mobileContext(true)
                .foregroundTimeout(options.hasKey("foregroundTimeout") ? options.getInt("foregroundTimeout") : 600)
                .foregroundTimeout(options.hasKey("backgroundTimeout") ? options.getInt("backgroundTimeout") : 300)
                .sessionContext(options.hasKey("setSessionContext") && options.getBoolean("setSessionContext"))
                .applicationContext(options.hasKey("setApplicationContext") && options.getBoolean("setApplicationContext"))
                .build()
        );
        Subject subject = new Subject.SubjectBuilder().build();
        this.tracker.setSubject(subject);
        Log.d("Tracker", "Initialized");
        callback.invoke(null, true);
    }

    @ReactMethod
    public void setSubjectUserId(String userId, Callback callback) {
        tracker.getSubject().setUserId(userId);
        callback.invoke(null, true);
    }

    @ReactMethod
    public void setSubjectColorDepth(Integer colorDepth, Callback callback) {
        tracker.getSubject().setColorDepth(colorDepth);
        callback.invoke(null, true);
    }

    @ReactMethod
    public void useSubjectDefaultScreenResolution(Callback callback) {
        tracker.getSubject().setDefaultScreenResolution(reactContext);
        callback.invoke(null, true);
    }

    @ReactMethod
    public void setSubjectIpAddress(String ipAddress, Callback callback) {
        tracker.getSubject().setIpAddress(ipAddress);
        callback.invoke(null, true);
    }

    @ReactMethod
    public void setSubjectLanguage(String language, Callback callback) {
        tracker.getSubject().setLanguage(language);
        callback.invoke(null, true);
    }

    @ReactMethod
    public void setSubjectNetworkUserId(String networkUserId, Callback callback) {
        tracker.getSubject().setNetworkUserId(networkUserId);
        callback.invoke(null, true);
    }

    @ReactMethod
    public void setSubjectScreenResolution(Integer width, Integer height, Callback callback) {
        tracker.getSubject().setScreenResolution(width, height);
        callback.invoke(null, true);
    }

    @ReactMethod
    public void setSubjectTimezone(String timezone, Callback callback) {
        tracker.getSubject().setTimezone(timezone);
        callback.invoke(null, true);
    }

    @ReactMethod
    public void setSubjectUseragent(String userAgent, Callback callback) {
        tracker.getSubject().setUseragent(userAgent);
        callback.invoke(null, true);
    }

    @ReactMethod
    public void setSubjectViewPort(Integer width, Integer height, Callback callback) {
        tracker.getSubject().setViewPort(width, height);
        callback.invoke(null, true);
    }

    @ReactMethod
    public void trackSelfDescribingEvent(ReadableMap event, ReadableArray contexts, Callback callback) {
        SelfDescribing trackerEvent = EventUtil.getSelfDescribingEvent(event, contexts);
        if (trackerEvent != null) {
            tracker.track(trackerEvent);
            callback.invoke(null, true);
        }
    }

    @ReactMethod
    public void trackStructuredEvent(String category, String action, String label,
                                     String property, Float value,
                                     ReadableArray contexts, Callback callback) {
        Structured trackerEvent = EventUtil.getStructuredEvent(category, action, label,
                property, value, contexts);

        if (trackerEvent != null) {
            tracker.track(trackerEvent);
            callback.invoke(null, true);
        }
    }

    @ReactMethod
    public void trackScreenViewEvent(String screenName, String screenId, String screenType,
                                     String previousScreenName, String previousScreenType,
                                     String previousScreenId, String transitionType,
                                     ReadableArray contexts, Callback callback) {
        if (screenId == null) {
            screenId = UUID.randomUUID().toString();
        }
        ScreenView trackerEvent = EventUtil.getScreenViewEvent(screenName,
                screenId, screenType, previousScreenName, previousScreenId, previousScreenType,
                transitionType, contexts);
        if (trackerEvent != null) {
            tracker.track(trackerEvent);
            callback.invoke(null, true);
        }
    }

    @ReactMethod
    public void trackEcommerceEvent(String orderId, String affiliation, Double total,
                                    Double tax,Double shipping, ReadableArray items,
                                    ReadableArray contexts, Callback callback) {
        EcommerceTransaction trackerEvent = EventUtil.getEcommerceTransactionEvent(orderId, total,
                affiliation, tax, shipping, items, contexts);
        if (trackerEvent != null) {
            tracker.track(trackerEvent);
            callback.invoke(null, true);
        }
    }
}
