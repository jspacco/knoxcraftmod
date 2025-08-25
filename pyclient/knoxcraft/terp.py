import json
import requests

class Terp:
    """
    Represents a single-threaded Terp program.
    See also ParallelTerp for multi-threaded program.
    Methods correspond to Logo-like commands
    (forward, back, left, right, up, down, turn_left, turn_right, set_block).
    The blocks are defined in TerpBlockType.
    You can see the block images in the Javadoc here:
    https://jspacco.github.io/knoxcraftmod/javadoc/edu/knox/knoxcraftmod/client/TerpBlockType.html
    """
    CMD = "command"

    def __init__(self, name="default", description=""):
        self.name = name
        self.description = description
        self.instructions = []
        self.type = "serial"

    def forward(self):
        '''
        Move the terp forward one block in the direction it is facing.
        '''
        self._add_instruction({Terp.CMD: "forward"})

    def back(self):
        '''
        Move the terp backward one block in the direction it is facing.
        '''
        self._add_instruction({Terp.CMD: "back"})

    def left(self):
        '''
        Move the terp left one block, while still facing the same direction.
        This is like strafing in a first-person shooter.
        '''
        self._add_instruction({Terp.CMD: "left"})

    def right(self):
        '''
        Move the terp right one block, while still facing the same direction.
        This is like strafing in a first-person shooter.
        '''
        self._add_instruction({Terp.CMD: "right"})

    def up(self):
        '''
        Move the terp up one block in the direction it is facing.
        '''
        self._add_instruction({Terp.CMD: "up"})

    def down(self):
        '''
        Move the terp down one block in the direction it is facing.
        '''
        self._add_instruction({Terp.CMD: "down"})

    def turn_left(self):
        '''
        Turn the terp left 90 degrees.
        '''
        self._add_instruction({Terp.CMD: "turnleft"})

    def turn_right(self):
        '''
        Turn the terp right 90 degrees.
        '''
        self._add_instruction({Terp.CMD: "turnright"})
    
    def set_block(self, block_type):
        '''
        Set the block at the terp's current position to the given block type.
        The block types are defined in TerpBlockType.
        '''
        self._add_instruction({
            Terp.CMD: "setBlock",
            "blockType": block_type.value
        })

    def _add_instruction(self, instruction):
        self.instructions.append(instruction)

    def to_json(self):
        '''
        Convert the Terp program to JSON format.
        '''
        return json.dumps({
            "programName": self.name,
            "description": self.description,
            "instructions": self.instructions
        }, indent=2)
    
    def upload(self, url, minecraftPlayername, username="", password=""):
        return _upload(self, url, minecraftPlayername, username, password)


class ParallelTerp:
    def __init__(self, name="default", description=""):
        self.name = name
        self.description = description
        self.threads = []
        self.type = "parallel"

    def add_thread(self, threadFunction):
        '''
        Add a new thread to the parallel terp.
        The threadFunction parameter should be a function that takes an instance
        of type Terp object; this method will create a new Terp, pass it to
        the function, and then store the resulting instructions as a new thread.
        '''
        threadTerp = Terp()
        threadFunction(threadTerp)
        self.threads.append(threadTerp.instructions)
    
    def add_threads(self, threadFunctions):
        '''
        Add multiple threads to the parallel terp.
        The threadFunctions parameter should be a list of functions that take
        an instance of type Terp object; this method will create a new Terp
        for each function, pass it to the function, and then store the resulting
        instructions as a new thread.
        '''
        for func in threadFunctions:
            self.add_thread(func)

    def to_json(self):
        '''
        Convert the ParallelTerp program to JSON format.
        '''
        return json.dumps({
            "programName": self.name,
            "description": self.description,
            "threads": self.threads,
            "type": "parallel"
        }, indent=2)
    
    def upload(self, url, minecraftPlayername, username="", password=""):
        return _upload(self, url, minecraftPlayername, username, password)

def _upload(terp, url, minecraftPlayername, username="", password=""):
    """
    Uploads the Terp (parallel or serial) to the server at
    the given URL.
    """
    headers = {
        "Content-Type": "application/json",
        "X-MinecraftPlayername": minecraftPlayername,
        "X-Type": 'parallel' if isinstance(terp, ParallelTerp) else 'serial'
    }
    if username and password:
        headers["X-Username"] = username
        headers["X-Password"] = password

    payload = terp.to_json()

    response = requests.post(url, data=payload, headers=headers)

    if response.status_code == 200:
        return response.text
    else:
        raise Exception(f"Failed to upload Terp: {response.status_code} - {response.text}")
