import nbtlib
import json
import sys

if len(sys.argv) < 2:
    print("Usage: python nbtprint.py <nbt_file>")
    sys.exit(1)

nbt = nbtlib.load(sys.argv[1])

json_data = json.dumps(nbt, indent=2)

print(json_data)