#!/bin/sh
# Exercises resolve-release-secrets.sh without real credentials.
set -eu

ROOT=$(CDPATH= cd -- "$(dirname "$0")" && pwd)
RESOLVE="$ROOT/resolve-release-secrets.sh"
TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

run_case() {
  name=$1
  expected_exit=$2
  expected_signing=$3
  expected_play=$4
  out="$TMP/out-$name"
  : > "$out"
  set +e
  GITHUB_OUTPUT="$out" \
    RELEASE_KEYSTORE_B64="$5" \
    STORE_PASSWORD="$6" \
    KEY_ALIAS="$7" \
    KEY_PASSWORD="$8" \
    PLAY_CONSOLE_JSON="$9" \
    sh "$RESOLVE" > "$TMP/log-$name" 2>&1
  status=$?
  set -e
  if [ "$status" -ne "$expected_exit" ]; then
    echo "---- log ----"
    cat "$TMP/log-$name"
    fail "$name: exit $status, expected $expected_exit"
  fi
  if [ "$expected_exit" -ne 0 ]; then
    return 0
  fi
  got_signing=$(sed -n 's/^signing=//p' "$out")
  got_play=$(sed -n 's/^play=//p' "$out")
  if [ "$got_signing" != "$expected_signing" ] || [ "$got_play" != "$expected_play" ]; then
    echo "---- output ----"
    cat "$out"
    fail "$name: signing=$got_signing play=$got_play"
  fi
}

BLANK=$(printf ' \n\t ')

# name exit signing play keystore store_pw alias key_pw play_json
run_case none 0 false false "" "" "" "" ""
run_case whitespace 0 false false "$BLANK" "" "" "" "$BLANK"
run_case signing_only 0 true false "a2V5c3RvcmU=" "store" "alias" "key" ""
run_case both 0 true true "a2V5c3RvcmU=" "store" "alias" "key" '{"type":"service_account"}'
run_case multiline_json 0 true true "a2V5c3RvcmU=" "store" "alias" "key" "$(printf '%s\n' '{' '"type": "service_account"' '}')"
run_case partial 1 "" "" "a2V5c3RvcmU=" "" "alias" "" ""
run_case play_without_signing 1 "" "" "" "" "" "" '{"type":"service_account"}'

# Secret-like values must not appear in stdout/stderr.
if grep -q 'service_account' "$TMP/log-both" "$TMP/log-multiline_json" "$TMP/log-play_without_signing"; then
  fail "secret material leaked into logs"
fi

echo "resolve-release-secrets: ok"
