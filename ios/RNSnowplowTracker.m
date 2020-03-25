#import "RNSnowplowTracker.h"
#import <React/RCTConvert.h>
#import <SnowplowTracker/SPTracker.h>
#import <SnowplowTracker/SPEmitter.h>
#import <SnowplowTracker/SPEvent.h>
#import <SnowplowTracker/SPSelfDescribingJson.h>
#import <SnowplowTracker/SPSubject.h>

@implementation RNSnowplowTracker

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(initialize
                  :(nonnull NSString *)endpoint
                  :(nonnull NSString *)method
                  :(nonnull NSString *)protocol
                  :(nonnull NSString *)namespace
                  :(nonnull NSString *)appId
                  :(NSDictionary *)options
                  :(RCTResponseSenderBlock)callback
                ) {
    SPSubject *subject = [[SPSubject alloc] initWithPlatformContext:YES andGeoContext:NO];

    SPEmitter *emitter = [SPEmitter build:^(id<SPEmitterBuilder> builder) {
        [builder setUrlEndpoint:endpoint];
        [builder setHttpMethod:([@"post" caseInsensitiveCompare:method] == NSOrderedSame) ? SPRequestPost : SPRequestGet];
        [builder setProtocol:([@"https" caseInsensitiveCompare:protocol] == NSOrderedSame) ? SPHttps : SPHttp];
    }];
    
    self.tracker = [SPTracker build:^(id<SPTrackerBuilder> builder) {
        [builder setEmitter:emitter];
        [builder setAppId:appId];
        [builder setTrackerNamespace:namespace];
        if (options[@"setSessionContext"] != nil) {
            [builder setSessionContext:options[@"setSessionContext"]];
        }
        if (options[@"setApplicationContext"] != nil) {
            [builder setApplicationContext:options[@"setApplicationContext"]];
        }
        if (options[@"foregroundTimeout"] != nil) {
            [builder setForegroundTimeout:[RCTConvert NSInteger: options[@"foregroundTimeout"]]];
        }
        if (options[@"backgroundTimeout"] != nil) {
            [builder setBackgroundTimeout:[RCTConvert NSInteger: options[@"backgroundTimeout"]]];
        }
        [builder setSubject:subject];
    }];

    callback(@[[NSNull null], @true]);
}

RCT_EXPORT_METHOD(trackSelfDescribingEvent
                  :(nonnull SPSelfDescribingJson *)event
                  :(NSArray<SPSelfDescribingJson *> *)contexts
                  :(RCTResponseSenderBlock)callback) {
    SPUnstructured * unstructEvent = [SPUnstructured build:^(id<SPUnstructuredBuilder> builder) {
        [builder setEventData:event];
        if (contexts) {
            [builder setContexts:[[NSMutableArray alloc] initWithArray:contexts]];
        }
    }];
    [self.tracker trackUnstructuredEvent:unstructEvent];
    callback(@[[NSNull null], @true]);
}

RCT_EXPORT_METHOD(setSubjectUserId
                  :(NSString *)userId
                  :(RCTResponseSenderBlock)callback) {
    [[self.tracker subject] setUserId:userId];

    callback(@[[NSNull null], @true]);
}

RCT_EXPORT_METHOD(setSubjectColorDepth
                  :(NSInteger *)colorDepth
                  :(RCTResponseSenderBlock)callback) {
    [[self.tracker subject] setColorDepth:*colorDepth];

    callback(@[[NSNull null], @true]);
}

RCT_EXPORT_METHOD(setSubjectTimezone
                  :(NSString *)timezone
                  :(RCTResponseSenderBlock)callback) {
    [[self.tracker subject] setTimezone:timezone];

    callback(@[[NSNull null], @true]);
}

RCT_EXPORT_METHOD(setSubjectLanguage
                  :(NSString *)language
                  :(RCTResponseSenderBlock)callback) {
    [[self.tracker subject] setLanguage:language];

    callback(@[[NSNull null], @true]);
}

RCT_EXPORT_METHOD(setSubjectIpAddress
                  :(NSString *)ipAddress
                  :(RCTResponseSenderBlock)callback) {
    [[self.tracker subject] setIpAddress:ipAddress];

    callback(@[[NSNull null], @true]);
}

RCT_EXPORT_METHOD(setSubjectUseragent
                  :(NSString *)userAgent
                  :(RCTResponseSenderBlock)callback) {
    [[self.tracker subject] setUseragent:userAgent];

    callback(@[[NSNull null], @true]);
}

RCT_EXPORT_METHOD(setSubjectNetworkUserId
                  :(NSString *)networkUserId
                  :(RCTResponseSenderBlock)callback) {
    [[self.tracker subject] setNetworkUserId:networkUserId];

    callback(@[[NSNull null], @true]);
}


RCT_EXPORT_METHOD(trackStructuredEvent
                  :(nonnull NSString *)category // required (non-empty string)
                  :(nonnull NSString *)action // required
                  :(NSString *)label
                  :(NSString *)property
                  :(double)value
                  :(NSArray<SPSelfDescribingJson *> *)contexts
                  :(RCTResponseSenderBlock)callback) {
    SPStructured * trackerEvent = [SPStructured build:^(id<SPStructuredBuilder> builder) {
        [builder setCategory:category];
        [builder setAction:action];
        [builder setValue:value];
        if (label != nil) [builder setLabel:label];
        if (property != nil) [builder setProperty:property];
        if (contexts) {
            [builder setContexts:[[NSMutableArray alloc] initWithArray:contexts]];
        }
    }];
    [self.tracker trackStructuredEvent:trackerEvent];

    callback(@[[NSNull null], @true]);
}

RCT_EXPORT_METHOD(trackScreenViewEvent
                  :(nonnull NSString *)screenName
                  :(NSString *)screenId
                  :(NSString *)screenType
                  :(NSString *)previousScreenName
                  :(NSString *)previousScreenType
                  :(NSString *)previousScreenId
                  :(NSString *)transitionType
                  :(NSArray<SPSelfDescribingJson *> *)contexts
                  :(RCTResponseSenderBlock)callback) {
    SPScreenView * SVevent = [SPScreenView build:^(id<SPScreenViewBuilder> builder) {
        [builder setName:screenName];
        if (screenId != nil) [builder setScreenId:screenId]; else [builder setScreenId:[[NSUUID UUID] UUIDString]];
        if (screenType != nil) [builder setType:screenType];
        if (previousScreenName != nil) [builder setPreviousScreenName:previousScreenName];
        if (previousScreenType != nil) [builder setPreviousScreenType:previousScreenType];
        if (previousScreenId != nil) [builder setPreviousScreenId:previousScreenId];
        if (transitionType != nil) [builder setTransitionType:transitionType];
        if (contexts) {
            [builder setContexts:[[NSMutableArray alloc] initWithArray:contexts]];
        }
      }];
      [self.tracker trackScreenViewEvent:SVevent];
      callback(@[[NSNull null], @true]);
}

RCT_EXPORT_METHOD(trackEcommerceEvent
                 :(nonnull NSString *)orderId
                 :(nonnull NSString *)affiliation
                 :(double)total
                 :(double)tax
                 :(double)shipping
                 :(nonnull NSArray<NSDictionary *> *)items
                 :(NSArray<SPSelfDescribingJson *> *)contexts
                 :(RCTResponseSenderBlock)callback) {

   NSMutableArray *itemArray = [NSMutableArray array];

   for (NSDictionary *item in items) {
       NSNumber *price = [item valueForKey:@"price"];
       NSNumber *quantity = [item valueForKey:@"quantity"];
    
       [itemArray addObject:[SPEcommerceItem build:^(id<SPEcommTransactionItemBuilder> builder) {
         [builder setItemId: [item valueForKey:@"orderId"]];
         [builder setSku: [item valueForKey:@"sku"]];
         [builder setPrice: [price doubleValue]];
         [builder setQuantity: [quantity integerValue]];
       }]];
   }
   

   SPEcommerce *event = [SPEcommerce build:^(id<SPEcommTransactionBuilder> builder) {
    [builder setOrderId:orderId];
    [builder setTotalValue:total];
    [builder setAffiliation:affiliation];
    [builder setShipping:shipping];
    [builder setItems:itemArray];
    if (contexts) {
      [builder setContexts:[[NSMutableArray alloc] initWithArray:contexts]];
    }
   }];
   [self.tracker trackEcommerceEvent:event];
   callback(@[[NSNull null], @true]);
}
@end
