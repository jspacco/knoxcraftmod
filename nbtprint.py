# /Library/Frameworks/Python.framework/Versions/3.12/bin/python3 nbtprint.py > OUT
import nbtlib
import json

nbt = nbtlib.load('run/saves/New World/data/toro.dat')
#print(nbt)

json_data = json.dumps(nbt, indent=2)

print(json_data)