#!/usr/bin/env bash
set -euo pipefail

# e.g., ./my-new-server
MC_DIR="$1"
if [[ -z "$MC_DIR" ]]; then
    echo "Usage: $0 <minecraft-server-directory>"
    exit 1
fi

# installer jar
INSTALLER="forge-1.21-51.0.33-installer.jar"
# mod file
MOD_FILE="build/libs/knoxcraftmod-0.0.1.jar"

mkdir -p "$MC_DIR"
cp "$INSTALLER" "$MC_DIR/"
cd "$MC_DIR"

# 1) Install Forge server
java -jar "$INSTALLER" --installServer .

# 2) Drop your pre-baked config files BEFORE first run
cat > eula.txt <<'EOF'
eula=true
EOF

cat > server.properties <<'EOF'
spawn-animals=true
spawn-monsters=false
spawn-npcs=false
difficulty=normal
gamemode=creative
spawn-protection=0
generate-structures=false
level-type=flat
generator-settings={}
view-distance=10
simulation-distance=10
motd=Knoxcraft Server
EOF

cp ../$MOD_FILE mods/

# 4) First run
#   The run script is created by the installer; use nogui to avoid GUI pop.
bash run.sh --nogui
