from knoxcraft import Terp, TerpBlockType

def main():
    # TODO: replace the URL, minecraftPlayername, username, and password 
    # with values provided by your instructor.
    url = 'http://localhost:8080/upload'
    minecraftPlayername = 'spacdog'
    username = 'test'
    password = 'foobar123'

    t = Terp("pyramid", "Draw a pyramid.")
    for base in range(8, -1, -2):
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
    
    response = t.upload(t, url, minecraftPlayername, username, password)
    print(response)

if __name__ == "__main__":
    main()