#!/usr/bin/env bash
set -euo pipefail

INSTALLER="$1"
MOD_FILE="$2"
SERVER_DIR="$3"
if [[ -z "$INSTALLER" || -z "$MOD_FILE" || -z "$SERVER_DIR" ]]; then
    echo "Usage: $0 <installer-jar> <modfile> <server-dir>"
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

# $SERVER_DIR should either exist and be empty, or not exist
if [[ -d "$SERVER_DIR" ]]; then
    if [[ -n "$(ls -A $SERVER_DIR 2>/dev/null)" ]]; then
        echo "Error: The server directory '$SERVER_DIR' is not empty."
        echo "Please run this script in an empty directory."
        exit 1
    fi
else
    mkdir -p "$SERVER_DIR"
fi


# 1) Install Forge server into $SERVER_DIR
java -jar "$INSTALLER" --installServer $SERVER_DIR

# 2) Drop your pre-baked config files BEFORE first run
cat > $SERVER_DIR/eula.txt <<'EOF'
eula=true
EOF

cat > $SERVER_DIR/server.properties <<'EOF'
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

mkdir $SERVER_DIR/mods
cp $MOD_FILE $SERVER_DIR/mods/

# 4) First run
#   The run script is created by the installer; use nogui to avoid GUI pop.
cd $SERVER_DIR
bash run.sh --nogui
