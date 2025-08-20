import time, requests

API = "https://minecraft.wiki/api.php"
S = requests.Session()
S.headers["User-Agent"] = "KnoxcraftCrawler/1.0 (+contact)"

def api_get(**params):
    params.setdefault("format", "json")
    params.setdefault("formatversion", 2)
    for _ in range(4):
        r = S.get(API, params=params, timeout=30)
        if r.status_code in (429, 503):
            time.sleep(1.0); continue
        r.raise_for_status()
        return r.json()
    raise RuntimeError("API retries exhausted")

def list_category_members(cattitle, cmtype="page|subcat"):
    out, cont = [], {}
    while True:
        data = api_get(action="query", list="categorymembers",
                       cmtitle=cattitle, cmtype=cmtype, cmlimit="max", **cont)
        out += data["query"]["categorymembers"]
        cont = data.get("continue") or {}
        if not cont: break
    return out

def pages_images(titles):
    out, cont = {}, {}
    # 50 titles per batch
    for i in range(0, len(titles), 50):
        batch = titles[i:i+50]
        cont = {}
        while True:
            data = api_get(action="query", prop="images",
                           titles="|".join(batch), imlimit="max", **cont)
            for p in data["query"]["pages"]:
                out.setdefault(p["title"], [])
                out[p["title"]].extend([im["title"] for im in p.get("images", [])])
            cont = data.get("continue") or {}
            if not cont: break
    return out

def imageinfo(files):
    out, cont = {}, {}
    for i in range(0, len(files), 50):
        batch = files[i:i+50]
        data = api_get(action="query", prop="imageinfo", titles="|".join(batch),
                       iiprop="url|mime|size")
        for p in data["query"]["pages"]:
            ii = (p.get("imageinfo") or [None])[0]
            if ii: out[p["title"]] = ii["url"]
    return out

def main():
    root_cat = "Category:Blocks"
    print(f"Gathering block pages in {root_cat}…")
    pages = [p["title"] for p in list_category_members(root_cat, "page|subcat")]
    print(f"Found {len(pages)} block pages")

    print("Finding images used on block pages…")
    page_files = pages_images(pages)
    print(f"Found {sum(len(v) for v in page_files.values())} images across {len(page_files)} pages")

    all_files = sorted({f for lst in page_files.values() for f in lst})
    print(f"Found {len(all_files)} unique files referenced on block pages")

    print("Fetching original URLs for files…")
    file_urls = imageinfo(all_files)
    
    # Example output
    for file, url in file_urls.items():
        print(f"{file}: {url}")
    

if __name__ == "__main__":
    main()