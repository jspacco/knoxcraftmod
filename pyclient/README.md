# Knoxcraft

Python library for [Knoxcraft Mod](https://github.com/jspacco/knoxcraftmod)

Knoxcraft enables students to write code in Java, Python, Blockly, or other languages that build 3D structures inside Minecraft using a Logo-like set of instructions.

Student code can have multiple "threads" of execution.

Students can upload their code to a modded server, run their programs, and watch their Terrapin turtles (or *Terps*) move around to build their structures.

*NOTE* You need a Minecraft server modded with [Knoxcraftmod](https://github.com/jspacco/knoxcraftmod). See [https://github.com/jspacco/knoxcraftmod](https://github.com/jspacco/knoxcraftmod) for more details.

## Example Serial (single-threaded) Python code

```python
from pyterp import Terp, TerpBlockType

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

## Example Parallel (multithreaded) Python code

```python
from knoxcraft import ParallelTerp, TerpBlockType

# Get the URL from your instructor
# Or just run the mod yourself
url = 'http://localhost:8080/upload'
# Your minecraft playername
minecraftPlayername = 'spacdog'
# If your instructor has username/password enabled
# These are very basic
username = 'test'
password = 'foobar123'

programName = "ptunnel"
description = "Draw a tunnel IN PARALLEL"

# 
terp = ParallelTerp(programName, description)
def thread1(t):
    t.left()
    for _ in range(3):
        t.forward()
        t.set_block(TerpBlockType.DIRT)
def thread2(t):
    t.right()
    for _ in range(3):
        t.forward()
        t.set_block(TerpBlockType.STONE)
def thread3(t):
    t.up()
    for _ in range(3):
        t.forward()
        t.set_block(TerpBlockType.SANDSTONE)

def thread4(t):
    t.down()
    for _ in range(3):
        t.forward()
        t.set_block(TerpBlockType.BRICKS)

terp.add_thread(thread1)
terp.add_thread(thread2)
terp.add_thread(thread3)
terp.add_thread(thread4)

# upload to the server
response = terp.upload(terp, url, minecraftPlayername, username, password)
print(response)
```