#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TEMPLATE_DIR="$ROOT/kustomize/templates"
DEFAULT_TEMPLATE="$TEMPLATE_DIR/overlay.yaml.tpl"

USER_ROLE_URL="USER"
DEVELOPER_ROLE_URL="DEVELOPER"

extra_user_orgs_for_namespace() {
  local namespace="$1"
  case "$namespace" in
    afk-no|bfk-no|ofk-no)
      printf 'viken.no frid-iks.no'
      ;;
    *)
      printf ''
      ;;
  esac
}

extra_resources_for_overlay() {
  local namespace="$1"
  local env_path="$2"
  case "${namespace}:${env_path}" in
    afk-no:*|ofk-no:*)
      printf 'acos-oauth2-client.yaml isygraving-oauth2-client.yaml'
      ;;
    bfk-no:*)
      printf 'acos-oauth2-client.yaml fskyss-oauth2-client.yaml isygraving-oauth2-client.yaml'
      ;;
    mrfylke-no:api|telemarkfylke-no:*|vestfoldfylke-no:*)
      printf 'isygraving-oauth2-client.yaml'
      ;;
    nfk-no:api)
      printf 'side-oauth2-client.yaml'
      ;;
    fintlabs-no:beta)
      printf 'digisak-oauth2-client.yaml side-oauth2-client.yaml'
      ;;
    vlfk-no:beta)
      printf 'digisak-oauth2-client.yaml'
      ;;
    *)
      printf ''
      ;;
  esac
}

extra_env_for_overlay() {
  local namespace="$1"
  local env_path="$2"
  case "${namespace}:${env_path}" in
    afk-no:*|ofk-no:*)
      printf 'fint.flyt.acos.available fint.flyt.isygraving.available'
      ;;
    bfk-no:*)
      printf 'fint.flyt.acos.available fint.flyt.isygraving.available fint.flyt.fskyss.available'
      ;;
    mrfylke-no:api|telemarkfylke-no:*|vestfoldfylke-no:*)
      printf 'fint.flyt.isygraving.available'
      ;;
    nfk-no:api)
      printf 'fint.flyt.side.available'
      ;;
    fintlabs-no:beta)
      printf 'fint.flyt.digisak.available fint.flyt.side.available'
      ;;
    vlfk-no:beta)
      printf 'fint.flyt.digisak.available'
      ;;
    *)
      printf ''
      ;;
  esac
}

extra_env_from_for_overlay() {
  local namespace="$1"
  local env_path="$2"
  case "${namespace}:${env_path}" in
    afk-no:*|ofk-no:*)
      printf 'fint-flyt-acos-oauth2-client fint-flyt-isygraving-oauth2-client'
      ;;
    bfk-no:*)
      printf 'fint-flyt-acos-oauth2-client fint-flyt-isygraving-oauth2-client fint-flyt-fskyss-oauth2-client'
      ;;
    mrfylke-no:api|telemarkfylke-no:*|vestfoldfylke-no:*)
      printf 'fint-flyt-isygraving-oauth2-client'
      ;;
    nfk-no:api)
      printf 'fint-flyt-side-oauth2-client'
      ;;
    fintlabs-no:beta)
      printf 'fint-flyt-digisak-oauth2-client fint-flyt-side-oauth2-client'
      ;;
    vlfk-no:beta)
      printf 'fint-flyt-digisak-oauth2-client'
      ;;
    *)
      printf ''
      ;;
  esac
}

onepassword_item_path_for_overlay() {
  local namespace="$1"
  local env_path="$2"
  case "${namespace}:${env_path}" in
    *:beta)
      printf 'vaults/aks-beta-vault/items/fint-flyt-egrunnerverv-oauth2-client'
      ;;
    *)
      printf ''
      ;;
  esac
}

render_authorized_role_pairs() {
  local org_id="$1"
  shift

  local entries=("\"${org_id}\":[\"${USER_ROLE_URL}\"]")
  for extra_org in "$@"; do
    entries+=("\"${extra_org}\":[\"${USER_ROLE_URL}\"]")
  done
  entries+=("\"vigo.no\":[\"${DEVELOPER_ROLE_URL}\",\"${USER_ROLE_URL}\"]")
  entries+=("\"novari.no\":[\"${DEVELOPER_ROLE_URL}\",\"${USER_ROLE_URL}\"]")

  local total="${#entries[@]}"
  printf '            {\n'
  for idx in "${!entries[@]}"; do
    local comma=","
    if [[ "$idx" == "$((total - 1))" ]]; then
      comma=""
    fi
    printf '              %s%s\n' "${entries[$idx]}" "$comma"
  done
  printf '            }\n'
}

choose_template() {
  local env_path="$1"
  if [[ -z "$env_path" ]]; then
    printf '%s' "$DEFAULT_TEMPLATE"
    return
  fi

  local candidate="overlay-${env_path//\//-}.yaml.tpl"
  local candidate_path="$TEMPLATE_DIR/$candidate"

  if [[ -f "$candidate_path" ]]; then
    printf '%s' "$candidate_path"
  else
    printf '%s' "$DEFAULT_TEMPLATE"
  fi
}

while IFS= read -r file; do
  rel="${file#"$ROOT/kustomize/overlays/"}"
  dir="$(dirname "$rel")"

  namespace="${dir%%/*}"
  env_path="${dir#*/}"
  if [[ "$env_path" == "$namespace" ]]; then
    env_path=""
  fi

  path_prefix="/$namespace"
  if [[ -n "$env_path" && "$env_path" != "api" ]]; then
    path_prefix="/${env_path}/$namespace"
  fi

  declare -a additional_user_orgs=()
  extra_orgs="$(extra_user_orgs_for_namespace "$namespace")"
  if [[ -n "$extra_orgs" ]]; then
    for extra_org in $extra_orgs; do
      additional_user_orgs+=("$extra_org")
    done
  fi

  export NAMESPACE="$namespace"
  export FINT_KAFKA_TOPIC_ORG_ID="$namespace"
  export ORG_ID="${namespace//-/.}"
  export APP_INSTANCE_LABEL="fint-flyt-authorization-service_${namespace//-/_}"
  export KAFKA_TOPIC="${namespace}.flyt.*"
  export URL_BASE_PATH="$path_prefix"
  export INGRESS_BASE_PATH="${path_prefix}/api/intern/authorization"
  export STARTUP_PATH="${path_prefix}/actuator/health"
  export READINESS_PATH="${path_prefix}/actuator/health/readiness"
  export LIVENESS_PATH="${path_prefix}/actuator/health/liveness"
  export METRICS_PATH="${path_prefix}/actuator/prometheus"
  extra_resources="$(extra_resources_for_overlay "$namespace" "$env_path")"
  EXTRA_RESOURCES=""
  if [[ -n "$extra_resources" ]]; then
    for resource in $extra_resources; do
      EXTRA_RESOURCES+=$'\n  - '"${resource}"
    done
  fi
  export EXTRA_RESOURCES

  extra_env="$(extra_env_for_overlay "$namespace" "$env_path")"
  EXTRA_ENV_PATCHES=""
  if [[ -n "$extra_env" ]]; then
    for env_name in $extra_env; do
      EXTRA_ENV_PATCHES+=$'      - op: add\n'
      EXTRA_ENV_PATCHES+=$'        path: "/spec/env/-"\n'
      EXTRA_ENV_PATCHES+=$'        value:\n'
      EXTRA_ENV_PATCHES+=$'          name: "'"${env_name}"$'"\n'
      EXTRA_ENV_PATCHES+=$'          value: "true"\n'
    done
    EXTRA_ENV_PATCHES="${EXTRA_ENV_PATCHES%$'\n'}"
  fi

  extra_env_from="$(extra_env_from_for_overlay "$namespace" "$env_path")"
  EXTRA_ENV_FROM_PATCHES=""
  if [[ -n "$extra_env_from" ]]; then
    idx=0
    for secret in $extra_env_from; do
      EXTRA_ENV_FROM_PATCHES+=$'      - op: add\n'
      EXTRA_ENV_FROM_PATCHES+=$'        path: "/spec/envFrom/'"${idx}"$'"\n'
      EXTRA_ENV_FROM_PATCHES+=$'        value:\n'
      EXTRA_ENV_FROM_PATCHES+=$'          secretRef:\n'
      EXTRA_ENV_FROM_PATCHES+=$'            name: '"${secret}"$'\n'
      idx=$((idx + 1))
    done
    EXTRA_ENV_FROM_PATCHES="${EXTRA_ENV_FROM_PATCHES%$'\n'}"
  fi

  EXTRA_APP_PATCHES=""
  if [[ -n "$EXTRA_ENV_PATCHES" ]]; then
    EXTRA_APP_PATCHES+=$'\n'"$EXTRA_ENV_PATCHES"
  fi
  if [[ -n "$EXTRA_ENV_FROM_PATCHES" ]]; then
    EXTRA_APP_PATCHES+=$'\n'"$EXTRA_ENV_FROM_PATCHES"
  fi
  if [[ -n "$EXTRA_APP_PATCHES" ]]; then
    EXTRA_APP_PATCHES+=$'\n'
  fi
  export EXTRA_APP_PATCHES

  onepassword_item_path="$(onepassword_item_path_for_overlay "$namespace" "$env_path")"
  EXTRA_PATCHES=""
  if [[ -n "$onepassword_item_path" ]]; then
    EXTRA_PATCHES+=$'\n'
    EXTRA_PATCHES+=$'  - patch: |-\n'
    EXTRA_PATCHES+=$'      - op: replace\n'
    EXTRA_PATCHES+=$'        path: "/spec/itemPath"\n'
    EXTRA_PATCHES+=$'        value: "'"${onepassword_item_path}"$'"\n'
    EXTRA_PATCHES+=$'    target:\n'
    EXTRA_PATCHES+=$'      kind: OnePasswordItem\n'
    EXTRA_PATCHES+=$'      name: fint-flyt-egrunnerverv-oauth2-client\n'
    EXTRA_PATCHES="${EXTRA_PATCHES%$'\n'}"
  fi
  export EXTRA_PATCHES

  if ((${#additional_user_orgs[@]})); then
    AUTHORIZED_ORG_ROLE_PAIRS="$(render_authorized_role_pairs "$ORG_ID" "${additional_user_orgs[@]}")"
  else
    AUTHORIZED_ORG_ROLE_PAIRS="$(render_authorized_role_pairs "$ORG_ID")"
  fi
  export AUTHORIZED_ORG_ROLE_PAIRS

  template="$(choose_template "$env_path")"
  target_dir="$ROOT/kustomize/overlays/$dir"

  tmp="$(mktemp "$target_dir/.kustomization.yaml.XXXXXX")"
  envsubst '$NAMESPACE $FINT_KAFKA_TOPIC_ORG_ID $APP_INSTANCE_LABEL $ORG_ID $KAFKA_TOPIC $URL_BASE_PATH $INGRESS_BASE_PATH $STARTUP_PATH $READINESS_PATH $LIVENESS_PATH $METRICS_PATH $AUTHORIZED_ORG_ROLE_PAIRS $EXTRA_RESOURCES $EXTRA_APP_PATCHES $EXTRA_PATCHES' \
    < "$template" > "$tmp"
  mv "$tmp" "$target_dir/kustomization.yaml"
done < <(find "$ROOT/kustomize/overlays" -name kustomization.yaml -print | sort)
