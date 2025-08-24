import re

def main():
    """
    Generate the TerpBlockType enum from the types-1.21.8.txt file.
    The output is printed to standard out and can be redirected to
    a file. Right now that's the bottom of knoxcraft/terp.py file.
    """
    print('''from enum import Enum

class TerpBlockType(Enum):
    """
    Represents a block type in the Terp system.
    """
''')
    for line in open('types-1.21.8.txt', 'r'):
        line = re.sub(r'(#.*)|(//.*)', '', line).strip()
        if line:
            # ACACIA_PLANKS('minecraft:acacia_planks'),
            match = re.match(r"(\w+)\(\"([^\"]+)\"\)", line)
            if match:
                block_name = match.group(1)
                block_id = match.group(2)
                # print(f"Block Name: {block_name}, Block ID: {block_id}")
                print(f'    {block_name} = "{block_id}"')

if __name__ == "__main__":
    main()