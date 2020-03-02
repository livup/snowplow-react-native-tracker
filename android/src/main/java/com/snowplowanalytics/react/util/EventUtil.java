package com.snowplowanalytics.react.util;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableArray;
import com.snowplowanalytics.snowplow.tracker.events.EcommerceTransaction;
import com.snowplowanalytics.snowplow.tracker.events.EcommerceTransactionItem;
import com.snowplowanalytics.snowplow.tracker.events.SelfDescribing;
import com.snowplowanalytics.snowplow.tracker.events.Structured;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.events.ScreenView;

import java.util.ArrayList;
import java.util.List;

public class EventUtil {
    private static List<SelfDescribingJson> getContexts(ReadableArray contexts) {
        List<SelfDescribingJson> nativeContexts = new ArrayList<>();
        for (int i = 0; i < contexts.size(); i++) {
            SelfDescribingJson json = EventUtil.getSelfDescribingJson(contexts.getMap(i));
            nativeContexts.add(json);
        }
        return nativeContexts;
    }

    private static SelfDescribingJson getSelfDescribingJson(ReadableMap json) {
        String schema = json.getString("schema");
        ReadableMap dataMap = json.getMap("data");
        if (schema != null && dataMap != null) {
            return new SelfDescribingJson(schema, dataMap.toHashMap());
        } else {
            // log error
        }
        return null;
    }

    public static SelfDescribing getSelfDescribingEvent(ReadableMap event, ReadableArray contexts) {
        SelfDescribingJson data = EventUtil.getSelfDescribingJson(event);
        List<SelfDescribingJson> nativeContexts = EventUtil.getContexts(contexts);
        SelfDescribing.Builder eventBuilder = SelfDescribing.builder();
        if (data == null) return null;
        eventBuilder.eventData(data);
        if (nativeContexts != null) {
            eventBuilder.customContext(nativeContexts);
        }
        return eventBuilder.build();
    }

    public static Structured getStructuredEvent(String category, String action, String label,
                                                String property, Number value, ReadableArray contexts) {
        Structured.Builder eventBuilder = Structured.builder()
                .action(action)
                .category(category)
                .value(value.doubleValue())
                .property(property)
                .label(label);
        List<SelfDescribingJson> nativeContexts = EventUtil.getContexts(contexts);
        if (nativeContexts != null) {
            eventBuilder.customContext(nativeContexts);
        }
        return eventBuilder.build();
    }

    public static ScreenView getScreenViewEvent(String screenName, String screenId, String screenType,
                                                String previousScreenName, String previousScreenType, String previousScreenId,
                                                String transitionType, ReadableArray contexts) {
        ScreenView.Builder eventBuilder = ScreenView.builder()
                .name(screenName)
                .id(screenId)
                .type(screenType)
                .previousName(previousScreenName)
                .previousId(previousScreenId)
                .previousType(previousScreenType)
                .transitionType(transitionType);
        List<SelfDescribingJson> nativeContexts = EventUtil.getContexts(contexts);
        if (nativeContexts != null) {
            eventBuilder.customContext(nativeContexts);
        }
        return eventBuilder.build();
    }

    public static EcommerceTransaction getEcommerceTransactionEvent(String orderId, Double totalValue,
                                                                    String affiliation, Double taxValue, Double shipping, ReadableArray items, ReadableArray contexts) {
        EcommerceTransaction.Builder eventBuilder = EcommerceTransaction.builder()
                .affiliation(affiliation)
                .orderId(orderId)
                .totalValue(totalValue)
                .taxValue(taxValue)
                .shipping(shipping);
        List<EcommerceTransactionItem> transactionItems = EventUtil.getEcommerceTransactionItemList(items);
        if (transactionItems != null) {
            eventBuilder.items(transactionItems);
        }
        List<SelfDescribingJson> nativeContexts = EventUtil.getContexts(contexts);
        if (nativeContexts != null) {
            eventBuilder.customContext(nativeContexts);
        }
        return eventBuilder.build();
    }

    private static List<EcommerceTransactionItem> getEcommerceTransactionItemList(ReadableArray array) {
        ArrayList<EcommerceTransactionItem> list = new ArrayList<>();
        for (int i = 0; i< array.size() ; i++) {
            list.add(getEcommerceTransactionItem(array.getMap(i)));
        }
        return list;
    }

    private static EcommerceTransactionItem getEcommerceTransactionItem(ReadableMap json) {
        EcommerceTransactionItem.Builder builder = EcommerceTransactionItem.builder()
                .itemId(json.getString("itemId"))
                .sku(json.getString("sku"))
                .price(json.getDouble("price"))
                .quantity(json.getInt("quantity"))
                .currency(json.getString("currency"));
        return builder.build();
    }
}
