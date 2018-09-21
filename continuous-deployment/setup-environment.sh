#!/usr/bin/env bash
openssl aes-256-cbc -K $encrypted_14d08a1c6cef_key -iv $encrypted_14d08a1c6cef_iv -in continuous-deployment/codesigning.asc.enc -out continuous-deployment/codesigning.asc -d
gpg -q --fast-import continuous-deployment/codesigning.asc

GRADLE_PROPERTIES=$HOME"/.gradle/gradle.properties"
echo "Gradle Properties should exist at $GRADLE_PROPERTIES"

if [ ! -f "$GRADLE_PROPERTIES" ]; then
    echo "Gradle Properties does not exist"
    echo "Creating Gradle Properties file..."
    touch $GRADLE_PROPERTIES
fi

echo "Writing secrets to gradle.properties..."
echo "ossrhUsername=$CI_DEPLOY_USERNAME" >> ${GRADLE_PROPERTIES}
echo "ossrhPassword=$CI_DEPLOY_PASSWORD" >> ${GRADLE_PROPERTIES}
echo "signing.keyId=$GPG_KEY_NAME" >> ${GRADLE_PROPERTIES}
echo "signing.password=$GPG_PASSPHRASE" >> ${GRADLE_PROPERTIES}
echo "signing.secretKeyRingFile=$HOME/.gnupg/secring.gpg" >> ${GRADLE_PROPERTIES}
