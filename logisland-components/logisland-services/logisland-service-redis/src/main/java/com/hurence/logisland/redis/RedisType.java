/**
 * Copyright (C) 2016 Hurence (support@hurence.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hurence.logisland.redis;

/**
 * Possible types of Redis instances.
 */
public enum RedisType {

    STANDALONE("standalone", "A single standalone Redis instance."),

    SENTINEL("sentinel", "Redis Sentinel which provides high-availability. Described further at https://redis.io/topics/sentinel"),

    CLUSTER("cluster", "Clustered Redis which provides sharding and replication. Described further at https://redis.io/topics/cluster-spec");

    private final String displayName;
    private final String description;

    RedisType(final String displayName, final String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static RedisType fromDisplayName(final String displayName) {
        for (RedisType redisType : values()) {
            if (redisType.getDisplayName().equals(displayName)) {
                return redisType;
            }
        }

        throw new IllegalArgumentException("Unknown RedisType: " + displayName);
    }

}
