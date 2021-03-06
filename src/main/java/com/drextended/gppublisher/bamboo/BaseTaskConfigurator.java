/*
 *  Copyright Roman Donchenko. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.drextended.gppublisher.bamboo;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.drextended.gppublisher.bamboo.util.AndroidPublisherHelper.*;

/**
 * Created by Romka on 06.01.2017.
 */
public class BaseTaskConfigurator extends AbstractTaskConfigurator {

    public static final String APPLICATION_NAME = "applicationName";
    public static final String PACKAGE_NAME = "packageName";
    public static final String JSON_KEY_PATH = "jsonKeyPath";
    public static final String JSON_KEY_CONTENT = "jsonKeyContent";
    public static final String FIND_JSON_KEY_IN_FILE = "findJsonKeyInFile";
    public static final String APK_PATH = "apkPath";
    public static final String DEOBFUSCATION_FILE_PATH = "deobfuscationFilePath";
    public static final String RECENT_CHANGES_LISTINGS = "recentChangesListings";
//    public static final String APK_ARTIFACT = "apkArtifact";
//    public static final String APK_ARTIFACT_LIST = "apkArtifactList";
    public static final String TRACK = "track";
    public static final String TRACK_TYPES = "trackTypes";

    public static final String ROLLOUT_FRACTION = "rolloutFraction";
    public static final String ROLLOUT_FRACTIONS = "rolloutFractions";
    public static final String ROLLOUT_FRACTION_DEFAULT = "0.1"; // Acceptable values are 0.05, 0.1, 0.2, and 0.5
    public static final Map<String, String> ROLLOUT_FRACTION_MAP = ImmutableMap.<String, String>builder()
            .put("0.05", "0.05")
            .put("0.1", "0.1")
            .put("0.2", "0.2")
            .put("0.5", "0.5")
            .build();
    private static final Map<String, String> TRACK_MAP = ImmutableMap.<String, String>builder()
            .put(TRACK_NONE, TRACK_NONE)
            .put(TRACK_INTERNAL, TRACK_INTERNAL)
            .put(TRACK_ALPHA, TRACK_ALPHA)
            .put(TRACK_BETA, TRACK_BETA)
            .put(TRACK_PRODUCTION, TRACK_PRODUCTION)
            .put(TRACK_ROLLOUT, TRACK_ROLLOUT)
            .build();

    public static final String DEFAULT_TRACK = TRACK_INTERNAL;

    public BaseTaskConfigurator() {
    }

    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        config.put(APPLICATION_NAME, params.getString(APPLICATION_NAME));
        config.put(PACKAGE_NAME, params.getString(PACKAGE_NAME));
        config.put(FIND_JSON_KEY_IN_FILE, params.getString(FIND_JSON_KEY_IN_FILE));
        config.put(JSON_KEY_PATH, params.getString(JSON_KEY_PATH));
        config.put(JSON_KEY_CONTENT, params.getString(JSON_KEY_CONTENT));
        config.put(APK_PATH, params.getString(APK_PATH));
        config.put(DEOBFUSCATION_FILE_PATH, params.getString(DEOBFUSCATION_FILE_PATH));
        config.put(RECENT_CHANGES_LISTINGS, params.getString(RECENT_CHANGES_LISTINGS));
        config.put(TRACK, params.getString(TRACK));
        config.put(ROLLOUT_FRACTION, params.getString(ROLLOUT_FRACTION));
        return config;
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context) {
        super.populateContextForCreate(context);
        context.put(TRACK_TYPES, TRACK_MAP);
        context.put(TRACK, DEFAULT_TRACK);
        context.put(FIND_JSON_KEY_IN_FILE, false);
        context.put(ROLLOUT_FRACTION, ROLLOUT_FRACTION_DEFAULT);
        context.put(ROLLOUT_FRACTIONS, ROLLOUT_FRACTION_MAP);
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);
        context.put(APPLICATION_NAME, taskDefinition.getConfiguration().get(APPLICATION_NAME));
        context.put(PACKAGE_NAME, taskDefinition.getConfiguration().get(PACKAGE_NAME));
        context.put(FIND_JSON_KEY_IN_FILE, taskDefinition.getConfiguration().get(FIND_JSON_KEY_IN_FILE));
        context.put(JSON_KEY_CONTENT, taskDefinition.getConfiguration().get(JSON_KEY_CONTENT));
        context.put(JSON_KEY_PATH, taskDefinition.getConfiguration().get(JSON_KEY_PATH));
        context.put(APK_PATH, taskDefinition.getConfiguration().get(APK_PATH));
        context.put(DEOBFUSCATION_FILE_PATH, taskDefinition.getConfiguration().get(DEOBFUSCATION_FILE_PATH));
        context.put(RECENT_CHANGES_LISTINGS, taskDefinition.getConfiguration().get(RECENT_CHANGES_LISTINGS));
        context.put(TRACK_TYPES, TRACK_MAP);
        context.put(TRACK, taskDefinition.getConfiguration().get(TRACK));
        String fraction = taskDefinition.getConfiguration().get(ROLLOUT_FRACTION);
        context.put(ROLLOUT_FRACTION, fraction != null ? fraction : ROLLOUT_FRACTION_DEFAULT);
        context.put(ROLLOUT_FRACTIONS, ROLLOUT_FRACTION_MAP);
    }

    @Override
    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection) {
        super.validate(params, errorCollection);

        validateNotEmpty(params, errorCollection, APPLICATION_NAME);
        validateNotEmpty(params, errorCollection, PACKAGE_NAME);
        if (params.getBoolean(FIND_JSON_KEY_IN_FILE)) {
            validateNotEmpty(params, errorCollection, JSON_KEY_PATH);
        } else {
            validateNotEmpty(params, errorCollection, JSON_KEY_CONTENT);
        }
        validateNotEmpty(params, errorCollection, APK_PATH);
        validateNotEmpty(params, errorCollection, TRACK);
        if (TRACK_ROLLOUT.equals(params.getString(TRACK))) {
            validateNotEmpty(params, errorCollection, ROLLOUT_FRACTION);
        }
    }

    private void validateNotEmpty(@NotNull ActionParametersMap params, @NotNull final ErrorCollection errorCollection, @NotNull String key) {
        final String value = params.getString(key);
        if (StringUtils.isEmpty(value)) {
            errorCollection.addError(key, "This field can't be empty");
        } else if (APK_PATH.equals(key) && !value.endsWith(".apk")) {
            errorCollection.addError(key, "Should be path to *.apk file");
        }
    }

}
