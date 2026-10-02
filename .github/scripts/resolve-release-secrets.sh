#!/bin/sh
# Decide whether this master release can sign an AAB and upload it to Play.
# Reads RELEASE_KEYSTORE_B64, STORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD, and
# PLAY_CONSOLE_JSON from the environment. Writes signing=true|false and
# play=true|false to $GITHUB_OUTPUT. Never prints secret values.
set -eu

present() {
  # True when the value contains a non-whitespace character.
  # The value is only consumed by this pipeline; it is not echoed.
  printf '%s' "$1" | tr -d '[:space:]' | grep -q .
}

sign=0
if present "${RELEASE_KEYSTORE_B64-}"; then sign=$((sign + 1)); fi
if present "${STORE_PASSWORD-}"; then sign=$((sign + 1)); fi
if present "${KEY_ALIAS-}"; then sign=$((sign + 1)); fi
if present "${KEY_PASSWORD-}"; then sign=$((sign + 1)); fi

if present "${PLAY_CONSOLE_JSON-}"; then
  play=true
else
  play=false
fi

if [ "$sign" -eq 4 ]; then
  signing=true
elif [ "$sign" -eq 0 ]; then
  signing=false
else
  echo "::error::Release signing secrets are incomplete. Set all of RELEASE_KEYSTORE_B64, STORE_PASSWORD, KEY_ALIAS, and KEY_PASSWORD."
  exit 1
fi

if [ "$play" = "true" ] && [ "$signing" != "true" ]; then
  echo "::error::PLAY_CONSOLE_JSON is set, but the release signing secrets are missing. Google Play requires a signed AAB."
  exit 1
fi

if [ -z "${GITHUB_OUTPUT-}" ]; then
  echo "::error::GITHUB_OUTPUT is not set"
  exit 1
fi

{
  echo "signing=$signing"
  echo "play=$play"
} >> "$GITHUB_OUTPUT"

if [ "$play" = "true" ]; then
  echo "Play Console credentials detected; the signed AAB will be uploaded to the internal track."
else
  echo "PLAY_CONSOLE_JSON is not set; Google Play upload will be skipped."
fi
