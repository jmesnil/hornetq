package org.hornetq.integration.aerogear;

import org.hornetq.api.core.SimpleString;

import java.util.HashSet;
import java.util.Set;

public class AeroGearConstants
{
   public static final Set<String> ALLOWABLE_PROPERTIES = new HashSet<>();
   public static final Set<String> REQUIRED_PROPERTIES = new HashSet<>();

   public static final String QUEUE_NAME = "queue";
   public static final String ENDPOINT_NAME = "endpoint";
   public static final String APPLICATION_ID_NAME = "application-id";
   public static final String APPLICATION_MASTER_SECRET_NAME = "master-secret";
   public static final String TTL_NAME = "ttl";
   public static final String BADGE_NAME = "badge";
   public static final String SOUND_NAME = "sound";
   public static final String FILTER_NAME = "filter";
   public static final String RETRY_INTERVAL_NAME = "retry-interval";
   public static final String RETRY_ATTEMPTS_NAME = "retry-attempts";
   public static final String VARIANTS_NAME = "variants";
   public static final String ALIASES_NAME = "aliases";
   public static final String DEVICE_TYPE_NAME = "device-types";


   public static final SimpleString AEROGEAR_ALERT = new SimpleString("AEROGEAR_ALERT");
   public static final SimpleString AEROGEAR_SOUND = new SimpleString("AEROGEAR_SOUND");
   public static final SimpleString AEROGEAR_BADGE = new SimpleString("AEROGEAR_BADGE");
   public static final SimpleString AEROGEAR_TTL = new SimpleString("AEROGEAR_TTL");
   public static final SimpleString AEROGEAR_VARIANTS = new SimpleString("AEROGEAR_VARIANTS");
   public static final SimpleString AEROGEAR_ALIASES = new SimpleString("AEROGEAR_ALIASES");
   public static final SimpleString AEROGEAR_DEVICE_TYPES = new SimpleString("AEROGEAR_DEVICE_TYPES");

   public static final String DEFAULT_SOUND = "default";
   public static final Integer DEFAULT_TTL = 3600;
   public static final int DEFAULT_RETRY_INTERVAL = 5;
   public static final int DEFAULT_RETRY_ATTEMPTS = 5;

   static
   {
      ALLOWABLE_PROPERTIES.add(QUEUE_NAME);
      ALLOWABLE_PROPERTIES.add(ENDPOINT_NAME);
      ALLOWABLE_PROPERTIES.add(APPLICATION_ID_NAME);
      ALLOWABLE_PROPERTIES.add(APPLICATION_MASTER_SECRET_NAME);
      ALLOWABLE_PROPERTIES.add(TTL_NAME);
      ALLOWABLE_PROPERTIES.add(BADGE_NAME);
      ALLOWABLE_PROPERTIES.add(SOUND_NAME);
      ALLOWABLE_PROPERTIES.add(FILTER_NAME);
      ALLOWABLE_PROPERTIES.add(RETRY_INTERVAL_NAME);
      ALLOWABLE_PROPERTIES.add(RETRY_ATTEMPTS_NAME);
      ALLOWABLE_PROPERTIES.add(VARIANTS_NAME);
      ALLOWABLE_PROPERTIES.add(ALIASES_NAME);
      ALLOWABLE_PROPERTIES.add(DEVICE_TYPE_NAME);

      REQUIRED_PROPERTIES.add(QUEUE_NAME);
      REQUIRED_PROPERTIES.add(ENDPOINT_NAME);
      REQUIRED_PROPERTIES.add(APPLICATION_ID_NAME);
      REQUIRED_PROPERTIES.add(APPLICATION_MASTER_SECRET_NAME);
   }

}
