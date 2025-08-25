import re

def main():
    """
    Generate blockAliases.js from the types-1.21.8.txt file.
    The output is printed to standard out and can be redirected to
    a file. This is a JSON file loaded by blockly.html.

    We put the block types in a separate file because
    there are a lot of them and the HTML file gets too big.
    """
    lines = []
    for line in open('types-1.21.8.txt', 'r'):
        line = re.sub(r'(#.*)|(//.*)', '', line).strip()
        if line:
            # ACACIA_PLANKS('minecraft:acacia_planks'),
            match = re.match(r"(\w+)\(\"([^\"]+)\"\)", line)
            if match:
                block_name = match.group(1)
                block_id = match.group(2)
                # print(f"Block Name: {block_name}, Block ID: {block_id}")
                lines.append(f'  "{block_name}" : "{block_id}"')
    
    print('{')
    print(',\n'.join(lines))
    print('}')
if __name__ == "__main__":
    main()