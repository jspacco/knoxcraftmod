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