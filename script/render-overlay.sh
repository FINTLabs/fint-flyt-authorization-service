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

app_instance_suffix() {
  local namespace="$1"

  case "$namespace" in
    bym-oslo-kommune-no)
      printf '%s' "$namespace"
      ;;
    *)
      printf '%s' "${namespace//-/_}"
      ;;
  esac
}

authorized_org_id() {
  local namespace="$1"
  printf '%s' "${namespace//-/.}"
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
      printf 'side-oauth2-client.yaml eapply-onepassword.yaml'
      ;;
    vlfk-no:beta)
      printf 'eapply-onepassword.yaml'
      ;;
    ra-no:*)
      printf 'authorization-client-onepassword.yaml'
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
      printf 'fint.flyt.isygraving.available fint.flyt.side.available'
      ;;
    fintlabs-no:beta)
      printf 'fint.flyt.digisak.available fint.flyt.side.available fint.flyt.eapply.available'
      ;;
    vlfk-no:beta)
      printf 'fint.flyt.digisak.available fint.flyt.eapply.available'
      ;;
    *)
      printf ''
      ;;
  esac
}

extra_client_id_apps_for_overlay() {
  local namespace="$1"
  local env_path="$2"

  case "${namespace}:${env_path}" in
    afk-no:*|ofk-no:*)
      printf 'acos isygraving'
      ;;
    bfk-no:*)
      printf 'acos isygraving fskyss'
      ;;
    mrfylke-no:api|telemarkfylke-no:*|vestfoldfylke-no:*)
      printf 'isygraving'
      ;;
    nfk-no:api)
      printf 'isygraving side'
      ;;
    fintlabs-no:beta)
      printf 'side eapply'
      ;;
    vlfk-no:beta)
      printf 'eapply'
      ;;
    *)
      printf ''
      ;;
  esac
}

client_id_property_for_app() {
  local app="$1"
  printf 'fint.flyt.%s.sso.client-id' "$app"
}

oauth2_secret_name_for_app() {
  local app="$1"
  printf 'fint-flyt-%s-oauth2-client' "$app"
}

base_client_env_index_for_app() {
  local app="$1"

  case "$app" in
    vigo)
      printf '1'
      ;;
    altinn)
      printf '2'
      ;;
    egrunnerverv)
      printf '3'
      ;;
    hmsreg)
      printf '4'
      ;;
    authorization)
      printf '5'
      ;;
    *)
      return 1
      ;;
  esac
}

base_client_resource_kind_for_app() {
  local app="$1"

  case "$app" in
    vigo|hmsreg|authorization)
      printf 'NamOAuthClientApplicationResource'
      ;;
    altinn|egrunnerverv|eapply)
      printf 'OnePasswordItem'
      ;;
    *)
      return 1
      ;;
  esac
}

base_client_resource_api_version_for_app() {
  local app="$1"

  case "$app" in
    vigo|hmsreg|authorization)
      printf 'fintlabs.no/v1alpha1'
      ;;
    altinn|egrunnerverv|eapply)
      printf 'onepassword.com/v1'
      ;;
    *)
      return 1
      ;;
  esac
}

excluded_base_client_id_apps_for_overlay() {
  local namespace="$1"
  local env_path="$2"

  case "${namespace}:${env_path}" in
    ra-no:*)
      # ra-no keeps only authorization.
      # Descending env index order is required:
      # hmsreg=4, egrunnerverv=3, altinn=2, vigo=1
      printf 'hmsreg egrunnerverv altinn vigo'
      ;;

    bym-oslo-kommune-no:*)
      # bym-oslo-kommune-no keeps only altinn.
      # Descending env index order:
      # authorization=5, hmsreg=4, egrunnerverv=3, vigo=1
      printf 'authorization hmsreg egrunnerverv vigo'
      ;;

    *)
      printf ''
      ;;
  esac
}

authorization_credentials_from_onepassword_for_overlay() {
  local namespace="$1"
  local env_path="$2"

  case "${namespace}:${env_path}" in
    ra-no:*)
      return 0
      ;;
    *)
      return 1
      ;;
  esac
}

base_onepassword_item_names() {
  while IFS= read -r file; do
    sed -n 's/^  name: //p' "$file"
  done < <(
    find "$ROOT/kustomize/base" \
      -maxdepth 1 \
      -name '*-onepassword.yaml' \
      -print |
      sort
  )
}

onepassword_item_names_for_overlay() {
  local namespace="$1"
  local env_path="$2"

  case "${namespace}:${env_path}" in
    *:api)
      printf ''
      ;;
    *:beta)
      base_onepassword_item_names |
        sed '/^fint-flyt-eapply-oauth2-client$/d'
      ;;
    *)
      printf ''
      ;;
  esac
}

is_excluded_base_client_resource() {
  local item_name="$1"
  local excluded_apps="$2"

  if [[ -z "$excluded_apps" ]]; then
    return 1
  fi

  local app

  for app in $excluded_apps; do
    if [[ "$item_name" == "$(oauth2_secret_name_for_app "$app")" ]]; then
      return 0
    fi
  done

  return 1
}

render_authorized_role_pairs() {
  local org_id="$1"
  shift

  local entries=("\"${org_id}\":[\"${USER_ROLE_URL}\"]")

  local extra_org

  for extra_org in "$@"; do
    entries+=("\"${extra_org}\":[\"${USER_ROLE_URL}\"]")
  done

  entries+=("\"vigo.no\":[\"${DEVELOPER_ROLE_URL}\",\"${USER_ROLE_URL}\"]")
  entries+=("\"novari.no\":[\"${DEVELOPER_ROLE_URL}\",\"${USER_ROLE_URL}\"]")

  local total="${#entries[@]}"

  printf '            {\n'

  local idx

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

  case "$env_path" in
    api)
      vault_name="aks-api-vault"
      ;;
    beta)
      vault_name="aks-beta-vault"
      ;;
    *)
      vault_name="aks-api-vault"
      ;;
  esac

  declare -a additional_user_orgs=()

  extra_orgs="$(extra_user_orgs_for_namespace "$namespace")"

  if [[ -n "$extra_orgs" ]]; then
    for extra_org in $extra_orgs; do
      additional_user_orgs+=("$extra_org")
    done
  fi

  export NAMESPACE="$namespace"
  export ORG_ID="${namespace//-/.}"
  export APP_INSTANCE_LABEL="fint-flyt-authorization-service_$(app_instance_suffix "$namespace")"
  export KAFKA_TOPIC="${namespace}.flyt.*"
  export INGRESS_BASE_PATH="${path_prefix}/api/intern/authorization"
  export SERVLET_CONTEXT_PATH="$path_prefix"
  export STARTUP_PATH="${path_prefix}/actuator/health"
  export READINESS_PATH="${path_prefix}/actuator/health/readiness"
  export LIVENESS_PATH="${path_prefix}/actuator/health/liveness"
  export METRICS_PATH="${path_prefix}/actuator/prometheus"
  export NOVARI_KAFKA_TOPIC_ORGID="$namespace"

  #
  # Extra resources
  #

  extra_resources="$(
    extra_resources_for_overlay "$namespace" "$env_path"
  )"

  EXTRA_RESOURCES=""

  if [[ -n "$extra_resources" ]]; then
    for resource in $extra_resources; do
      EXTRA_RESOURCES+=$'\n  - '"${resource}"
    done
  fi

  export EXTRA_RESOURCES

  #
  # Extra boolean env vars
  #

  extra_env="$(
    extra_env_for_overlay "$namespace" "$env_path"
  )"

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

  #
  # Extra client-id env vars
  #

  extra_client_id_apps="$(
    extra_client_id_apps_for_overlay "$namespace" "$env_path"
  )"

  EXTRA_CLIENT_ID_ENV_PATCHES=""

  if [[ -n "$extra_client_id_apps" ]]; then
    for app in $extra_client_id_apps; do
      client_id_property="$(client_id_property_for_app "$app")"
      secret_name="$(oauth2_secret_name_for_app "$app")"

      EXTRA_CLIENT_ID_ENV_PATCHES+=$'      - op: add\n'
      EXTRA_CLIENT_ID_ENV_PATCHES+=$'        path: "/spec/env/-"\n'
      EXTRA_CLIENT_ID_ENV_PATCHES+=$'        value:\n'
      EXTRA_CLIENT_ID_ENV_PATCHES+=$'          name: "'"${client_id_property}"$'"\n'
      EXTRA_CLIENT_ID_ENV_PATCHES+=$'          valueFrom:\n'
      EXTRA_CLIENT_ID_ENV_PATCHES+=$'            secretKeyRef:\n'
      EXTRA_CLIENT_ID_ENV_PATCHES+=$'              name: '"${secret_name}"$'\n'
      EXTRA_CLIENT_ID_ENV_PATCHES+=$'              key: '"${client_id_property}"$'\n'
    done

    EXTRA_CLIENT_ID_ENV_PATCHES="${EXTRA_CLIENT_ID_ENV_PATCHES%$'\n'}"
  fi

  EXTRA_APP_PATCHES=""

  if [[ -n "$EXTRA_ENV_PATCHES" ]]; then
    EXTRA_APP_PATCHES+=$'\n'"$EXTRA_ENV_PATCHES"
  fi

  if [[ -n "$EXTRA_CLIENT_ID_ENV_PATCHES" ]]; then
    EXTRA_APP_PATCHES+=$'\n'"$EXTRA_CLIENT_ID_ENV_PATCHES"
  fi

  if [[ -n "$EXTRA_APP_PATCHES" ]]; then
    EXTRA_APP_PATCHES+=$'\n'
  fi

  export EXTRA_APP_PATCHES

  #
  # Determine excluded base clients
  #

  excluded_base_client_id_apps="$(
    excluded_base_client_id_apps_for_overlay "$namespace" "$env_path"
  )"

  #
  # Find base OnePassword items
  #

  declare -a onepassword_item_names=()

  while IFS= read -r item_name; do
    if [[ -n "$item_name" ]]; then
      onepassword_item_names+=("$item_name")
    fi
  done < <(
    onepassword_item_names_for_overlay "$namespace" "$env_path"
  )

  #
  # Build extra patches
  #

  EXTRA_PATCHES=""

  #
  # ra-no:
  #
  # The base already contains:
  #
  #   name: fint.flyt.authorization.sso.client-id
  #   secretKeyRef:
  #     name: fint-flyt-authorization-oauth2-client
  #     key: fint.sso.client-id
  #
  # Replace the Secret key with the actual OnePassword field name:
  #
  #   fint.flyt.authorization.sso.client-id
  #
  # Then add the client-secret from the same Secret:
  #
  #   fint.flyt.authorization.sso.client-secret
  #
  # This patch must run before removing env indexes because
  # authorization is index 5 in the base.
  #

  if authorization_credentials_from_onepassword_for_overlay \
    "$namespace" \
    "$env_path"; then

    EXTRA_PATCHES+=$'\n'
    EXTRA_PATCHES+=$'  - patch: |-\n'

    EXTRA_PATCHES+=$'      - op: replace\n'
    EXTRA_PATCHES+=$'        path: "/spec/env/5/valueFrom/secretKeyRef/key"\n'
    EXTRA_PATCHES+=$'        value: "fint.flyt.authorization.sso.client-id"\n'

    EXTRA_PATCHES+=$'      - op: add\n'
    EXTRA_PATCHES+=$'        path: "/spec/env/-"\n'
    EXTRA_PATCHES+=$'        value:\n'
    EXTRA_PATCHES+=$'          name: "fint.flyt.authorization.sso.client-secret"\n'
    EXTRA_PATCHES+=$'          valueFrom:\n'
    EXTRA_PATCHES+=$'            secretKeyRef:\n'
    EXTRA_PATCHES+=$'              name: fint-flyt-authorization-oauth2-client\n'
    EXTRA_PATCHES+=$'              key: fint.flyt.authorization.sso.client-secret\n'

    EXTRA_PATCHES+=$'    target:\n'
    EXTRA_PATCHES+=$'      kind: Application\n'
    EXTRA_PATCHES+=$'      name: fint-flyt-authorization-service\n'
  fi

  #
  # Remove env entries belonging to excluded clients.
  #
  # Apps must be returned in descending index order.
  #

  if [[ -n "$excluded_base_client_id_apps" ]]; then
    EXTRA_PATCHES+=$'\n'
    EXTRA_PATCHES+=$'  - patch: |-\n'

    for app in $excluded_base_client_id_apps; do
      env_index="$(base_client_env_index_for_app "$app")"

      EXTRA_PATCHES+=$'      - op: remove\n'
      EXTRA_PATCHES+=$'        path: "/spec/env/'"${env_index}"$'"\n'
    done

    EXTRA_PATCHES+=$'    target:\n'
    EXTRA_PATCHES+=$'      kind: Application\n'
    EXTRA_PATCHES+=$'      name: fint-flyt-authorization-service\n'
  fi

  #
  # Delete excluded base resources
  #

  if [[ -n "$excluded_base_client_id_apps" ]]; then
    for app in $excluded_base_client_id_apps; do
      resource_kind="$(base_client_resource_kind_for_app "$app")"
      resource_api_version="$(base_client_resource_api_version_for_app "$app")"
      resource_name="$(oauth2_secret_name_for_app "$app")"

      resource_group="${resource_api_version%/*}"
      resource_version="${resource_api_version#*/}"

      EXTRA_PATCHES+=$'\n'
      EXTRA_PATCHES+=$'  - target:\n'
      EXTRA_PATCHES+=$'      group: '"${resource_group}"$'\n'
      EXTRA_PATCHES+=$'      version: '"${resource_version}"$'\n'
      EXTRA_PATCHES+=$'      kind: '"${resource_kind}"$'\n'
      EXTRA_PATCHES+=$'      name: '"${resource_name}"$'\n'
      EXTRA_PATCHES+=$'    patch: |-\n'
      EXTRA_PATCHES+=$'      apiVersion: '"${resource_api_version}"$'\n'
      EXTRA_PATCHES+=$'      kind: '"${resource_kind}"$'\n'
      EXTRA_PATCHES+=$'      metadata:\n'
      EXTRA_PATCHES+=$'        name: '"${resource_name}"$'\n'
      EXTRA_PATCHES+=$'      $patch: delete\n'
    done
  fi

  #
  # Rewrite OnePassword item paths for remaining base resources
  #

  if ((${#onepassword_item_names[@]})); then
    for item_name in "${onepassword_item_names[@]}"; do
      if is_excluded_base_client_resource \
        "$item_name" \
        "$excluded_base_client_id_apps"; then
        continue
      fi

      item_path="$item_name"

      EXTRA_PATCHES+=$'\n'
      EXTRA_PATCHES+=$'  - patch: |-\n'
      EXTRA_PATCHES+=$'      - op: replace\n'
      EXTRA_PATCHES+=$'        path: "/spec/itemPath"\n'
      EXTRA_PATCHES+=$'        value: "vaults/'"${vault_name}"$'/items/'"${item_path}"$'"\n'
      EXTRA_PATCHES+=$'    target:\n'
      EXTRA_PATCHES+=$'      kind: OnePasswordItem\n'
      EXTRA_PATCHES+=$'      name: '"${item_name}"$'\n'
    done
  fi

  EXTRA_PATCHES="${EXTRA_PATCHES%$'\n'}"
  export EXTRA_PATCHES

  #
  # Authorized org/role pairs
  #

  if ((${#additional_user_orgs[@]})); then
    AUTHORIZED_ORG_ROLE_PAIRS="$(
      render_authorized_role_pairs \
        "$(authorized_org_id "$namespace")" \
        "${additional_user_orgs[@]}"
    )"
  else
    AUTHORIZED_ORG_ROLE_PAIRS="$(
      render_authorized_role_pairs \
        "$(authorized_org_id "$namespace")"
    )"
  fi

  export AUTHORIZED_ORG_ROLE_PAIRS

  #
  # Render
  #

  template="$(choose_template "$env_path")"
  target_dir="$ROOT/kustomize/overlays/$dir"

  mkdir -p "$target_dir"

  tmp="$(mktemp "$target_dir/.kustomization.yaml.XXXXXX")"

  envsubst \
    '$NAMESPACE $APP_INSTANCE_LABEL $ORG_ID $KAFKA_TOPIC $INGRESS_BASE_PATH $SERVLET_CONTEXT_PATH $STARTUP_PATH $READINESS_PATH $LIVENESS_PATH $METRICS_PATH $AUTHORIZED_ORG_ROLE_PAIRS $EXTRA_RESOURCES $EXTRA_APP_PATCHES $EXTRA_PATCHES $NOVARI_KAFKA_TOPIC_ORGID' \
    < "$template" > "$tmp"

  mv "$tmp" "$target_dir/kustomization.yaml"

done < <(
  {
    find "$ROOT/kustomize/overlays" \
      -name kustomization.yaml \
      -print

    printf '%s\n' \
      "$ROOT/kustomize/overlays/ra-no/beta/kustomization.yaml" \
      "$ROOT/kustomize/overlays/ra-no/api/kustomization.yaml"
  } |
    sort -u
)
