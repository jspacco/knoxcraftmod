# PyTerp

Python library for [Knoxcraft Mod](https://github.com/jspacco/knoxcraftmod)

Knoxcraft enables students to write code in Java, Python, Blockly, or other languages that build 3D structures inside Minecraft using a Logo-like set of instructions.

## Example Python code

```python
from pyterp import Terp

# Get the URL from your instructor
# Or just run the mod yourself
url = 'http://localhost:8080/upload'
# Your minecraft playername
minecraftPlayername = 'spacdog'
# If your instructor has username/password enabled
# These are very basic
username = 'test'
password = 'foobar123'

# this draws a pyramid that is 10x10 at the base, and then tapers
t = Terp("pyramid", "Draw a pyramid")
for base in range(10, -1, -2):
    for i in range(base):
        for j in range(base):
            t.forward()
            t.set_block(TerpBlockType.SANDSTONE)
        for h in range(base):
            t.back()
        t.right()
    for i in range(base):
        t.left()
    t.forward()
    t.right()
    t.up()

# upload to the server
response = t.upload(url, minecraftPlayername, username, password)
print(response)
```

