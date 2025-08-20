import re
"""
This script produces the javadoc documentation with images for
all of the block types. It's a very ugly process that is not that automated.

First we run BlockDumper on the server to produce the initial enum file.

Then extract just the enum portion — DIRT("minecraft:dirt"), — and put in
types.txt. Yes, we could parse the java file as well but we don't bother.

Then run blocks.py and pipe to list.txt, which repeatedly hits the api of the
minecraft wiki site to get all of the blocks. This code was heavily written
by GPT Sensei, so I'm sure smarter human work could produce better results.

Then we run this script (process.py) with types.txt and list.txt as inputs. 
We match up the enum keys (i.e. DIRT, IRON_ORE, OAK_DOOR, etc) from types.txt 
with the best matched image in list.txt. If there are multiple matching image,
we move the one we want to the top of the file.

One problem is that list.txt contains a ton of screenshots and other stuff
that accidentally matches. Again, smarter work with blocks.py might eliminate
this but web crawling even with the wiki API is slow, so we process the text file.
So we had to do some manual matching of things, again moving the entry we want
to the top of the file.

We couldn't find good matches for the following, which we found the image links
and manually added to the beginning of list.txt to force the match.
campfire, bamboo, carrots, conduit, command block, repeater, 
scaffolding, sniffer_egg

No idea what will happen when we run with different MC versions.
"""

def read_list(types, list_file):
    mapping = {}
    for line in open(list_file):
        (key, url) = line.strip().split("\t")
        if key.endswith('.ogg'):
            continue
        if key.startswith('Efe'):
            continue
        if key.startswith('SlotSprite'):
            continue
        if key.startswith('Invicon'):
            continue
        if '(texture)' in key:
            continue
        if '(item)' in key:
            continue
        if 'pre-release' in key:
            continue
        #if not re.search(r'JE\d+\.png', key) and not re.search(r'JE\d+ BE\d+', key):
        #    continue
        key = key.replace('.png', '').replace('.jpg', '').replace('.jpeg', '').replace('.gif', '')
        x = re.search(r'Block of (.+)', key)
        if x:
            rest = x.group(1)
            oldkey = key
            key = rest + " Block"
            #print(f"Rewrote {oldkey} to {key}")
            
        key = key.replace(" ", "_").upper()
        stuff = key.split("_")
        for i in range(len(stuff), -1, -1):
            lookup = '_'.join(stuff[0:i+1])
            if lookup in types:
                if lookup in mapping:
                    #print(f"Duplicate mapping for {lookup}: {mapping[lookup]} and {url}")
                    # don't overwrite existing mapping
                    break
                mapping[lookup] = url
                break
    return mapping

def read_types(type_file):
    types = {}
    for line in open(type_file):
        if line.strip() == "":
            continue
        try:
            (key, _) = line.strip().split("(")
            types[key] = line
        except ValueError as e:
            print(f"Error processing line {line}")
            raise e
    return types


def main():
    type_file = 'types-1.21.8.txt'
    block_list_file = 'list.txt'

    types = read_types(type_file)
    blocks = read_list(types, block_list_file)
    #print(f'enum has {len(types)} types, and {len(blocks)} blocks')

    missing = set(types.keys()) - set(blocks.keys())
    for m in missing:
        pass
        #print(f"Missing block for type: {m}")

    for key, full in types.items():
        if key in blocks:
            url = blocks[key]
            comment = f'''    /**
      * <img src="{url}"/>
      */'''
            print(comment)
            print(full.rstrip())
        else:
            print(full.rstrip())

if __name__ == "__main__":
    main()