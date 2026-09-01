#!/bin/bash
set -e

# Uploads card images to the VPS without doing a full deploy.
# Run this whenever you add or update images in frontend/public/cards/.

VPS_IP="${VPS_IP:-31.97.169.38}"
VPS_USER="${VPS_USER:-root}"

BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"
CARDS_DIR="${BASE_DIR}/frontend/public/cards"

if ! find "${CARDS_DIR}" -name '*.webp' -print -quit 2>/dev/null | grep -q .; then
  echo "No card images found in ${CARDS_DIR}"
  exit 1
fi

COUNT=$(find "${CARDS_DIR}" -name '*.webp' | wc -l | tr -d ' ')
SIZE=$(du -sh "${CARDS_DIR}" | cut -f1)

echo "Uploading ${COUNT} card images (${SIZE}) to ${VPS_USER}@${VPS_IP}..."

ssh ${VPS_USER}@${VPS_IP} "mkdir -p /var/www/gwent/cards"
scp -r "${CARDS_DIR}/"* ${VPS_USER}@${VPS_IP}:/var/www/gwent/cards/

echo "Done. Card images synced to /var/www/gwent/cards/"
