#!/bin/bash

# =============================================================================
# GCP IAM 권한 부여 스크립트
#
# 사용법: ./grant-iam-permissions.sh
# 실행 전: TEAM_MEMBERS, PHOTO_TEAM_MEMBERS 배열에 이메일 추가
# =============================================================================

set -e

# -----------------------------------------------------------------------------
# 설정
# -----------------------------------------------------------------------------

PROJECT_ID="project-37afc2aa-d3d3-4a1a-8cd"

# 전체 팀원 이메일 (로그 뷰어 + 모니터링 뷰어 권한)
TEAM_MEMBERS=(
    # "팀원1@gmail.com"
    # "팀원2@gmail.com"
    # TODO: 팀원 이메일 추가
)

# 사진 담당 팀원 이메일 (GCS 권한 추가)
PHOTO_TEAM_MEMBERS=(
    # "사진담당자@gmail.com"
    # TODO: 사진 담당자 이메일 추가
)

# -----------------------------------------------------------------------------
# 함수
# -----------------------------------------------------------------------------

grant_role() {
    local email=$1
    local role=$2
    local role_name=$3

    echo "  → ${role_name} 권한 부여 중..."
    gcloud projects add-iam-policy-binding "$PROJECT_ID" \
        --member="user:${email}" \
        --role="${role}" \
        --quiet > /dev/null 2>&1
    echo "    ✓ 완료"
}

# -----------------------------------------------------------------------------
# 메인 실행
# -----------------------------------------------------------------------------

echo "========================================"
echo "GCP IAM 권한 부여 스크립트"
echo "프로젝트: ${PROJECT_ID}"
echo "========================================"
echo ""

# 팀원 수 확인
if [ ${#TEAM_MEMBERS[@]} -eq 0 ]; then
    echo "⚠️  TEAM_MEMBERS 배열이 비어있습니다."
    echo "   스크립트를 수정하여 팀원 이메일을 추가하세요."
    exit 1
fi

echo "📋 전체 팀원 권한 부여 (${#TEAM_MEMBERS[@]}명)"
echo "----------------------------------------"

for email in "${TEAM_MEMBERS[@]}"; do
    echo ""
    echo "👤 ${email}"
    grant_role "$email" "roles/logging.viewer" "로그 뷰어"
    grant_role "$email" "roles/monitoring.viewer" "모니터링 뷰어"
done

echo ""
echo ""

# 사진 담당 팀원 추가 권한
if [ ${#PHOTO_TEAM_MEMBERS[@]} -gt 0 ]; then
    echo "📸 사진 담당 팀원 추가 권한 (${#PHOTO_TEAM_MEMBERS[@]}명)"
    echo "----------------------------------------"

    for email in "${PHOTO_TEAM_MEMBERS[@]}"; do
        echo ""
        echo "👤 ${email}"
        grant_role "$email" "roles/storage.objectViewer" "GCS 객체 뷰어"
    done

    echo ""
fi

echo ""
echo "========================================"
echo "✅ 모든 권한 부여 완료!"
echo "========================================"
echo ""
echo "팀원들에게 다음 안내를 공유하세요:"
echo "  - GCP 콘솔: https://console.cloud.google.com"
echo "  - 로그 탐색기: https://console.cloud.google.com/logs/query"
echo "  - 가이드 문서: docs/infra/GCP_LOGGING_GUIDE.md"
