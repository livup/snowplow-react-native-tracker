
package com.snowplowanalytics.react.tracker;

import java.util.UUID;
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
import com.snowplowanalytics.snowplow.tracker.emitter.RequestSecurity;
import com.snowplowanalytics.snowplow.tracker.events.SelfDescribing;
import com.snowplowanalytics.snowplow.tracker.events.Structured;
import com.snowplowanalytics.snowplow.tracker.events.ScreenView;

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

    @ReactMethod
    public void initialize(String endpoint, String method, String protocol,
                           String namespace, String appId, boolean enableGeoLocation,
                           ReadableMap options) {
        this.emitter = new Emitter.EmitterBuilder(endpoint, this.reactContext)
                .method(method.equalsIgnoreCase("post") ? HttpMethod.POST : HttpMethod.GET)
                .security(protocol.equalsIgnoreCase("https") ? RequestSecurity.HTTPS : RequestSecurity.HTTP)
                .build();
        this.emitter.waitForEventStore();
        this.tracker = Tracker.init(new Tracker
                .TrackerBuilder(this.emitter, namespace, appId, this.reactContext)
                .base64(false)
                .mobileContext(true)
                .geoLocationContext(enableGeoLocation)
                .screenviewEvents(options.hasKey("autoScreenView") ? options.getBoolean("autoScreenView") : false)
                .build()
        );
        Subject subject = new Subject.SubjectBuilder().build();
        this.tracker.setSubject(subject);
    }

    @ReactMethod
    public void setSubjectUserId(String userId) {
        tracker.getSubject().setUserId(userId);
    }

    @ReactMethod
    public void setSubjectColorDepth(Integer colorDepth) {
        tracker.getSubject().setColorDepth(colorDepth);
    }

    @ReactMethod
    public void useSubjectDefaultScreenResolution() {
        tracker.getSubject().setDefaultScreenResolution(reactContext);
    }

    @ReactMethod
    public void setSubjectIpAddress(String ipAddress) {
        tracker.getSubject().setIpAddress(ipAddress);
    }

    @ReactMethod
    public void setSubjectLanguage(String language) {
        tracker.getSubject().setLanguage(language);
    }

    @ReactMethod
    public void setSubjectNetworkUserId(String networkUserId) {
        tracker.getSubject().setNetworkUserId(networkUserId);
    }

    @ReactMethod
    public void setSubjectUserId(Integer width, Integer height) {
        tracker.getSubject().setScreenResolution(width, height);
    }

    @ReactMethod
    public void setSubjectTimezone(String timezone) {
        tracker.getSubject().setTimezone(timezone);
    }

    @ReactMethod
    public void setSubjectUseragent(String useragent) {
        tracker.getSubject().setUseragent(useragent);
    }

    @ReactMethod
    public void setSubjectViewPort(Integer width, Integer height) {
        tracker.getSubject().setViewPort(width, height);
    }

    @ReactMethod
    public void trackSelfDescribingEvent(ReadableMap event, ReadableArray contexts) {
        SelfDescribing trackerEvent = EventUtil.getSelfDescribingEvent(event, contexts);
        if (trackerEvent != null) {
            tracker.track(trackerEvent);
        }
    }

    @ReactMethod
    public void trackStructuredEvent(String category, String action, String label,
                                     String property, Float value, ReadableArray contexts) {
        Structured trackerEvent = EventUtil.getStructuredEvent(category, action, label,
                property, value, contexts);
        if (trackerEvent != null) {
            tracker.track(trackerEvent);
        }
    }

    @ReactMethod
    public void trackScreenViewEvent(String screenName, String screenId, String screenType,
                                     String previousScreenName, String previousScreenType,
                                     String previousScreenId, String transitionType,
                                     ReadableArray contexts) {
        if (screenId == null) {
          screenId = UUID.randomUUID().toString();
        }
        ScreenView trackerEvent = EventUtil.getScreenViewEvent(screenName,
                screenId, screenType, previousScreenName, previousScreenId, previousScreenType,
                transitionType, contexts);
        if (trackerEvent != null) {
            tracker.track(trackerEvent);
        }
    }
}
