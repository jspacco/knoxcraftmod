#!/usr/bin/env bash
#set -euo pipefail

INSTALLER="$1"
MOD_FILE="$2"
if [[ -z "$INSTALLER" || -z "$MOD_FILE" ]]; then
    echo "Usage: $0 <installer-jar> <modfile>"
    exit 1
fi

# check if $INSTALLER and $MOD_FILE exist
if [[ ! -f "$INSTALLER" ]]; then
    echo "Installer jar '$INSTALLER' does not exist."
    exit 1
fi
if [[ ! -f "$MOD_FILE" ]]; then
    echo "Mod file '$MOD_FILE' does not exist."
    exit 1
fi

echo "Going to install a Minecraft server into the current directory? This directory should be empty, other than the installer jar, mod file, mods folder, and this script."

read -p "Press Enter to continue or Ctrl+C to cancel."

# the current directory should be empty
if [[ -n "$(ls -A . 2>/dev/null)" ]]; then
    echo "Error: The current directory is not empty. Please run this script in an empty directory."
    echo "The only files allowed are the installer jar, mod file, and mods folder."
    exit 1
fi

# copy the installer jar to the current directory
cp "$INSTALLER" .

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

mkdir mods
cp $MOD_FILE mods/

# 4) First run
#   The run script is created by the installer; use nogui to avoid GUI pop.
bash run.sh --nogui
